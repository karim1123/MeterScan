package com.gabbasov.meterscan.auth.data.repository

import com.gabbasov.meterscan.auth.AuthDataSource
import com.gabbasov.meterscan.auth.User
import com.gabbasov.meterscan.auth.domain.SignUpRepository
import com.gabbasov.meterscan.network.Resource

internal class SignUpRepositoryImpl(
    private val authDataSource: AuthDataSource,
) : SignUpRepository {
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
}
