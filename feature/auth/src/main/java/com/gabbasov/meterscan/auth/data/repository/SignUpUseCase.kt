package com.gabbasov.meterscan.auth.data.repository

import android.util.Patterns
import com.gabbasov.meterscan.model.auth.User
import com.gabbasov.meterscan.repository.AuthRepository
import com.gabbasov.meterscan.common.network.Errors
import com.gabbasov.meterscan.base.Resource

const val MIN_PASSWORD_LENGTH = 6

class SignUpUseCase(
    private val authRepository: AuthRepository
) {
    data class Params(val email: String, val password: String, val repeatPassword: String)

    suspend fun execute(params: Params): Resource<User> {
        validate(params)?.let { error ->
            return Resource.Error(Throwable(error.name))
        }
        return authRepository.signUp(params.email, params.password)
    }

    private fun validate(params: Params): Errors? = when {
        params.email.isBlank() -> Errors.EMPTY_EMAIL
        !Patterns.EMAIL_ADDRESS.matcher(params.email).matches() -> Errors.INVALID_EMAIL
        params.password.isBlank() -> Errors.EMPTY_PASSWORD
        params.password.length < MIN_PASSWORD_LENGTH -> Errors.SHORT_PASSWORD
        params.password != params.repeatPassword -> Errors.PASSWORDS_DO_NOT_MATCH
        else -> null
    }
}
