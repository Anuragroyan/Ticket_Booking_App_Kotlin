package com.example.bookingticket.data

import com.google.firebase.firestore.DocumentId

data class Ticket(
    @DocumentId
    var id: String = "",
    var route: String = "",
    var date: String = "",
    var totalSeats: Int = 100
)
