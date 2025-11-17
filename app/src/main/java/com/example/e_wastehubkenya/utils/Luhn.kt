package com.example.e_wastehubkenya.utils

object Luhn {
    fun isValidLuhn(number: String): Boolean {
        if (number.any { !it.isDigit() }) {
            return false
        }

        var sum = 0
        var alternate = false
        for (i in number.length - 1 downTo 0) {
            var n = number.substring(i, i + 1).toInt()
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = (n % 10) + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }
}