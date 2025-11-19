package com.sigittuning.tuninggarage.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigittuning.tuninggarage.data.models.AddToCartRequest
import com.sigittuning.tuninggarage.data.models.CategoryDto
import com.sigittuning.tuninggarage.data.models.ProductDto
import com.sigittuning.tuninggarage.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductsState {
    object Loading : ProductsState()
    data class Success(val products: List<ProductDto>) : ProductsState()
    data class Error(val message: String) : ProductsState()
}

sealed class CategoriesState {
    object Loading : CategoriesState()
    data class Success(val categories: List<CategoryDto>) : CategoriesState()
    data class Error(val message: String) : CategoriesState()
}

class ProductsViewModel : ViewModel() {

    private val _productsState = MutableStateFlow<ProductsState>(ProductsState.Loading)
    val productsState: StateFlow<ProductsState> = _productsState.asStateFlow()

    private val _categoriesState = MutableStateFlow<CategoriesState>(CategoriesState.Loading)
    val categoriesState: StateFlow<CategoriesState> = _categoriesState.asStateFlow()

    private val _cartMessage = MutableStateFlow<String?>(null)
    val cartMessage: StateFlow<String?> = _cartMessage.asStateFlow()

    // Cargar categorías
    fun loadCategories(context: Context) {
        viewModelScope.launch {
            try {
                _categoriesState.value = CategoriesState.Loading

                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.getCategories()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        _categoriesState.value = CategoriesState.Success(apiResponse.data)
                    } else {
                        _categoriesState.value = CategoriesState.Error(apiResponse.message)
                    }
                } else {
                    _categoriesState.value = CategoriesState.Error("Error al cargar categorías")
                }
            } catch (e: Exception) {
                _categoriesState.value = CategoriesState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // Cargar productos (todos o por categoría)
    fun loadProducts(context: Context, categoryId: Int? = null) {
        viewModelScope.launch {
            try {
                _productsState.value = ProductsState.Loading

                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.getProducts(categoryId)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        _productsState.value = ProductsState.Success(apiResponse.data)
                    } else {
                        _productsState.value = ProductsState.Error(apiResponse.message)
                    }
                } else {
                    _productsState.value = ProductsState.Error("Error al cargar productos")
                }
            } catch (e: Exception) {
                _productsState.value = ProductsState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // Buscar productos
    fun searchProducts(context: Context, query: String) {
        viewModelScope.launch {
            try {
                _productsState.value = ProductsState.Loading

                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.searchProducts(query)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        _productsState.value = ProductsState.Success(apiResponse.data)
                    } else {
                        _productsState.value = ProductsState.Error(apiResponse.message)
                    }
                } else {
                    _productsState.value = ProductsState.Error("Error en la búsqueda")
                }
            } catch (e: Exception) {
                _productsState.value = ProductsState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // Agregar al carrito
    fun addToCart(context: Context, productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(context)
                val response = apiService.addToCart(AddToCartRequest(productId, quantity))

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    _cartMessage.value = if (apiResponse.success) {
                        "✅ Producto agregado al carrito"
                    } else {
                        "❌ ${apiResponse.message}"
                    }
                } else {
                    _cartMessage.value = "❌ Error al agregar al carrito"
                }

                // Limpiar mensaje después de 3 segundos
                kotlinx.coroutines.delay(3000)
                _cartMessage.value = null

            } catch (e: Exception) {
                _cartMessage.value = "❌ Error: ${e.message}"
                kotlinx.coroutines.delay(3000)
                _cartMessage.value = null
            }
        }
    }

    fun clearCartMessage() {
        _cartMessage.value = null
    }
}