package com.example.bookingticket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingticket.data.Passenger
import com.example.bookingticket.data.Ticket
import com.example.bookingticket.model.FirestoreRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PassengerViewModel(private val repo: FirestoreRepository): ViewModel() {
    private val _passengers = MutableStateFlow<List<Passenger>>(emptyList())
    val passengers: StateFlow<List<Passenger>> = _passengers

    private val _ticketDetails = MutableStateFlow<Ticket?>(null)
    val ticketDetails: StateFlow<Ticket?> = _ticketDetails

    private var passengerListener: ListenerRegistration? = null
    private var ticketListener: ListenerRegistration? = null
    private var passengersListener: ListenerRegistration? = null

    fun setPassengers(list: List<Passenger>) {
        _passengers.value = list
    }

    fun fetchAndObserve(ticketId: String) {
        viewModelScope.launch {
            // Step 1: Initial quick load
            val initialList = repo.listPassengers(ticketId)
            setPassengers(initialList)

            // Step 2: Real-time updates
            passengerListener?.remove()
            passengerListener = repo.observePassengers(ticketId) { list ->
                setPassengers(list)
            }

            // Real-time ticket details too
            ticketListener?.remove()
            ticketListener = repo.observeTicket(ticketId) { ticket ->
                _ticketDetails.value = ticket
            }
        }
    }

    override fun onCleared() {
        passengerListener?.remove()
        ticketListener?.remove()
        super.onCleared()
    }
    fun observePassengers(ticketId: String) {
        viewModelScope.launch {
            repo.streamPassengers(ticketId).collect { _passengers.value = it }
        }
        viewModelScope.launch {
            val t = repo.getTicket(ticketId)
            _ticketDetails.value = t
        }
    }

    fun addPassenger(passenger: Passenger, onError: (String?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.addPassengerWithSeatCheck(passenger)
            } catch (e: Exception) {
                onError(e.message)
            }
        }
    }

    fun updatePassenger(passenger: Passenger, onError: (String?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.updatePassenger(passenger)
            } catch (e: Exception) {
                onError(e.message)
            }
        }
    }

    fun deletePassenger(ticketId: String, passengerId: String, onError: (String?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.deletePassenger(ticketId, passengerId)
            } catch (e: Exception) {
                onError(e.message)
            }
        }
    }

    fun observeTicketAndPassengers(ticketId: String) {
        ticketListener?.remove()
        passengersListener?.remove()

        ticketListener = repo.observeTicket(ticketId) { ticket ->
            _ticketDetails.value = ticket
        }

        passengersListener = repo.observePassengers(ticketId) { passengerList ->
            _passengers.value = passengerList
        }
    }

}