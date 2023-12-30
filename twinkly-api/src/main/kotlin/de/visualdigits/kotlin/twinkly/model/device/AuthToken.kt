package de.visualdigits.kotlin.twinkly.model.device

class AuthToken(
    val authToken: String? = null,
    val tokenExpires: Long = 0,
    var loggedIn: Boolean? = null
 )
