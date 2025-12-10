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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.compose.material.icons.filled.Check
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.widget.Toast
import androidx.compose.material.icons.filled.LocationOn
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog


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
            onConfirm = { titulo, monto, esIngreso, rutaFoto, lat, long -> // Nuevos parámetros
                // Pasamos todo al ViewModel
                viewModel.agregarTransaccion(titulo, monto, esIngreso, rutaFoto, lat, long)
                mostrarDialogo = false
            }
        )
    }
}

// Componente para dibujar cada fila de la lista
@Composable
fun ItemTransaccion(transaccion: Transaccion, onDelete: (Transaccion) -> Unit) {
    val context = LocalContext.current

    // Estado para controlar si el popup de la foto está abierto
    var mostrarFotoGrande by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 1. FOTO PEQUEÑA (Clickable)
                if (transaccion.imagenPath.isNotEmpty()) {
                    AsyncImage(
                        model = transaccion.imagenPath,
                        contentDescription = "Foto Boleta",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            // AQUÍ ESTÁ EL TRUCO: Al hacer click, activamos el popup
                            .clickable { mostrarFotoGrande = true },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // 2. DATOS
                Column {
                    Text(transaccion.titulo, fontWeight = FontWeight.Bold)
                    val fechaFormato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(transaccion.fecha))
                    Text(fechaFormato, fontSize = 12.sp, color = Color.Gray)

                    if (transaccion.latitud != 0.0) {
                        Text("📍 Ver ubicación", fontSize = 10.sp, color = Color(0xFF1565C0))
                    }
                }
            }

            // 3. ACCIONES
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Botón MAPA
                if (transaccion.latitud != 0.0) {
                    IconButton(onClick = {
                        val uri = Uri.parse("geo:${transaccion.latitud},${transaccion.longitud}?q=${transaccion.latitud},${transaccion.longitud}(${transaccion.titulo})")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")
                        if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                        else context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Ver Mapa", tint = Color(0xFF1565C0))
                    }
                }

                // Monto y Borrar
                Column(horizontalAlignment = Alignment.End) {
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

    // --- CÓDIGO DEL POPUP (Zoom de Imagen) ---
    if (mostrarFotoGrande) {
        Dialog(onDismissRequest = { mostrarFotoGrande = false }) {
            // Una tarjeta simple para contener la foto grande
            Card(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = transaccion.imagenPath,
                        contentDescription = "Foto Grande",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color.Black), // Fondo negro para que resalte
                        contentScale = ContentScale.Fit // Fit para ver la foto completa sin recortes
                    )
                    // Botón para cerrar (opcional, ya que tocando fuera se cierra)
                    Button(
                        onClick = { mostrarFotoGrande = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}
// Componente para el Formulario (Dialog)
@Composable
fun DialogoAgregarTransaccion(onDismiss: () -> Unit, onConfirm: (String, String, Boolean, String, Double, Double) -> Unit) {
    val context = LocalContext.current
    var titulo by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var esIngreso by remember { mutableStateOf(false) }

    // Variables Foto
    var fotoPathActual by remember { mutableStateOf("") }

    // Variables GPS
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var ubicacionTexto by remember { mutableStateOf("Sin ubicación") }

    // Lógica Cámara (Igual que antes)
    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (!exito) fotoPathActual = ""
    }
    val abrirCamara = {
        val archivo = crearArchivoImagen(context)
        fotoPathActual = archivo.absolutePath
        val uri = FileProvider.getUriForFile(context, "com.example.mybabywallet.provider", archivo)
        launcherCamara.launch(uri)
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) abrirCamara() }

    // Lógica GPS (NUEVO)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Asegúrate de tener este import arriba: import android.widget.Toast

    val obtenerUbicacion = {
        Toast.makeText(context, "Buscando señal GPS...", Toast.LENGTH_SHORT).show() // Feedback visual

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitud = location.latitude
                    longitud = location.longitude
                    ubicacionTexto = "Lat: ${String.format("%.4f", latitud)}, Lon: ${String.format("%.4f", longitud)}"
                    Toast.makeText(context, "¡Ubicación encontrada!", Toast.LENGTH_SHORT).show()
                } else {
                    ubicacionTexto = "GPS activo pero sin memoria reciente"
                    Toast.makeText(context, "Abre Google Maps para calibrar el GPS", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: SecurityException) {
            ubicacionTexto = "Error de permisos"
            Toast.makeText(context, "Faltan permisos", Toast.LENGTH_SHORT).show()
        }
    }

    val permisoGPS = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) obtenerUbicacion() else ubicacionTexto = "Permiso denegado"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Movimiento") },
        text = {
            Column {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto") })

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = esIngreso, onCheckedChange = { esIngreso = it })
                    Text("Es un Ingreso")
                }
                Spacer(modifier = Modifier.height(8.dp))

                // BOTÓN CÁMARA
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) abrirCamara()
                        else permisoCamara.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (fotoPathActual.isNotEmpty()) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary)
                ) {
                    Icon(if (fotoPathActual.isNotEmpty()) Icons.Default.Check else Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (fotoPathActual.isNotEmpty()) "Foto Lista" else "Tomar Foto")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // BOTÓN GPS (NUEVO)
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            obtenerUbicacion()
                        } else {
                            permisoGPS.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (latitud != 0.0) Color(0xFF1565C0) else Color.Gray)
                ) {
                    Icon(Icons.Default.LocationOn, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (latitud != 0.0) "Ubicación Guardada" else "Obtener Ubicación GPS")
                }
                if (latitud != 0.0) {
                    Text(ubicacionTexto, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(titulo, monto, esIngreso, fotoPathActual, latitud, longitud) }) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}// Función auxiliar para crear un archivo temporal donde se guardará la foto
fun crearArchivoImagen(context: android.content.Context): java.io.File {
    val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
    val nombreArchivo = "JPEG_${timeStamp}_"
    // Usamos el directorio de imágenes privadas de la app
    val directorio = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return java.io.File.createTempFile(nombreArchivo, ".jpg", directorio)
}