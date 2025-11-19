package com.sigittuning.tuninggarage.pantallas

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.sigittuning.tuninggarage.viewmodels.AuthViewModel
import com.sigittuning.tuninggarage.utils.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var imagenUriSeleccionada by remember { mutableStateOf<Uri?>(null) }

    val selectorImagenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        imagenUriSeleccionada = uri
    }

    // Cargar datos del usuario
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            nombre = tokenManager.userName.first() ?: ""
            email = tokenManager.userEmail.first() ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MI PERFIL",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
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
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar / Foto de perfil
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = imagenUriSeleccionada ?: avatarUrl ?: "https://i.pravatar.cc/300",
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )

                FloatingActionButton(
                    onClick = {
                        selectorImagenLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.size(36.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Cambiar foto",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                nombre.ifBlank { "Usuario" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Información personal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "INFORMACIÓN PERSONAL",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = false
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* TODO: Guardar cambios */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GUARDAR CAMBIOS")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Opciones adicionales
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Cambiar contraseña") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    ListItem(
                        headlineContent = { Text("Mis direcciones") },
                        leadingContent = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    ListItem(
                        headlineContent = { Text("Notificaciones") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de cerrar sesión
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            "Cerrar sesión",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    tonalElevation = 0.dp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Diálogo de confirmación de cierre de sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, contentDescription = null) },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logout(context)
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}