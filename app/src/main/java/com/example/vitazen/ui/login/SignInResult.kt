package com.example.vitazen.ui.login

// Lớp này chứa dữ liệu người dùng trả về từ Google
data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)

// Lớp này chứa kết quả cuối cùng: hoặc là dữ liệu người dùng, hoặc là lỗi
data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)
