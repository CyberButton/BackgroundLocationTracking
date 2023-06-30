package com.nf_hack.backgroundlocationtracking

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MyApiService {
    @GET("/danger_level")
    suspend fun getData(@Query("latitude") param1: String, @Query("longitude") param2: String): Response<ResponseModel>
}

