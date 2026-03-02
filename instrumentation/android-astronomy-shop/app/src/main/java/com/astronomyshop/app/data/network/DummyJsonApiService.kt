package com.astronomyshop.app.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface DummyJsonApiService {

    @GET("products")
    suspend fun getProducts(@Query("limit") limit: Int = 30): DummyProductsResponse

    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): DummyProductsResponse

    @GET("products/categories")
    suspend fun getCategories(): List<DummyCategory>
}
