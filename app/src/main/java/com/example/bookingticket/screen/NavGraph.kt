package com.example.bookingticket.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bookingticket.model.FirestoreRepository

@Composable
fun NavGraph(navController: NavHostController, repo: FirestoreRepository) {
    NavHost(navController = navController, startDestination = "tickets") {
        composable("tickets") {
            TicketsListScreen(repo, onOpenPassengers = { ticketId -> navController.navigate("passengers/$ticketId") }, onOpenSeats = { ticketId -> navController.navigate("seats/$ticketId") })
        }
        composable("passengers/{ticketId}") { backStack ->
            val ticketId = backStack.arguments?.getString("ticketId") ?: return@composable
            PassengerScreen(ticketId = ticketId, repo = repo, onBack = { navController.popBackStack() })
        }
        composable("seats/{ticketId}") { backStack ->
            val ticketId = backStack.arguments?.getString("ticketId") ?: return@composable
            SeatAvailabilityScreen(ticketId = ticketId, repo = repo, onBack = { navController.popBackStack() })
        }
    }
}