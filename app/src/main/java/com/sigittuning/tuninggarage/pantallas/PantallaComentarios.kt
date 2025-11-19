package com.sigittuning.tuninggarage.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sigittuning.tuninggarage.viewmodels.CommentDto
import com.sigittuning.tuninggarage.viewmodels.SocialPostDto
import com.sigittuning.tuninggarage.viewmodels.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaComentarios(
    postID: Int,
    onBack: () -> Unit,
    viewModel: SocialViewModel = viewModel()
) {
    val context = LocalContext.current
    val commentsState by viewModel.commentsState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    var nuevoComentario by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar comentarios al iniciar
    LaunchedEffect(postID) {
        viewModel.loadComments(context, postID)
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comentarios") },
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
                        value = nuevoComentario,
                        onValueChange = { nuevoComentario = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un comentario...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (nuevoComentario.isNotBlank()) {
                                viewModel.addComment(context, postID, nuevoComentario)
                                nuevoComentario = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (nuevoComentario.isNotBlank())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                        enabled = nuevoComentario.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = if (nuevoComentario.isNotBlank())
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        when (val state = commentsState) {
            is SocialViewModel.CommentsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is SocialViewModel.CommentsState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
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
                                        "No hay comentarios aún",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "¡Sé el primero en comentar!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.comments) { comentario ->
                            TarjetaComentario(comentario)
                        }
                    }
                }
            }

            is SocialViewModel.CommentsState.Error -> {
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
                        Button(onClick = { viewModel.loadComments(context, postID) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaComentario(comentario: CommentDto) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = comentario.usuarioAvatar ?: "https://i.pravatar.cc/150?img=${comentario.userID}",
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        comentario.usuarioNombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        comentario.textoComentario,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                comentario.tiempoTranscurrido,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}