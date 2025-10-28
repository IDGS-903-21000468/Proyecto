package com.sigittuning.tuninggarage.pantallas

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sigittuning.tuninggarage.viewmodels.AsistenteViewModel
import kotlinx.coroutines.launch

data class Mensaje(
    val texto: String,
    val esUsuario: Boolean,
    val imagenUri: Uri? = null,
    val esError: Boolean = false,
    val esCargando: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAsistenteAVT(
    viewModel: AsistenteViewModel = viewModel()
) {
    val mensajes by viewModel.mensajes.collectAsState()
    val estaCargando by viewModel.estaCargando.collectAsState()
    var textoMensaje by remember { mutableStateOf("") }
    var imagenSeleccionada by remember { mutableStateOf<Uri?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    val context = LocalContext.current

    val seleccionadorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenSeleccionada = uri
    }

    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(mensajes.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "AVT",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "ASISTENTE AVT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (estaCargando) {
                                Text(
                                    "Escribiendo...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.limpiarChat() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Limpiar chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Lista de mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mensajes) { mensaje ->
                    BurbujaMensaje(mensaje)
                }
            }

            // Vista previa de imagen seleccionada
            if (imagenSeleccionada != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = imagenSeleccionada,
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Imagen adjunta",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        IconButton(onClick = { imagenSeleccionada = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar imagen")
                        }
                    }
                }
            }

            // Barra de entrada
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { seleccionadorImagen.launch("image/*") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Adjuntar imagen",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = textoMensaje,
                        onValueChange = { textoMensaje = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textoMensaje.isNotBlank() || imagenSeleccionada != null) {

                                // --- ¡CAMBIO REALIZADO AQUÍ! ---
                                // Pasamos el 'context' que definimos arriba
                                viewModel.enviarMensaje(
                                    textoMensaje,
                                    imagenSeleccionada,
                                    context
                                )

                                textoMensaje = ""
                                imagenSeleccionada = null
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (textoMensaje.isNotBlank() || imagenSeleccionada != null)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                        enabled = textoMensaje.isNotBlank() || imagenSeleccionada != null
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = if (textoMensaje.isNotBlank() || imagenSeleccionada != null)
                                Color.White
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BurbujaMensaje(mensaje: Mensaje) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mensaje.esUsuario) Arrangement.End else Arrangement.Start
    ) {
        if (!mensaje.esUsuario) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "AVT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            color = when {
                mensaje.esError -> MaterialTheme.colorScheme.errorContainer
                mensaje.esCargando -> MaterialTheme.colorScheme.surfaceVariant
                mensaje.esUsuario -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (mensaje.esUsuario) 16.dp else 4.dp,
                bottomEnd = if (mensaje.esUsuario) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (mensaje.imagenUri != null) {
                    AsyncImage(
                        model = mensaje.imagenUri,
                        contentDescription = "Imagen adjunta",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (mensaje.texto.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (mensaje.esCargando) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                } else if (mensaje.texto.isNotBlank()) {
                    Text(
                        text = mensaje.texto,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (mensaje.esError)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}