package com.gabbasov.meterscan.network

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>

    data class Error(val exception: Throwable) : Resource<Nothing>
}
