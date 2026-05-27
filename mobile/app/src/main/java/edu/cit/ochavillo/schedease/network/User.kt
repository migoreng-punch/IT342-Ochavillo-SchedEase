package edu.cit.ochavillo.schedease.network

data class User(
    val username: String,
    val firstName: String,
    val role: String,
    val isEmailVerified: Boolean
)