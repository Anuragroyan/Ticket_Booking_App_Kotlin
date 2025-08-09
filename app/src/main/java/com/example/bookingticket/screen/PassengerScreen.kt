package com.example.bookingticket.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookingticket.data.Passenger
import com.example.bookingticket.model.FirestoreRepository
import com.example.bookingticket.viewmodel.PassengerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerScreen(
    ticketId: String,
    repo: FirestoreRepository,
    onBack: () -> Unit
) {
    val vm = remember { PassengerViewModel(repo) }
    val passengers by vm.passengers.collectAsState()
    val ticketDetails by vm.ticketDetails.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var seatText by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Passenger?>(null) }
    var showDeleteDialogFor by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(ticketId) {
        vm.observePassengers(ticketId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Passengers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            Text("Add / Edit Passenger", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = seatText,
                onValueChange = { seatText = it },
                label = { Text("Seat Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val seat = seatText.toIntOrNull() ?: -1
                    if (name.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Name is required") }
                        return@Button
                    }
                    val totalSeats = ticketDetails?.totalSeats ?: 40
                    if (seat <= 0 || seat > totalSeats) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Seat must be between 1 and $totalSeats"
                            )
                        }
                        return@Button
                    }

                    if (editing == null) {
                        val p = Passenger(
                            ticketId = ticketId,
                            name = name.trim(),
                            phone = phone.trim(),
                            seatNumber = seat
                        )
                        vm.addPassenger(p) { err ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    err ?: "Error adding passenger"
                                )
                            }
                        }
                    } else {
                        val p = editing!!.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            seatNumber = seat
                        )
                        vm.updatePassenger(p) { err ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    err ?: "Error updating passenger"
                                )
                            }
                        }
                        editing = null
                    }
                    name = ""
                    phone = ""
                    seatText = ""
                }) {
                    Text(if (editing == null) "Add" else "Save")
                }

                Spacer(Modifier.width(8.dp))
                if (editing != null) {
                    Button(onClick = {
                        editing = null
                        name = ""
                        phone = ""
                        seatText = ""
                    }) { Text("Cancel") }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Passengers list", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(passengers) { p ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(p.name)
                                Text("Seat: ${p.seatNumber}")
                                if (p.phone.isNotBlank()) Text(p.phone)
                            }
                            Row {
                                Text(
                                    "Edit",
                                    modifier = Modifier
                                        .clickable {
                                            editing = p
                                            name = p.name
                                            phone = p.phone
                                            seatText = p.seatNumber.toString()
                                        }
                                        .padding(8.dp)
                                )
                                Text(
                                    "Delete",
                                    modifier = Modifier
                                        .clickable { showDeleteDialogFor = p.id }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialogFor != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialogFor = null },
                title = { Text("Delete passenger") },
                text = { Text("Are you sure you want to delete this passenger?") },
                confirmButton = {
                    TextButton(onClick = {
                        val id = showDeleteDialogFor!!
                        vm.deletePassenger(ticketId, id) { err ->
                            scope.launch {
                                snackbarHostState.showSnackbar(err ?: "Error deleting")
                            }
                        }
                        showDeleteDialogFor = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialogFor = null }) { Text("Cancel") }
                }
            )
        }
    }
}
