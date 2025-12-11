package com.example.mybabywallet

object WalletUtils {

    // Calcula el saldo final
    fun calcularSaldo(totalIngresos: Double, totalGastos: Double): Double {
        return totalIngresos - totalGastos
    }

    // Valida si un monto ingresado por el usuario es válido
    fun esMontoValido(montoTexto: String): Boolean {
        if (montoTexto.isBlank()) return false
        val numero = montoTexto.toDoubleOrNull()
        return numero != null && numero > 0
    }

    // Determina si estamos en números rojos (deuda)
    fun esSaldoNegativo(saldo: Double): Boolean {
        return saldo < 0
    }
}