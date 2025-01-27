package com.gabbasov.meterscan.data.repository

import com.gabbasov.meterscan.auth.User
import com.gabbasov.meterscan.network.Resource

interface SignUpRepository {
    suspend fun signUp(
        email: String,
        password: String,
    ): Resource<User>
}
