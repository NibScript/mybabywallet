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
                    // LOGIN
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
                            onLoginSuccess = { userId ->
                                navController.navigate("home/$userId") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate("registro") }
                        )
                    }

                    // REGISTRO
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

                    // HOME
                    composable(
                        route = "home/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.IntType }),
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(700))
                        },
                        exitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(700))
                        }
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getInt("userId") ?: 0

                        WalletScreen(
                            usuarioId = userId,
                            onLogout = {
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