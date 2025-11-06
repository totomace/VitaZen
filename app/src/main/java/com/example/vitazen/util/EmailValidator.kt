package com.example.vitazen.util

import android.util.Patterns

/**
 * Utility class để validate email thật sự tồn tại.
 * Kiểm tra domain hợp lệ và chặn disposable/temp email.
 */
object EmailValidator {
    
    // Danh sách các disposable email domains phổ biến
    private val disposableEmailDomains = setOf(
        "10minutemail.com", "guerrillamail.com", "mailinator.com",
        "tempmail.com", "throwaway.email", "temp-mail.org",
        "fakeinbox.com", "yopmail.com", "trashmail.com",
        "dispostable.com", "emailondeck.com", "maildrop.cc",
        "sharklasers.com", "spam4.me", "getairmail.com"
    )
    
    // Các domain email provider hợp lệ phổ biến
    private val validEmailProviders = setOf(
        "gmail.com", "outlook.com", "hotmail.com", "yahoo.com",
        "icloud.com", "protonmail.com", "mail.com", "aol.com",
        "zoho.com", "gmx.com", "yandex.com", "mail.ru"
    )
    
    /**
     * Kiểm tra email có format hợp lệ không
     */
    fun isValidFormat(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Kiểm tra email có phải disposable/temp email không
     */
    fun isDisposableEmail(email: String): Boolean {
        val domain = email.substringAfterLast("@", "").lowercase()
        return disposableEmailDomains.contains(domain)
    }
    
    /**
     * Kiểm tra domain có thuộc các provider phổ biến không
     */
    fun isCommonProvider(email: String): Boolean {
        val domain = email.substringAfterLast("@", "").lowercase()
        return validEmailProviders.contains(domain)
    }
    
    /**
     * Kiểm tra domain có structure hợp lệ không (có . và phần sau . không rỗng)
     */
    fun hasValidDomainStructure(email: String): Boolean {
        val domain = email.substringAfterLast("@", "")
        if (domain.isEmpty()) return false
        
        val parts = domain.split(".")
        if (parts.size < 2) return false
        
        // Kiểm tra mỗi phần không rỗng và chỉ chứa ký tự hợp lệ
        return parts.all { part ->
            part.isNotEmpty() && 
            part.length >= 2 && 
            part.all { it.isLetterOrDigit() || it == '-' }
        }
    }
    
    /**
     * Validation toàn diện - trả về error message nếu không hợp lệ
     */
    fun validate(email: String): EmailValidationResult {
        val trimmedEmail = email.trim()
        
        // 1. Kiểm tra format cơ bản
        if (!isValidFormat(trimmedEmail)) {
            return EmailValidationResult(
                isValid = false,
                errorMessage = "Định dạng email không hợp lệ."
            )
        }
        
        // 2. Kiểm tra disposable email
        if (isDisposableEmail(trimmedEmail)) {
            return EmailValidationResult(
                isValid = false,
                errorMessage = "Không được sử dụng email tạm thời. Vui lòng dùng email thật."
            )
        }
        
        // 3. Kiểm tra domain structure
        if (!hasValidDomainStructure(trimmedEmail)) {
            return EmailValidationResult(
                isValid = false,
                errorMessage = "Tên miền email không hợp lệ. Vui lòng sử dụng email thật."
            )
        }
        
        // 4. Nếu không phải common provider, cảnh báo nhẹ (vẫn cho phép)
        val isCommon = isCommonProvider(trimmedEmail)
        
        return EmailValidationResult(
            isValid = true,
            isVerified = isCommon,
            warningMessage = if (!isCommon) "Đảm bảo email này đang hoạt động để nhận link xác thực." else null
        )
    }
}

/**
 * Kết quả validation email
 */
data class EmailValidationResult(
    val isValid: Boolean,
    val isVerified: Boolean = false, // true nếu là common provider
    val errorMessage: String? = null,
    val warningMessage: String? = null
)
