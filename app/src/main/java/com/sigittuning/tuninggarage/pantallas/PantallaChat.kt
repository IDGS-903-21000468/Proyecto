package com.sigittuning.tuninggarage.pantallas

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
import com.sigittuning.tuninggarage.viewmodels.ChatViewModel
import com.sigittuning.tuninggarage.viewmodels.ChatState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaChat(
    listingID: Int,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val chatState by viewModel.chatState.collectAsState()
    var nuevoMensaje by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar chat al iniciar
    LaunchedEffect(listingID) {
        viewModel.initializeChat(context, listingID)
    }

    // Auto-scroll al enviar mensaje
    LaunchedEffect(chatState) {
        if (chatState is ChatState.Success) {
            val messages = (chatState as ChatState.Success).chat.mensajes
            if (messages.isNotEmpty()) {
                coroutineScope.launch {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = chatState) {
                        is ChatState.Success -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = state.chat.otroUsuarioAvatar ?: "https://i.pravatar.cc/150",
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        state.chat.otroUsuarioNombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        state.chat.listingTitulo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        else -> Text("Chat")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
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
                    OutlinedTextField(
                        value = nuevoMensaje,
                        onValueChange = { nuevoMensaje = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (nuevoMensaje.isNotBlank() && chatState is ChatState.Success) {
                                val chatID = (chatState as ChatState.Success).chat.chatID
                                viewModel.sendMessage(context, chatID, nuevoMensaje)
                                nuevoMensaje = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (nuevoMensaje.isNotBlank())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                        enabled = nuevoMensaje.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = if (nuevoMensaje.isNotBlank())
                                Color.White
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        when (val state = chatState) {
            is ChatState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ChatState.Success -> {
                if (state.chat.mensajes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay mensajes aún",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Envía el primer mensaje",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.chat.mensajes) { mensaje ->
                            BurbujaMensajeChat(mensaje)
                        }
                    }
                }
            }

            is ChatState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.initializeChat(context, listingID) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BurbujaMensajeChat(mensaje: com.sigittuning.tuninggarage.viewmodels.ChatMessageDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mensaje.esPropio) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (mensaje.esPropio)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (mensaje.esPropio) 16.dp else 4.dp,
                bottomEnd = if (mensaje.esPropio) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!mensaje.esPropio) {
                    Text(
                        mensaje.senderNombre,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    mensaje.mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    mensaje.fechaEnvio.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}