package com.example.bookingticket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingticket.data.Ticket
import com.example.bookingticket.model.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TicketsViewModel(private val repo: FirestoreRepository): ViewModel() {
    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _tickets

    init {
        viewModelScope.launch {
            repo.streamTickets().collect { _tickets.value = it }
        }
    }

    fun search(query: String): List<Ticket> {
        val q = query.lowercase().trim()
        return _tickets.value.filter { it.route.lowercase().contains(q) || it.date.contains(q) }
    }
}