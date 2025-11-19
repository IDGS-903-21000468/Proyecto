package com.sigittuning.tuninggarage.data.models

import com.google.gson.annotations.SerializedName

// ===== REQUESTS =====

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("telefono") val telefono: String? = null
)

// ===== RESPONSES =====

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String? = null,
    @SerializedName("usuario") val usuario: UserDto? = null
)

data class UserDto(
    @SerializedName("userID") val userID: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("avatarURL") val avatarURL: String? = null,
    @SerializedName("fechaRegistro") val fechaRegistro: String
)

// ===== PRODUCTOS =====

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T? = null
)

data class CategoryDto(
    @SerializedName("categoryID") val categoryID: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("imagenURL") val imagenURL: String? = null
)

data class ProductDto(
    @SerializedName("productID") val productID: Int,
    @SerializedName("categoryID") val categoryID: Int,
    @SerializedName("categoriaNombre") val categoriaNombre: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("precio") val precio: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("imagenURL") val imagenURL: String? = null,
    @SerializedName("marca") val marca: String? = null,
    @SerializedName("modelo") val modelo: String? = null,
    @SerializedName("anio") val anio: String? = null,
    @SerializedName("disponible") val disponible: Boolean
)

// ===== CARRITO =====

data class AddToCartRequest(
    @SerializedName("productID") val productID: Int,
    @SerializedName("cantidad") val cantidad: Int
)

data class CartDto(
    @SerializedName("items") val items: List<CartItemDto>,
    @SerializedName("total") val total: Double,
    @SerializedName("totalItems") val totalItems: Int
)

data class CartItemDto(
    @SerializedName("cartItemID") val cartItemID: Int,
    @SerializedName("productID") val productID: Int,
    @SerializedName("productoNombre") val productoNombre: String,
    @SerializedName("productoImagen") val productoImagen: String? = null,
    @SerializedName("precioUnitario") val precioUnitario: Double,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("stockDisponible") val stockDisponible: Int
)

// ===== PEDIDOS =====

data class CreateOrderRequest(
    @SerializedName("direccionEnvio") val direccionEnvio: String,
    @SerializedName("ciudad") val ciudad: String? = null,
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("codigoPostal") val codigoPostal: String? = null,
    @SerializedName("telefonoContacto") val telefonoContacto: String,
    @SerializedName("metodoPago") val metodoPago: String? = null
)

data class OrderDto(
    @SerializedName("orderID") val orderID: Int,
    @SerializedName("fechaPedido") val fechaPedido: String,
    @SerializedName("total") val total: Double,
    @SerializedName("estatus") val estatus: String,
    @SerializedName("direccionEnvio") val direccionEnvio: String,
    @SerializedName("ciudad") val ciudad: String? = null,
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("numeroSeguimiento") val numeroSeguimiento: String? = null,
    @SerializedName("detalles") val detalles: List<OrderDetailDto>
)

data class OrderDetailDto(
    @SerializedName("productID") val productID: Int,
    @SerializedName("productoNombre") val productoNombre: String,
    @SerializedName("productoImagen") val productoImagen: String? = null,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precioUnitario") val precioUnitario: Double,
    @SerializedName("subtotal") val subtotal: Double
)

