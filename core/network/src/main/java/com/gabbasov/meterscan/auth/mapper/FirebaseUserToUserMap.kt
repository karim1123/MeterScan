package com.gabbasov.meterscan.auth.mapper

import com.gabbasov.meterscan.auth.User
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toUser(): User {
    return User(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )
}
