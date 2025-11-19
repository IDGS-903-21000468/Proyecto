package com.sigittuning.tuninggarage.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigittuning.tuninggarage.data.models.CartDto
import com.sigittuning.tuninggarage.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CartState {
    object Loading : CartState()
    data class Success(val cart: CartDto) : CartState()
    data class Empty(val message: String) : CartState()
    data class Error(val message: String) : CartState()
}

class CartViewModel : ViewModel() {

    private val _cartState = MutableStateFlow<CartState>(CartState.Loading)
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    // Cargar carrito
    fun loadCart(context: Context) {
        viewModelScope.launch {
            try {
                _cartState.value = CartState.Loading

                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.getCart()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        if (apiResponse.data.items.isEmpty()) {
                            _cartState.value = CartState.Empty("Tu carrito está vacío")
                        } else {
                            _cartState.value = CartState.Success(apiResponse.data)
                        }
                    } else {
                        _cartState.value = CartState.Error(apiResponse.message)
                    }
                } else {
                    _cartState.value = CartState.Error("Error al cargar el carrito")
                }
            } catch (e: Exception) {
                _cartState.value = CartState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // Actualizar cantidad de un item
    fun updateCartItem(context: Context, cartItemId: Int, newQuantity: Int) {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.updateCartItem(cartItemId, newQuantity)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _actionMessage.value = "✅ Cantidad actualizada"
                        loadCart(context) // Recargar carrito
                    } else {
                        _actionMessage.value = "❌ ${apiResponse.message}"
                    }
                } else {
                    _actionMessage.value = "❌ Error al actualizar"
                }

                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null

            } catch (e: Exception) {
                _actionMessage.value = "❌ Error: ${e.message}"
                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null
            }
        }
    }

    // Eliminar item del carrito
    fun removeFromCart(context: Context, cartItemId: Int) {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.removeFromCart(cartItemId)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _actionMessage.value = "✅ Producto eliminado"
                        loadCart(context) // Recargar carrito
                    } else {
                        _actionMessage.value = "❌ ${apiResponse.message}"
                    }
                } else {
                    _actionMessage.value = "❌ Error al eliminar"
                }

                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null

            } catch (e: Exception) {
                _actionMessage.value = "❌ Error: ${e.message}"
                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null
            }
        }
    }

    // Vaciar carrito
    fun clearCart(context: Context) {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.clearCart()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _actionMessage.value = "✅ Carrito vaciado"
                        _cartState.value = CartState.Empty("Tu carrito está vacío")
                    } else {
                        _actionMessage.value = "❌ ${apiResponse.message}"
                    }
                } else {
                    _actionMessage.value = "❌ Error al vaciar carrito"
                }

                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null

            } catch (e: Exception) {
                _actionMessage.value = "❌ Error: ${e.message}"
                kotlinx.coroutines.delay(2000)
                _actionMessage.value = null
            }
        }
    }
}