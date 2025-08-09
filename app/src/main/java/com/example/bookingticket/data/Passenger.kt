package com.example.bookingticket.data

import com.google.firebase.firestore.DocumentId


data class Passenger(
    @DocumentId
    var id: String = "",
    var ticketId: String = "",
    var name: String = "",
    var seatNumber: Int = -1,
    var phone: String = ""
)