package com.sigittuning.tuninggarage.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigittuning.tuninggarage.data.models.CreateOrderRequest
import com.sigittuning.tuninggarage.data.models.OrderDto
import com.sigittuning.tuninggarage.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OrdersState {
    object Loading : OrdersState()
    data class Success(val orders: List<OrderDto>) : OrdersState()
    data class Empty(val message: String) : OrdersState()
    data class Error(val message: String) : OrdersState()
}

sealed class CreateOrderState {
    object Idle : CreateOrderState()
    object Loading : CreateOrderState()
    data class Success(val order: OrderDto) : CreateOrderState()
    data class Error(val message: String) : CreateOrderState()
}

class OrdersViewModel : ViewModel() {

    private val _ordersState = MutableStateFlow<OrdersState>(OrdersState.Loading)
    val ordersState: StateFlow<OrdersState> = _ordersState.asStateFlow()

    private val _createOrderState = MutableStateFlow<CreateOrderState>(CreateOrderState.Idle)
    val createOrderState: StateFlow<CreateOrderState> = _createOrderState.asStateFlow()

    // Cargar mis pedidos
    fun loadMyOrders(context: Context) {
        viewModelScope.launch {
            try {
                _ordersState.value = OrdersState.Loading

                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.getMyOrders()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        if (apiResponse.data.isEmpty()) {
                            _ordersState.value = OrdersState.Empty("No tienes pedidos aún")
                        } else {
                            _ordersState.value = OrdersState.Success(apiResponse.data)
                        }
                    } else {
                        _ordersState.value = OrdersState.Error(apiResponse.message)
                    }
                } else {
                    _ordersState.value = OrdersState.Error("Error al cargar pedidos")
                }
            } catch (e: Exception) {
                _ordersState.value = OrdersState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // Crear pedido
    fun createOrder(
        context: Context,
        direccion: String,
        ciudad: String,
        estado: String,
        codigoPostal: String,
        telefono: String,
        metodoPago: String
    ) {
        viewModelScope.launch {
            try {
                _createOrderState.value = CreateOrderState.Loading

                // Validaciones
                if (direccion.isBlank() || telefono.isBlank()) {
                    _createOrderState.value = CreateOrderState.Error("Completa todos los campos obligatorios")
                    return@launch
                }

                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.createOrder(
                    CreateOrderRequest(
                        direccionEnvio = direccion,
                        ciudad = ciudad.ifBlank { null },
                        estado = estado.ifBlank { null },
                        codigoPostal = codigoPostal.ifBlank { null },
                        telefonoContacto = telefono,
                        metodoPago = metodoPago.ifBlank { null }
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        _createOrderState.value = CreateOrderState.Success(apiResponse.data)
                        // Recargar la lista de pedidos
                        loadMyOrders(context)
                    } else {
                        _createOrderState.value = CreateOrderState.Error(apiResponse.message)
                    }
                } else {
                    _createOrderState.value = CreateOrderState.Error("Error al crear el pedido")
                }
            } catch (e: Exception) {
                _createOrderState.value = CreateOrderState.Error("Error: ${e.message}")
            }
        }
    }

    fun resetCreateOrderState() {
        _createOrderState.value = CreateOrderState.Idle
    }
}