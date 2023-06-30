package com.nf_hack.backgroundlocationtracking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("http://172.20.10.3:5000") // Replace with your base URL
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(MyApiService::class.java)
