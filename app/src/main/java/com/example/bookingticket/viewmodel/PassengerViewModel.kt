package com.example.bookingticket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingticket.data.Passenger
import com.example.bookingticket.data.Ticket
import com.example.bookingticket.model.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PassengerViewModel(private val repo: FirestoreRepository): ViewModel() {
    private val _passengers = MutableStateFlow<List<Passenger>>(emptyList())
    val passengers: StateFlow<List<Passenger>> = _passengers

    private val _ticketDetails = MutableStateFlow<Ticket?>(null)
    val ticketDetails: StateFlow<Ticket?> = _ticketDetails

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

    suspend fun isSeatTaken(ticketId: String, seatNumber: Int): Boolean {
        return repo.isSeatTaken(ticketId, seatNumber)
    }
}