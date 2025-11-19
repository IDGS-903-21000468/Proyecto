package com.sigittuning.tuninggarage.pantallas

// NUEVO: Imports para el selector de fotos y manejo de URI
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
// FIN NUEVOS IMPORTS

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController // <- IMPORTACIÓN AGREGADA
import coil.compose.AsyncImage
import com.sigittuning.tuninggarage.navegacion.Rutas
// Importa tu clase Rutas (asegúrate de que la ruta sea correcta)
// import com.sigittuning.tuninggarage.nav.Rutas
import com.sigittuning.tuninggarage.viewmodels.MarketplaceListingDto
import com.sigittuning.tuninggarage.viewmodels.MarketplaceState
import com.sigittuning.tuninggarage.viewmodels.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMarketplace(
    navController: NavController, // AGREGADO
    viewModel: MarketplaceViewModel = viewModel()
) {
    val context = LocalContext.current
    val marketplaceState by viewModel.marketplaceState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // NUEVO: Estado para guardar la URI de la imagen seleccionada
    var imagenUriSeleccionada by remember { mutableStateOf<Uri?>(null) }

    // NUEVO: Launcher para el selector de fotos
    val selectorImagenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        imagenUriSeleccionada = uri
    }

    LaunchedEffect(Unit) {
        viewModel.loadListings(context)
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
        }
    }

    Scaffold(
        // ... (TopAppBar y FAB no cambian) ...
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MARKETPLACE",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadListings(context) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Vender auto", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        // ... (El when(marketplaceState) no cambia) ...
        when (val state = marketplaceState) {
            is MarketplaceState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando vehículos...")
                    }
                }
            }

            is MarketplaceState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay vehículos publicados",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sé el primero en vender tu auto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is MarketplaceState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.listings) { listing ->
                        // LLAMADA MODIFICADA
                        TarjetaVehiculoReal(
                            listing = listing,
                            viewModel = viewModel,
                            context = context,
                            onNavigateToChat = {
                                // Asegúrate de tener 'Rutas' importado
                                navController.navigate(Rutas.Chat.crearRuta(listing.listingID))
                            }
                        )
                    }
                }
            }

            is MarketplaceState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Error al cargar marketplace",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadListings(context) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }

    // Diálogo para crear listing (SIN CAMBIOS)
    if (showCreateDialog) {
        var titulo by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        // ELIMINADO: var imagenURL by remember { mutableStateOf("") }
        var precio by remember { mutableStateOf("") }
        var marca by remember { mutableStateOf("") }
        var modelo by remember { mutableStateOf("") }
        var anio by remember { mutableStateOf("") }
        var kilometraje by remember { mutableStateOf("") }
        var modificaciones by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                imagenUriSeleccionada = null // NUEVO: Limpiar URI al cerrar
            },
            title = { Text("Vender mi Auto") },
            text = {
                LazyColumn( // Ya estabas usando un LazyColumn, ¡perfecto!
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ... (item para Titulo, Descripcion, Precio, Marca, Modelo, Anio, Kilometraje, Modificaciones no cambian) ...
                    item {
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = { Text("Título *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción *") },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = precio,
                            onValueChange = { precio = it },
                            label = { Text("Precio (USD) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = marca,
                            onValueChange = { marca = it },
                            label = { Text("Marca") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = modelo,
                            onValueChange = { modelo = it },
                            label = { Text("Modelo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = anio,
                            onValueChange = { anio = it },
                            label = { Text("Año") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = kilometraje,
                            onValueChange = { kilometraje = it },
                            label = { Text("Kilometraje") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = modificaciones,
                            onValueChange = { modificaciones = it },
                            label = { Text("Modificaciones") },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ELIMINADO: item { OutlinedTextField(value = imagenURL, ...) }

                    // NUEVO: Botón para seleccionar imagen
                    item {
                        Button(
                            onClick = {
                                selectorImagenLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Seleccionar Imagen")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar Imagen *")
                        }
                    }

                    // NUEVO: Vista previa de la imagen
                    item {
                        imagenUriSeleccionada?.let { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Vista previa de la imagen",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    item {
                        Text(
                            "* Se aplicará una comisión del 15% al vender",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // MODIFICADO: Añadida la comprobación de la imagen
                        if (titulo.isNotBlank() && descripcion.isNotBlank() && precio.isNotBlank() && imagenUriSeleccionada != null) {
                            viewModel.createListing(
                                context,
                                titulo,
                                descripcion,
                                // MODIFICADO: Pasamos la Uri, no el String
                                imagenUriSeleccionada, // imagenURL.ifBlank { null },
                                precio.toDoubleOrNull() ?: 0.0,
                                marca.ifBlank { null },
                                modelo.ifBlank { null },
                                anio.toIntOrNull(),
                                kilometraje.toIntOrNull(),
                                modificaciones.ifBlank { null }
                            )
                            showCreateDialog = false
                            imagenUriSeleccionada = null // NUEVO: Limpiar URI
                        }
                    },
                    // MODIFICADO: El botón se activa solo si hay imagen
                    enabled = titulo.isNotBlank() && descripcion.isNotBlank() && precio.isNotBlank() && imagenUriSeleccionada != null
                ) {
                    Text("Publicar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    imagenUriSeleccionada = null // NUEVO: Limpiar URI
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// TARJETA MODIFICADA
@Composable
fun TarjetaVehiculoReal(
    listing: MarketplaceListingDto,
    viewModel: MarketplaceViewModel,
    context: android.content.Context,
    onNavigateToChat: () -> Unit // NUEVO PARÁMETRO
) {
    // ... (El código de esta tarjeta no cambia) ...
    var showBidDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Imagen del vehículo
            Box {
                if (!listing.imagenURL.isNullOrBlank()) {
                    AsyncImage(
                        model = listing.imagenURL,
                        contentDescription = listing.titulo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFF3D3D3D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                    }
                }

                // Badge de estatus
                Surface(
                    color = if (listing.estatus == "Activa") Color(0xFF4CAF50) else Color(0xFFFF5252),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = listing.estatus.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Información del vehículo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Vendedor
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = listing.vendedorAvatar ?: "https://i.pravatar.cc/150?img=${listing.vendedorID}",
                        contentDescription = "Avatar vendedor",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        listing.vendedorNombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = listing.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (!listing.marca.isNullOrBlank() || !listing.modelo.isNullOrBlank() || listing.anio != null) {
                    Text(
                        "${listing.marca ?: ""} ${listing.modelo ?: ""} ${listing.anio ?: ""}".trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (listing.kilometraje != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${String.format("%,d", listing.kilometraje)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!listing.descripcion.isNullOrBlank()) {
                    Text(
                        listing.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Precios
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Precio actual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$${String.format("%,.2f", listing.precioActual)} USD",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${listing.totalOfertas} ofertas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (listing.mejorOferta != null && listing.mejorOferta > listing.precioInicial) {
                            Text(
                                "Mejor: $${String.format("%,.2f", listing.mejorOferta)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón de ofertar (MODIFICADO)
                if (listing.estatus == "Activa") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón de chat
                        OutlinedButton(
                            onClick = onNavigateToChat,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CHAT")
                        }

                        // Botón de ofertar (existente)
                        Button(
                            onClick = { showBidDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OFERTAR") // Texto cambiado de "HACER OFERTA"
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp)) // Spacer agregado
                }
            }
        }
    }

    // Diálogo para hacer oferta (SIN CAMBIOS)
    if (showBidDialog) {
        var monto by remember { mutableStateOf("") }
        var mensaje by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBidDialog = false },
            title = { Text("Hacer Oferta") },
            text = {
                Column {
                    Text(
                        "Precio actual: $${String.format("%,.2f", listing.precioActual)} USD",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = monto,
                        onValueChange = { monto = it },
                        label = { Text("Tu oferta (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mensaje,
                        onValueChange = { mensaje = it },
                        label = { Text("Mensaje opcional") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val montoDouble = monto.toDoubleOrNull()
                        if (montoDouble != null && montoDouble > 0) {
                            viewModel.makeBid(
                                context,
                                listing.listingID,
                                montoDouble,
                                mensaje.ifBlank { null }
                            )
                            showBidDialog = false
                        }
                    },
                    enabled = monto.toDoubleOrNull() != null && monto.toDouble() > 0
                ) {
                    Text("Enviar Oferta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBidDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}