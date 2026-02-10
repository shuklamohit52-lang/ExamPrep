package com.examprep.data.api

import retrofit2.http.GET

interface ExamPrepApi {
    @GET("api/tests")
    suspend fun getTests(): List<ApiTestSeries>

    @GET("api/resources")
    suspend fun getResources(): List<ResourceFile>

    @GET("api/current-affairs")
    suspend fun getCurrentAffairs(): List<CurrentAffairItem>
}
