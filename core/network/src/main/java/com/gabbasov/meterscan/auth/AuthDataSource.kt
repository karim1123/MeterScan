package com.gabbasov.meterscan.auth

import com.gabbasov.meterscan.auth.mapper.toUser
import com.gabbasov.meterscan.model.auth.User
import com.gabbasov.meterscan.common.network.CoroutineDispatchers
import com.gabbasov.meterscan.base.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthDataSource(
    val firebaseAuth: FirebaseAuth,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    /**
     * Регистрация нового пользователя по email и паролю.
     *
     * @param email Email пользователя.
     * @param password Пароль пользователя.
     */
    suspend fun signUp(
        email: String,
        password: String,
    ): Resource<User> {
        return withContext(coroutineDispatchers.io) {
            try {
                val authResult =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                if (user != null) {
                    Resource.Success(user.toUser())
                } else {
                    Resource.Error(Exception("User is null after registration"))
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }
    }

    /**
     * Авторизация пользователя по email и паролю.
     *
     * @param email Email пользователя.
     * @param password Пароль пользователя.
     */
    suspend fun signIn(
        email: String,
        password: String,
    ): Resource<User> {
        return withContext(coroutineDispatchers.io) {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = authResult.user
                if (user != null) {
                    Resource.Success(user.toUser())
                } else {
                    Resource.Error(Exception("User is null after sign-in"))
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }
    }

    /**
     * Выход из аккаунта текущего пользователя.
     */
    suspend fun signOut(): Resource<Unit> {
        return withContext(coroutineDispatchers.io) {
            try {
                // Выполняем выход из аккаунта
                firebaseAuth.signOut()
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }
    }

    /**
     * Получение текущего авторизованного пользователя.
     * Возвращает `Flow`, так как состояние пользователя может изменяться.
     */
    fun getCurrentUser(): Flow<Resource<User?>> =
        flow {
            try {
                val currentUser = firebaseAuth.currentUser
                emit(Resource.Success(currentUser?.toUser()))
            } catch (e: Exception) {
                emit(Resource.Error(e))
            }
        }.flowOn(coroutineDispatchers.io)
}
