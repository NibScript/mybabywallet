package com.example.mybabywallet.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mybabywallet.ui.theme.MybabywalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MybabywalletTheme {
                val navController = rememberNavController()

                // Sistema de Navegación
                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    // PANTALLA 1: LOGIN
                    composable(
                        route = "login",
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(700))
                        },
                        exitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(700))
                        }
                    ) {
                        LoginScreen(
                            // AHORA RECIBIMOS EL ID DEL USUARIO (userId)
                            onLoginSuccess = { userId ->
                                // Navegamos a home llevándonos el ID en la ruta: "home/1", "home/2", etc.
                                navController.navigate("home/$userId") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate("registro") }
                        )
                    }

                    // PANTALLA 2: REGISTRO
                    composable(
                        route = "registro",
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(700))
                        },
                        exitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(700))
                        }
                    ) {
                        RegistroScreen(
                            onRegisterSuccess = { navController.popBackStack() },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // PANTALLA 3: HOME (Ahora dinámica por usuario)
                    composable(
                        route = "home/{userId}", // <--- Ruta dinámica
                        arguments = listOf(navArgument("userId") { type = NavType.IntType }), // <--- Definimos que recibe un número
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(700))
                        },
                        exitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(700))
                        }
                    ) { backStackEntry ->
                        // Recuperamos el ID que venía en la ruta
                        val userId = backStackEntry.arguments?.getInt("userId") ?: 0

                        WalletScreen(
                            usuarioId = userId, // <--- Se lo pasamos a la pantalla
                            onLogout = {
                                // Al salir, volvemos al login y borramos historial
                                navController.navigate("login") {
                                    popUpTo("home/{userId}") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}