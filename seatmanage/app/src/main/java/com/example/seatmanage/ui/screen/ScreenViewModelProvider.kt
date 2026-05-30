package com.example.seatmanage.ui.screen

import com.example.seatmanage.viewmodel.AuthViewModel
import com.example.seatmanage.viewmodel.ReservationViewModel
import com.example.seatmanage.viewmodel.SeatViewModel

class ScreenViewModelProvider(
    val authViewModel: AuthViewModel,
    val seatViewModel: SeatViewModel,
    val reservationViewModel: ReservationViewModel
)
