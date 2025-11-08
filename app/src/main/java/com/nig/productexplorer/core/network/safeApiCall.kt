package com.nig.productexplorer.core.network

import com.nig.productexplorer.core.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    apiCall: suspend () -> T
): Resource<T> {
    return withContext(dispatcher) {
        try {
            Resource.Success(apiCall.invoke())
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Bad request. Please check the request and try again."
                401 -> "Unauthorized."
                404 -> "Resource not found."
                in 500..599 -> "Server error. Please try again later."
                else -> "HTTP error: ${e.code()} - ${e.message()}"
            }
            Resource.Error(errorMessage, data = null)
        } catch (e: IOException) {
            Resource.Error("Network error: Could not connect to the server. Please check your internet connection.", data = null)
        } catch (e: Exception) {
            Resource.Error("An unexpected error occurred: ${e.localizedMessage ?: "Unknown error"}", data = null)
        }
    }
}