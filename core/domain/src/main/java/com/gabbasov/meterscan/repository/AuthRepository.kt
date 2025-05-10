package com.gabbasov.meterscan.repository

import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.model.auth.User
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

    suspend fun signOut(): Resource<Unit>
}
