package com.example.bookingticket.model

import com.example.bookingticket.data.Passenger
import com.example.bookingticket.data.Ticket
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val db: FirebaseFirestore) {
    private val ticketsCol = db.collection("ticket")

    suspend fun addTicket(ticket: Ticket): String {
        val ref = ticketsCol.add(ticket).await()
        return ref.id
    }

    suspend fun getTicket(ticketId: String): Ticket? {
        val snap = ticketsCol.document(ticketId).get().await()
        return snap.toObject(Ticket::class.java)?.copy(id = snap.id)
    }


    /**
     * Adds a passenger inside a transaction while ensuring seatNumber uniqueness for the ticket.
     */
    suspend fun addPassengerWithSeatCheck(passenger: Passenger): String {
        val ticketDoc = ticketsCol.document(passenger.ticketId)
        val passengersCol = ticketDoc.collection("passengers")

        // 1. Check seat availability before transaction
        val existingPassengers = passengersCol.get().await()
        val taken = existingPassengers.documents.any { doc ->
            val existing = doc.toObject(Passenger::class.java)
            existing != null && existing.seatNumber == passenger.seatNumber
        }
        if (passenger.seatNumber > 0 && taken) {
            throw FirebaseFirestoreException(
                "Seat already taken",
                FirebaseFirestoreException.Code.ABORTED
            )
        }

        // 2. Create passenger inside transaction
        val resultRef = db.runTransaction { transaction ->
            val newRef = passengersCol.document()
            transaction.set(newRef, passenger)
            newRef.id
        }.await()

        return resultRef
    }


    suspend fun updatePassenger(passenger: Passenger) {
        ticketsCol.document(passenger.ticketId)
            .collection("passengers")
            .document(passenger.id)
            .set(passenger)
            .await()
    }

    suspend fun deletePassenger(ticketId: String, passengerId: String) {
        ticketsCol.document(ticketId)
            .collection("passengers")
            .document(passengerId)
            .delete()
            .await()
    }

    fun observePassengers(
        ticketId: String,
        onUpdate: (List<Passenger>) -> Unit
    ): ListenerRegistration {
        return ticketsCol.document(ticketId)
            .collection("passengers")
            .addSnapshotListener { snap, e ->
                if (e != null) return@addSnapshotListener
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Passenger::class.java)?.copy(id = it.id)
                } ?: emptyList()
                onUpdate(list)
            }
    }

    suspend fun listPassengers(ticketId: String): List<Passenger> {
        val snap = ticketsCol.document(ticketId).collection("passengers").get().await()
        return snap.documents.mapNotNull { it.toObject(Passenger::class.java)?.copy(id = it.id) }
    }

    fun streamTickets(): Flow<List<Ticket>> = callbackFlow {
        val listener = ticketsCol.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            val list = snap?.documents
                ?.mapNotNull { it.toObject(Ticket::class.java)?.copy(id = it.id) }
                ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    fun streamPassengers(ticketId: String): Flow<List<Passenger>> = callbackFlow {
        val listener = ticketsCol.document(ticketId)
            .collection("passengers")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { it.toObject(Passenger::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun observeTicket(ticketId: String, onUpdate: (Ticket?) -> Unit): ListenerRegistration {
        return ticketsCol.document(ticketId)
            .addSnapshotListener { snap, e ->
                if (e != null) return@addSnapshotListener
                val ticket = snap?.toObject(Ticket::class.java)?.copy(id = snap.id)
                onUpdate(ticket)
            }
    }
}