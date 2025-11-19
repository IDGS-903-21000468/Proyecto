package com.sigittuning.tuninggarage.network

import com.sigittuning.tuninggarage.data.models.*
// NUEVO: Imports para subida de archivos
import com.sigittuning.tuninggarage.viewmodels.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
// NUEVO: Imports para subida de archivos
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {

    // ===== AUTENTICACIÓN =====

    @POST("Auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // ===== PRODUCTOS =====

    @GET("Products/categories")
    suspend fun getCategories(): Response<ApiResponse<List<CategoryDto>>>

    @GET("Products")
    suspend fun getProducts(@Query("categoryId") categoryId: Int? = null): Response<ApiResponse<List<ProductDto>>>

    @GET("Products/{id}")
    suspend fun getProduct(@Path("id") productId: Int): Response<ApiResponse<ProductDto>>

    @GET("Products/search")
    suspend fun searchProducts(@Query("query") query: String): Response<ApiResponse<List<ProductDto>>>

    // ===== CARRITO =====

    @GET("Cart")
    suspend fun getCart(): Response<ApiResponse<CartDto>>

    @POST("Cart")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<ApiResponse<String>>

    @PUT("Cart/{cartItemId}")
    suspend fun updateCartItem(
        @Path("cartItemId") cartItemId: Int,
        @Body cantidad: Int
    ): Response<ApiResponse<String>>

    @DELETE("Cart/{cartItemId}")
    suspend fun removeFromCart(@Path("cartItemId") cartItemId: Int): Response<ApiResponse<String>>

    @DELETE("Cart/clear")
    suspend fun clearCart(): Response<ApiResponse<String>>

    // ===== PEDIDOS =====

    @GET("Orders")
    suspend fun getMyOrders(): Response<ApiResponse<List<OrderDto>>>

    @GET("Orders/{id}")
    suspend fun getOrder(@Path("id") orderId: Int): Response<ApiResponse<OrderDto>>

    @POST("Orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<OrderDto>>

    // ===== RED SOCIAL =====

    // NUEVO: ENDPOINT PARA SUBIR IMÁGENES
    @Multipart
    @POST("Social/upload") // Esta ruta debe coincidir con tu controlador C#
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<com.sigittuning.tuninggarage.viewmodels.ImageUploadResponse>


    @GET("Social/posts")
    suspend fun getSocialPosts(): Response<ApiResponse<List<com.sigittuning.tuninggarage.viewmodels.SocialPostDto>>>

    @POST("Social/posts")
    suspend fun createSocialPost(@Body request: com.sigittuning.tuninggarage.viewmodels.CreateSocialPostRequest): Response<ApiResponse<com.sigittuning.tuninggarage.viewmodels.SocialPostDto>>

    @POST("Social/posts/{postId}/like")
    suspend fun toggleLike(@Path("postId") postId: Int): Response<ApiResponse<String>>

    @GET("Social/posts/{postId}/comments")
    suspend fun getComments(@Path("postId") postId: Int): Response<ApiResponse<List<com.sigittuning.tuninggarage.viewmodels.CommentDto>>>

    @POST("Social/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Int,
        @Body request: com.sigittuning.tuninggarage.viewmodels.CreateCommentRequest
    ): Response<ApiResponse<com.sigittuning.tuninggarage.viewmodels.CommentDto>>

    // ===== MARKETPLACE =====
    // (Asumiendo que estos DTOs están definidos en otro lugar)

    @GET("Marketplace/listings")
    suspend fun getMarketplaceListings(): Response<ApiResponse<List<com.sigittuning.tuninggarage.viewmodels.MarketplaceListingDto>>>

    @POST("Marketplace/listings")
    suspend fun createMarketplaceListing(@Body request: com.sigittuning.tuninggarage.viewmodels.CreateMarketplaceListingRequest): Response<ApiResponse<com.sigittuning.tuninggarage.viewmodels.MarketplaceListingDto>>

    @POST("Marketplace/listings/{listingId}/bids")
    suspend fun createBid(
        @Path("listingId") listingId: Int,
        @Body request: com.sigittuning.tuninggarage.viewmodels.CreateBidRequest
    ): Response<ApiResponse<com.sigittuning.tuninggarage.viewmodels.BidDto>>


    // AGREGAR estos endpoints a tu ApiService.kt existente:

// ===== CHAT MARKETPLACE =====

    @POST("Marketplace/listings/{listingId}/chat")
    suspend fun initiateChat(@Path("listingId") listingId: Int): Response<ApiResponse<Int>>

    @GET("Marketplace/chats/{chatId}/messages")
    suspend fun getChatMessages(@Path("chatId") chatId: Int): Response<ApiResponse<com.sigittuning.tuninggarage.viewmodels.ChatDto>>

    @POST("Marketplace/chats/{chatId}/messages")
    suspend fun sendChatMessage(
        @Path("chatId") chatId: Int,
        @Body request: com.sigittuning.tuninggarage.viewmodels.CreateChatMessageDto
    ): Response<ApiResponse<com.sigittuning.tuninggarage.viewmodels.ChatMessageDto>>
}
