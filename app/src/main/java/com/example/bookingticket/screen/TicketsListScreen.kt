package com.example.bookingticket.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookingticket.data.Ticket
import com.example.bookingticket.model.FirestoreRepository
import com.example.bookingticket.viewmodel.TicketsViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsListScreen(
    repo: FirestoreRepository,
    onOpenPassengers: (String) -> Unit,
    onOpenSeats: (String) -> Unit
) {
    val vm = remember { TicketsViewModel(repo) }
    val tickets by vm.tickets.collectAsState()
    var query by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Tickets") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    try {
                        val t = Ticket(
                            route = "City A -> City B",
                            date = "2025-08-10",
                            totalSeats = 40
                        )
                        repo.addTicket(t)
                        snackbarHostState.showSnackbar("Sample ticket added")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error: ${e.message}")
                    }
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add ticket")
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search route or date") }
            )
            Spacer(Modifier.height(12.dp))
            val results = if (query.isBlank()) tickets else vm.search(query)
            if (results.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tickets found")
                }
            } else {
                LazyColumn {
                    items(results) { ticket ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = ticket.route,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(text = ticket.date)
                                }
                                Row {
                                    Text(
                                        text = "Passengers",
                                        modifier = Modifier
                                            .clickable { onOpenPassengers(ticket.id) }
                                            .padding(8.dp)
                                    )
                                    Text(
                                        text = "Seats",
                                        modifier = Modifier
                                            .clickable { onOpenSeats(ticket.id) }
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
