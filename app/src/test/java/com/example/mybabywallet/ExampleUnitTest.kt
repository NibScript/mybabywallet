package com.example.mybabywallet

import org.junit.Test
import org.junit.Assert.*

class WalletUtilsTest {

    // Verificar que la resta de saldo sea correcta
    @Test
    fun calcularSaldo_matematicaCorrecta() {
        val ingresos = 10000.0
        val gastos = 4500.0

        val resultadoEsperado = 5500.0
        val resultadoReal = WalletUtils.calcularSaldo(ingresos, gastos)

        // Tiene que ser igual
        assertEquals(resultadoEsperado, resultadoReal, 0.0)
    }

    // Verificar que no acepte textos vacíos o letras como dinero
    @Test
    fun validacionMonto_letrasDanFalso() {
        val inputUsuario = "hola"
        val esValido = WalletUtils.esMontoValido(inputUsuario)

        // Tiene que ser Falso
        assertFalse(esValido)
    }

    // Verificar que acepte números positivos
    @Test
    fun validacionMonto_numerosDanVerdadero() {
        val inputUsuario = "5000"
        val esValido = WalletUtils.esMontoValido(inputUsuario)

        assertTrue(esValido)
    }

    // Verificar detección de saldo negativo
    @Test
    fun detectarSaldoNegativo_funciona() {
        val saldo = -100.0
        assertTrue(WalletUtils.esSaldoNegativo(saldo))
    }
}