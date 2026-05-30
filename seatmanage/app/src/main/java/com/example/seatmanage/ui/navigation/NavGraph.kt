package com.example.seatmanage.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.seatmanage.ui.screen.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object BindStudent : Screen("bind_student")
    object Home : Screen("home")
    object SeatMap : Screen("seat_map")
    object MyReservations : Screen("my_reservations")
    object Profile : Screen("profile")
    object History : Screen("history")
    object CreditLog : Screen("credit_log")
    object CreditStrategy : Screen("credit_strategy")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    object Favorites : Screen("favorites")
    object HelpFeedback : Screen("help_feedback")
    object StudyTime : Screen("study_time")
    object CheckIn : Screen("check_in")
    object Confirm : Screen("confirm/{seatId}/{date}/{startTime}/{endTime}")
    object CurrentSeat : Screen("current_seat")
}

@Composable
fun AppNavGraph(navController: NavHostController, viewModelProvider: () -> ScreenViewModelProvider) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = viewModelProvider().authViewModel,
                onLoginSuccess = { needsBind ->
                    viewModelProvider().reservationViewModel.loadMyReservations()
                    if (needsBind) navController.navigate(Screen.BindStudent.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    } else navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.BindStudent.route) {
            BindStudentScreen(
                authViewModel = viewModelProvider().authViewModel,
                onBindSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.BindStudent.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                seatViewModel = viewModelProvider().seatViewModel,
                reservationViewModel = viewModelProvider().reservationViewModel,
                authViewModel = viewModelProvider().authViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onNavigateSeatDetail = { seatId, date, start, end ->
                    navController.navigate("confirm/$seatId/$date/$start/$end")
                }
            )
        }
        composable(Screen.SeatMap.route) {
            SeatMapScreen(
                seatViewModel = viewModelProvider().seatViewModel,
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Screen.MyReservations.route) {
            MyReservationsScreen(
                reservationViewModel = viewModelProvider().reservationViewModel,
                seatViewModel = viewModelProvider().seatViewModel,
                onBack = { navController.popBackStack() },
                onCheckIn = { navController.navigate(Screen.CheckIn.route) }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = viewModelProvider().authViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                reservationViewModel = viewModelProvider().reservationViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CreditLog.route) {
            CreditLogScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.CreditStrategy.route) {
            CreditStrategyScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                authViewModel = viewModelProvider().authViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.HelpFeedback.route) {
            HelpFeedbackScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.StudyTime.route) {
            StudyTimeScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.CheckIn.route) {
            CheckInScreen(
                reservationViewModel = viewModelProvider().reservationViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Confirm.route) { backStackEntry ->
            val seatId = backStackEntry.arguments?.getString("seatId") ?: ""
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val startTime = backStackEntry.arguments?.getString("startTime") ?: ""
            val endTime = backStackEntry.arguments?.getString("endTime") ?: ""
            ConfirmScreen(
                seatId = seatId, date = date, startTime = startTime, endTime = endTime,
                seatViewModel = viewModelProvider().seatViewModel,
                reservationViewModel = viewModelProvider().reservationViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.CurrentSeat.route) {
            CurrentSeatScreen(
                reservationViewModel = viewModelProvider().reservationViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
