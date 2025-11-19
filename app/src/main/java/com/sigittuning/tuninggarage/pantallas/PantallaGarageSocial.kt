package com.sigittuning.tuninggarage.pantallas

// NUEVO: Imports necesarios para el selector de fotos y manejo de URI
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
// FIN NUEVOS IMPORTS

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState // NUEVO
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll // NUEVO
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
import androidx.navigation.NavController // <- IMPORTACIÓN AGREGADA
import coil.compose.AsyncImage
import com.sigittuning.tuninggarage.navegacion.Rutas
// Importa tu clase Rutas (asegúrate de que la ruta sea correcta)
// import com.sigittuning.tuninggarage.nav.Rutas
import com.sigittuning.tuninggarage.viewmodels.SocialPostDto
import com.sigittuning.tuninggarage.viewmodels.SocialPostsState
import com.sigittuning.tuninggarage.viewmodels.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGarageSocial(
    navController: NavController, // AGREGADO
    viewModel: SocialViewModel = viewModel()
) {
    val context = LocalContext.current
    val postsState by viewModel.postsState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // NUEVO: Estado para guardar la URI de la imagen seleccionada
    var imagenUriSeleccionada by remember { mutableStateOf<Uri?>(null) }

    // NUEVO: Launcher para el selector de fotos
    // Este launcher abre la galería del dispositivo
    val selectorImagenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // Cuando el usuario selecciona una imagen, su URI se guarda en el estado
        imagenUriSeleccionada = uri
    }

    LaunchedEffect(Unit) {
        viewModel.loadPosts(context)
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        // ... (El TopAppBar, FAB y SnackbarHost no cambian) ...
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GARAGE SOCIAL",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPosts(context) }) {
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
                Icon(Icons.Default.Add, contentDescription = "Crear publicación", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        // ... (El when(postsState) no cambia) ...
        when (val state = postsState) {
            is SocialPostsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando publicaciones...")
                    }
                }
            }

            is SocialPostsState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.posts) { post ->
                        // LLAMADA MODIFICADA
                        TarjetaPublicacionReal(
                            publicacion = post,
                            viewModel = viewModel,
                            context = context,
                            onNavigateToComments = {
                                // Asegúrate de tener 'Rutas' importado
                                // (he comentado la importación arriba como ejemplo)
                                navController.navigate(Rutas.Comentarios.crearRuta(post.postID))
                            }
                        )
                    }
                }
            }

            is SocialPostsState.Error -> {
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
                            "Error al cargar publicaciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPosts(context) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }

    // Diálogo para crear publicación (SIN CAMBIOS)
    if (showCreateDialog) {
        var titulo by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        // ELIMINADO: var imagenURL by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                imagenUriSeleccionada = null // NUEVO: Limpiar la URI si se cierra
            },
            title = { Text("Nueva Publicación") },
            text = {
                // NUEVO: Se agrega verticalScroll por si la vista previa hace el diálogo muy alto
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título (opcional)") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("¿Qué está pasando con tu auto?") },
                        minLines = 3,
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // ELIMINADO: El OutlinedTextField para 'imagenURL'

                    // NUEVO: Botón para abrir el selector de fotos
                    Button(onClick = {
                        // Lanza el selector de imágenes
                        selectorImagenLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Icon(Icons.Default.Image, contentDescription = "Seleccionar Imagen")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seleccionar Imagen")
                    }

                    // NUEVO: Vista previa de la imagen seleccionada
                    imagenUriSeleccionada?.let { uri ->
                        Spacer(modifier = Modifier.height(16.dp))
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (descripcion.isNotBlank()) {
                            // MODIFICADO: Ahora pasamos la URI (Uri?) en lugar de la URL (String?)
                            viewModel.createPost(
                                context,
                                titulo.ifBlank { null },
                                descripcion,
                                imagenUriSeleccionada // <-- Objeto Uri, no un String
                            )
                            showCreateDialog = false
                            imagenUriSeleccionada = null // NUEVO: Limpiar la URI después de publicar
                        }
                    },
                    enabled = descripcion.isNotBlank()
                ) {
                    Text("Publicar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    imagenUriSeleccionada = null // NUEVO: Limpiar la URI si se cancela
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ... (La TarjetaPublicacionReal no necesita cambios) ...
@Composable
fun TarjetaPublicacionReal(
    publicacion: SocialPostDto,
    viewModel: SocialViewModel,
    context: android.content.Context,
    onNavigateToComments: () -> Unit // NUEVO PARÁMETRO
) {
    var showCommentDialog by remember { mutableStateOf(false) }
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
            // Header con usuario
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                AsyncImage(
                    model = publicacion.usuarioAvatar ?: "https://i.pravatar.cc/150?img=${publicacion.userID}",
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = publicacion.usuarioNombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = publicacion.tiempoTranscurrido,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Título (si existe)
            if (!publicacion.titulo.isNullOrBlank()) {
                Text(
                    text = publicacion.titulo,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Descripción
            if (!publicacion.descripcion.isNullOrBlank()) {
                Text(
                    text = publicacion.descripcion,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Imagen
            if (!publicacion.imagenURL.isNullOrBlank()) {
                AsyncImage(
                    model = publicacion.imagenURL,
                    contentDescription = "Imagen del post",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Botones de interacción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Like
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.toggleLike(context, publicacion.postID)
                    }
                ) {
                    Icon(
                        imageVector = if (publicacion.usuarioLeDioLike) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (publicacion.usuarioLeDioLike) Color.Red
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${publicacion.totalLikes}")
                }

                // Comentarios (MODIFICADO)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        onNavigateToComments() // CAMBIO: En lugar de showCommentDialog = true
                    }
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comentarios",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${publicacion.totalComentarios}")
                }

                // Compartir
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // Diálogo de comentarios (Se mantiene, como pediste)
    // Nota: Este diálogo ahora no se puede abrir porque el botón que lo abría
    // ahora navega a otra pantalla.
    if (showCommentDialog) {
        var comentario by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCommentDialog = false },
            title = { Text("Agregar comentario") },
            text = {
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Tu comentario") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (comentario.isNotBlank()) {
                            viewModel.addComment(context, publicacion.postID, comentario)
                            showCommentDialog = false
                        }
                    },
                    enabled = comentario.isNotBlank()
                ) {
                    Text("Comentar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommentDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}