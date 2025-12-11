package com.example.mybabywallet.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mybabywallet.data.Transaccion
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    usuarioId: Int,
    viewModel: WalletViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(usuarioId) {
        viewModel.setUsuarioActual(usuarioId)
    }

    val listaTransacciones by viewModel.listaTransacciones.observeAsState(initial = emptyList())
    val totalIngresos by viewModel.totalIngresos.observeAsState(initial = 0.0)
    val totalGastos by viewModel.totalGastos.observeAsState(initial = 0.0)
    val saldo = (totalIngresos ?: 0.0) - (totalGastos ?: 0.0)

    val mensajeSync by viewModel.estadoSincronizacion.observeAsState("")

    LaunchedEffect(mensajeSync) {
        if (mensajeSync.isNotEmpty()) {
            Toast.makeText(context, mensajeSync, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajeSync()
        }
    }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var mostrarConversor by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MyBabyWallet", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // BOTÓN SINCRONIZAR
                    IconButton(onClick = { viewModel.sincronizarConNube() }) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Sincronizar")
                    }

                    // BOTÓN CONVERSOR
                    IconButton(onClick = { mostrarConversor = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Conversor")
                    }

                    // BOTÓN SALIR
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        },
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
            // ANIMACIÓN SALDO
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Saldo Disponible", fontSize = 18.sp)

                    // Animación Color
                    val colorAnimado by animateColorAsState(
                        targetValue = if (saldo >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                        animationSpec = tween(durationMillis = 1000),
                        label = "CambioColor"
                    )

                    // Animación Escala
                    val escalaAnimada by animateFloatAsState(
                        targetValue = if (saldo == 0.0) 1f else 1.1f,
                        animationSpec = tween(durationMillis = 500),
                        label = "Escala"
                    )

                    Text(
                        text = "$ ${String.format("%.0f", saldo)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorAnimado,
                        modifier = Modifier.scale(escalaAnimada)
                    )
                }
            }

            // MOVIMIENTOS
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

    // DIÁLOGOS EMERGENTES
    if (mostrarDialogo) {
        DialogoAgregarTransaccion(
            onDismiss = { mostrarDialogo = false },
            onConfirm = { titulo, monto, esIngreso, rutaFoto, lat, long ->
                viewModel.agregarTransaccion(titulo, monto, esIngreso, rutaFoto, lat, long)
                mostrarDialogo = false
            }
        )
    }

    if (mostrarConversor) {
        DialogoConversor(viewModel = viewModel, onDismiss = { mostrarConversor = false })
    }
}

@Composable
fun ItemTransaccion(transaccion: Transaccion, onDelete: (Transaccion) -> Unit) {
    val context = LocalContext.current
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
                // FOTO
                if (transaccion.imagenPath.isNotEmpty()) {
                    AsyncImage(
                        model = transaccion.imagenPath,
                        contentDescription = "Foto Boleta",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { mostrarFotoGrande = true },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column {
                    Text(transaccion.titulo, fontWeight = FontWeight.Bold)
                    val fechaFormato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(transaccion.fecha))
                    Text(fechaFormato, fontSize = 12.sp, color = Color.Gray)

                    if (transaccion.latitud != 0.0) {
                        Text("📍 Ver ubicación", fontSize = 10.sp, color = Color(0xFF1565C0))
                    }
                }
            }

            // ACCIONES
            Row(verticalAlignment = Alignment.CenterVertically) {
                // MAPA
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

                // MONTO Y BORRAR
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

    // AGRANDAR FOTO
    if (mostrarFotoGrande) {
        Dialog(onDismissRequest = { mostrarFotoGrande = false }) {
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
                            .background(Color.Black),
                        contentScale = ContentScale.Fit
                    )
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

// DIÁLOGO AGREGAR
@Composable
fun DialogoAgregarTransaccion(onDismiss: () -> Unit, onConfirm: (String, String, Boolean, String, Double, Double) -> Unit) {
    val context = LocalContext.current
    var titulo by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var esIngreso by remember { mutableStateOf(false) }
    var fotoPathActual by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var ubicacionTexto by remember { mutableStateOf("Sin ubicación") }

    // CÁMARA
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

    // GPS
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val obtenerUbicacion = {
        Toast.makeText(context, "Buscando señal GPS...", Toast.LENGTH_SHORT).show()
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitud = location.latitude
                    longitud = location.longitude
                    ubicacionTexto = "Lat: ${String.format("%.4f", latitud)}, Lon: ${String.format("%.4f", longitud)}"
                    Toast.makeText(context, "¡Ubicación encontrada!", Toast.LENGTH_SHORT).show()
                } else {
                    ubicacionTexto = "GPS activo pero sin memoria reciente"
                    Toast.makeText(context, "Abre Google Maps para calibrar", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: SecurityException) {
            ubicacionTexto = "Error de permisos"
            Toast.makeText(context, "Faltan permisos", Toast.LENGTH_SHORT).show()
        }
    }
    val permisoGPS = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) obtenerUbicacion() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Movimiento") },
        text = {
            Column {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
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

                // BOTÓN GPS
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) obtenerUbicacion()
                        else permisoGPS.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
            Button(onClick = { onConfirm(titulo, monto, esIngreso, fotoPathActual, latitud, longitud) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

fun crearArchivoImagen(context: android.content.Context): java.io.File {
    val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
    val nombreArchivo = "JPEG_${timeStamp}_"
    val directorio = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return java.io.File.createTempFile(nombreArchivo, ".jpg", directorio)
}

// DIÁLOGO CONVERSOR
@Composable
fun DialogoConversor(viewModel: WalletViewModel, onDismiss: () -> Unit) {
    var monto by remember { mutableStateOf("") }
    val resultado by viewModel.resultadoConversion.observeAsState("")
    val cargando by viewModel.cargandoConversion.observeAsState(false)

    AlertDialog(
        onDismissRequest = { viewModel.limpiarConversor(); onDismiss() },
        title = { Text("Conversor de Moneda") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ingresa monto en Pesos (CLP):", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("$ Monto") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                Spacer(modifier = Modifier.height(16.dp))

                if (cargando) CircularProgressIndicator()
                else if (resultado.isNotEmpty()) {
                    Text(text = resultado, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { viewModel.convertirMoneda(monto.toDoubleOrNull() ?: 0.0, "DOLAR") }) { Text("A Dólar") }
                    Button(onClick = { viewModel.convertirMoneda(monto.toDoubleOrNull() ?: 0.0, "UF") }) { Text("A UF") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.limpiarConversor(); onDismiss() }) { Text("Cerrar") }
        }
    )
}