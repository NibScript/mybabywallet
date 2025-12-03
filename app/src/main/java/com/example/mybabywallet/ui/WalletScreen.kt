package com.example.mybabywallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

// ESTOS SON LOS QUE SUELEN FALTAR:
import com.example.mybabywallet.data.Transaccion
import com.example.mybabywallet.ui.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(viewModel: WalletViewModel = viewModel()) {
    // Observamos los datos de la base de datos en tiempo real
    val listaTransacciones by viewModel.listaTransacciones.observeAsState(initial = emptyList())
    val totalIngresos by viewModel.totalIngresos.observeAsState(initial = 0.0)
    val totalGastos by viewModel.totalGastos.observeAsState(initial = 0.0)

    // Calcular saldo
    val saldo = (totalIngresos ?: 0.0) - (totalGastos ?: 0.0)

    // Estado para mostrar el cuadro de diálogo de "Agregar"
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 1. TARJETA DE SALDO
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Saldo Disponible", fontSize = 18.sp)
                    Text(
                        text = "$ ${String.format("%.0f", saldo)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (saldo >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            // 2. LISTA DE MOVIMIENTOS
            Text("Últimos movimientos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (listaTransacciones.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay movimientos aún", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(listaTransacciones) { transaccion ->
                        ItemTransaccion(transaccion, onDelete = { viewModel.eliminarTransaccion(it) })
                    }
                }
            }
        }
    }

    // El formulario emergente
    if (mostrarDialogo) {
        DialogoAgregarTransaccion(
            onDismiss = { mostrarDialogo = false },
            onConfirm = { titulo, monto, esIngreso ->
                viewModel.agregarTransaccion(titulo, monto, esIngreso)
                mostrarDialogo = false
            }
        )
    }
}

// Componente para dibujar cada fila de la lista
@Composable
fun ItemTransaccion(transaccion: Transaccion, onDelete: (Transaccion) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(transaccion.titulo, fontWeight = FontWeight.Bold)
                val fechaFormato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(transaccion.fecha))
                Text(fechaFormato, fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (if (transaccion.tipo == "INGRESO") "+ " else "- ") + "$ ${transaccion.monto.toInt()}",
                    color = if (transaccion.tipo == "INGRESO") Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onDelete(transaccion) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Gray)
                }
            }
        }
    }
}

// Componente para el Formulario (Dialog)
@Composable
fun DialogoAgregarTransaccion(onDismiss: () -> Unit, onConfirm: (String, String, Boolean) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var esIngreso by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Movimiento") },
        text = {
            Column {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto") })
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = esIngreso, onCheckedChange = { esIngreso = it })
                    Text("Es un Ingreso (Dinero a favor)")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(titulo, monto, esIngreso) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}