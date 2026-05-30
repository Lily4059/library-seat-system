package com.example.seatmanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.seatmanage.ui.navigation.AppNavGraph
import com.example.seatmanage.ui.screen.ScreenViewModelProvider
import com.example.seatmanage.ui.theme.SeatmanageTheme
import com.example.seatmanage.viewmodel.AuthViewModel
import com.example.seatmanage.viewmodel.ReservationViewModel
import com.example.seatmanage.viewmodel.SeatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        val seatViewModel = ViewModelProvider(this)[SeatViewModel::class.java]
        val reservationViewModel = ViewModelProvider(this)[ReservationViewModel::class.java]
        val vmProvider = ScreenViewModelProvider(authViewModel, seatViewModel, reservationViewModel)

        setContent {
            SeatmanageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        viewModelProvider = { vmProvider }
                    )
                }
            }
        }
    }
}
