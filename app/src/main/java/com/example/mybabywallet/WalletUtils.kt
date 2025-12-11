package com.example.mybabywallet

object WalletUtils {

    // Función 1: Calcula el saldo final
    fun calcularSaldo(totalIngresos: Double, totalGastos: Double): Double {
        return totalIngresos - totalGastos
    }

    // Función 2: Valida si un monto ingresado por el usuario es válido
    fun esMontoValido(montoTexto: String): Boolean {
        if (montoTexto.isBlank()) return false
        val numero = montoTexto.toDoubleOrNull()
        return numero != null && numero > 0
    }

    // Función 3: Determina si estamos en números rojos (deuda)
    fun esSaldoNegativo(saldo: Double): Boolean {
        return saldo < 0
    }
}