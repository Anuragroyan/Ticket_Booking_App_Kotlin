package com.example.bookingticket.screen

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bookingticket.model.FirestoreRepository
import com.example.bookingticket.viewmodel.PassengerViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatAvailabilityScreen(ticketId: String, repo: FirestoreRepository, onBack: () -> Unit) {
    val vm = remember { PassengerViewModel(repo) }
    val passengers by vm.passengers.collectAsState()
    val ticketDetails by vm.ticketDetails.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ticketId) { vm.observePassengers(ticketId) }

    val totalSeats = ticketDetails?.totalSeats ?: 40
    val occupied = passengers.map { it.seatNumber }.filter { it > 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seat Availability") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            Text(text = "Route: ${ticketDetails?.route ?: "-"}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Date: ${ticketDetails?.date ?: "-"}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))

            val cols = 4
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items((1..totalSeats step cols).toList()) { rowStart ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 0 until cols) {
                            val seatNum = rowStart + i
                            if (seatNum > totalSeats) break
                            val isTaken = occupied.contains(seatNum)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isTaken) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
                                )
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Seat $seatNum", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        if (isTaken) "Taken" else "Free",
                                        style = MaterialTheme.typography.labelMedium
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

