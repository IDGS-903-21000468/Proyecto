package com.sigittuning.tuninggarage.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class Producto(
    val nombre: String,
    val precio: String,
    val imagenUrl: String,
    val categoria: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBuscarProductos() {
    var textoBusqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todo") }

    val categorias = listOf("Todo", "Llantas", "Suspensión", "Aerodinámica", "Frenos", "Escape")

    val todosLosProductos = listOf(
        Producto(
            "Juego Rines Rotiform BLQ 19\"",
            "$38,500.00 MXN",
            "https://www.llanterarush.mx/web/image/product.template/5023/image_1024?unique=64977aa",
            "Llantas"
        ),
        Producto(
            "Juego Rines BBS CH-R 18\"",
            "$45,200.00 MXN",
            "https://www.llanterarush.mx/web/image/product.template/8038/image_1024?unique=60df670",
            "Llantas"
        ),
        Producto(
            "Juego Rines Work Emotion CR",
            "$33,990.00 MXN",
            "https://workwheelsmx.com/wp-content/uploads/2018/12/CR_KIWAMI-compressor-compressor.png",
            "Llantas"
        ),
        Producto(
            "Juego Rines Vossen HF-3 20\"",
            "$51,000.00 MXN",
            "https://static.felgenshop.de/vossen-hf3-deep-face-llanta-schwarz-llanta-de-invierno-llanta-de-aluminio-11-500x500.jpg",
            "Llantas"
        ),

        // SUSPENSIÓN
        Producto(
            "Coilovers Fortune Auto 500",
            "$31,800.00 MXN",
            "https://shop-fortune-auto.com/cdn/shop/products/500Series11200SQV_bfa7f112-43eb-4939-9028-ab130f2bee96.png?v=1673035452&width=1214",
            "Suspensión"
        ),
        Producto(
            "Coilovers BC Racing BR Series",
            "$24,500.00 MXN",
            "https://cdn.shopify.com/s/files/1/0036/6863/9856/products/bc-racing-default-br-series.jpg?v=1571253987",
            "Suspensión"
        ),
        Producto(
            "Coilovers KW V3",
            "$55,900.00 MXN",
            "https://www.kwsuspensions.com/media/catalog/product/k/w/kw_35250014_640_9.jpg",
            "Suspensión"
        ),
        Producto(
            "Coilovers Tein Flex Z",
            "$22,990.00 MXN",
            "https://boostzonemexico.com/cdn/shop/files/61p9erN1bPL._AC_SL1500_966x700.jpg?v=1697593103",
            "Suspensión"
        ),

        Producto(
            "Spoiler APR Fibra de Carbono GTC-300",
            "$18,500.00 MXN",
            "https://i.ebayimg.com/images/g/DJEAAMXQxKxR-hSt/s-l400.jpg",
            "Aerodinámica"
        ),
        Producto(
            "Cofre Seibon Carbono (Estilo OEM)",
            "$16,200.00 MXN",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS_HIJNf9QzJSbwyNdDoKDX_kHNdiZ8XU0z8w&s",
            "Aerodinámica"
        ),
        Producto(
            "Wide Body Kit Liberty Walk",
            "$180,000.00 MXN",
            "https://encrypted-tbn1.gstatic.com/shopping?q=tbn:ANd9GcTmUOPk6E7oSU-aZD5um8VbR9jcdTbDYemr09GiGFVUv5nVVkM-eB7iUpc3ykD9dI3oUo0M6LiR8YkI6Jd4lTi4hv8BrHrgBXkbMOxYXcZQdRrRtimekBU",
            "Aerodinámica"
        ),
        Producto(
            "Splitter Frontal Carbono Universal",
            "$4,800.00 MXN",
            "https://http2.mlstatic.com/D_NQ_NP_865167-MLM52182695517_102022-O-lip-fibra-carbono-splitter-universal-plastico-delantero-3pzs.webp",
            "Aerodinámica"
        ),

        Producto(
            "Big Brake Kit Brembo GT 6-Piston",
            "$78,000.00 MXN",
            "https://store.activeautowerke.com/cdn/shop/products/Large328_9d9bc2aa-431c-4161-9d3e-dde69c03f648.jpg?v=1409325866&width=600",
            "Frenos"
        ),
        Producto(
            "Big Brake Kit StopTech ST-40",
            "$46,500.00 MXN",
            "https://cdn11.bigcommerce.com/s-q5z3jegn8k/images/stencil/2048x2048/products/2173/1769/stoptech-bbk-328-332-st40-red-non-z-audi-vw-1280x800__65784.1527289689.jpg?c=2",
            "Frenos"
        ),
        Producto(
            "Kit de Frenos AP Racing",
            "$85,000.00 MXN",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ15uChEeOiPSkXcZKenFoyMobyjFmqzKQXqw&s",
            "Frenos"
        ),
        Producto(
            "Kit de Frenos Wilwood Superlite 6R",
            "$41,000.00 MXN",
            "https://m.media-amazon.com/images/I/61BaYiVTEPL.UF894,1000_QL80.jpg",
            "Frenos"
        ),

        Producto(
            "Cat-Back Borla S-Type",
            "$26,800.00 MXN",
            "https://m.media-amazon.com/images/I/51rna5DIZsL.AC_UF894,1000_QL80.jpg",
            "Escape"
        ),
        Producto(
            "Escape Akrapovic Titanio (Slip-On)",
            "$52,000.00 MXN",
            "https://www.geosdesign.mx/wp-content/uploads/2024/03/AKRAPOVIC-SLIP-ON-RACING-MUFFLER-SLIP-ON.jpg",
            "Escape"
        ),
        Producto(
            "Injen Cold Air Intake",
            "$8,500.00 MXN",
            "https://injen.com/images/F198656888.jpg",
            "Escape"
        ),
        Producto(
            "Sistema de Escape Milltek Sport",
            "$33,000.00 MXN",
            "https://www.tunerhouse.com/wp-content/uploads/2024/07/escapes-milltek-sport-03.webp",
            "Escape"
        )
    )
    val productosFiltrados = remember(categoriaSeleccionada, textoBusqueda) {
        todosLosProductos.filter { producto ->
            val coincideCategoria = categoriaSeleccionada == "Todo" ||
                    producto.categoria == categoriaSeleccionada

            val coincideBusqueda = textoBusqueda.isEmpty() ||
                    producto.nombre.contains(textoBusqueda, ignoreCase = true)

            coincideCategoria && coincideBusqueda
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "BUSCAR PRODUCTOS",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por pieza, marca, modelo...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (textoBusqueda.isNotEmpty()) {
                            IconButton(onClick = { textoBusqueda = "" }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Limpiar")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                ScrollableTabRow(
                    selectedTabIndex = categorias.indexOf(categoriaSeleccionada),
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    categorias.forEach { categoria ->
                        Tab(
                            selected = categoriaSeleccionada == categoria,
                            onClick = { categoriaSeleccionada = categoria },
                            text = {
                                Text(
                                    categoria,
                                    fontWeight = if (categoriaSeleccionada == categoria)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        if (productosFiltrados.isNotEmpty()) {
            Text(
                text = "${productosFiltrados.size} productos encontrados",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (productosFiltrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No se encontraron productos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productosFiltrados) { producto ->
                    TarjetaProducto(producto)
                }
            }
        }
    }
}

@Composable
fun TarjetaProducto(producto: Producto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable { /*  navegar a detalle  */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = producto.imagenUrl,
                contentDescription = producto.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = producto.categoria,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = producto.precio,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}