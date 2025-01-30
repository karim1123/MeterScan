package com.gabbasov.meterscan.auth.data.repository

import com.gabbasov.meterscan.auth.AuthDataSource
import com.gabbasov.meterscan.auth.User
import com.gabbasov.meterscan.auth.domain.AuthRepository
import com.gabbasov.meterscan.network.Resource
import kotlinx.coroutines.flow.Flow

internal class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource,
) : AuthRepository {
    /**
     * Регистрация нового пользователя по email и паролю.
     *
     * @param email Email пользователя.
     * @param password Пароль пользователя.
     * @return Результат операции: `Result.Success` с данными пользователя или `Result.Error` с исключением.
     */
    override suspend fun signUp(
        email: String,
        password: String,
    ): Resource<User> {
        return authDataSource.signUp(email, password)
    }

    /**
     * Авторизация пользователя по email и паролю.
     *
     * @param email Email пользователя.
     * @param password Пароль пользователя.
     * @return Результат операции: `Result.Success` с данными пользователя или `Result.Error` с исключением.
     */
    override suspend fun signIn(
        email: String,
        password: String,
    ): Resource<User> {
        return authDataSource.signIn(email, password)
    }

    override fun getCurrentUser(): Flow<Resource<User?>> {
        return authDataSource.getCurrentUser()
    }
}
