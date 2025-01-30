package com.gabbasov.meterscan.auth.domain

import com.gabbasov.meterscan.auth.User
import com.gabbasov.meterscan.network.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(
        email: String,
        password: String,
    ): Resource<User>

    suspend fun signIn(
        email: String,
        password: String,
    ): Resource<User>

    fun getCurrentUser(): Flow<Resource<User?>>
}
