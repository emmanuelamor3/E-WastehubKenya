package com.example.e_wastehubkenya

import com.example.e_wastehubkenya.data.model.Listing
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

// DATA CLASSES 
// These declarations ensure the backend knows what data to expect.

@Serializable
data class SignupRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val role: String
)

@Serializable
data class LoginResponse(
    val token: String?,
    val message: String,
    val role: String?
)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class SerialCheckRequest(val serialNumber: String)

@Serializable
data class MpesaCallback(
    val Body: MpesaBody
)

@Serializable
data class MpesaBody(
    val stkCallback: StkCallback
)

@Serializable
data class StkCallback(
    val MerchantRequestID: String,
    val CheckoutRequestID: String,
    val ResultCode: Int,
    val ResultDesc: String,
    val CallbackMetadata: CallbackMetadata? = null
)

@Serializable
data class CallbackMetadata(
    val Item: List<CallbackItem>
)

@Serializable
data class CallbackItem(
    val Name: String,
    val Value: Any? = null
)

// IN-MEMORY DATABASES 
// 

val userDatabase = mutableMapOf<String, SignupRequest>()
val listingsDatabase = mutableListOf<Listing>()
// A valid Luhn number is added to the stolen list for testing.
val stolenSerialNumbers = setOf("SN123456", "SN654321", "79927398713")

/**
 Checks if the given number string is valid according to the Luhn algorithm.
 The Luhn algorithm is used to validate a variety of identification numbers, such as credit card numbers and IMEI numbers.
 */
fun isValidLuhn(number: String): Boolean {
    // The number must be a digit-only string with a length greater than 0.
    if (!number.matches(Regex("^\\d+$")) || number.length <= 0) {
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

fun main() {
    // Changed port to 8080 to avoid conflicts
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        // AUTHENTICATION ROUTES 

        post("/signup") {
            val signupRequest = call.receive<SignupRequest>()
            if (userDatabase.containsKey(signupRequest.email)) {
                call.respond(MessageResponse("User with this email already exists."))
            } else {
                userDatabase[signupRequest.email] = signupRequest
                call.respond(MessageResponse("User signed up successfully"))
            }
        }

        post("/login") {
            val loginRequest = call.receive<LoginRequest>()
            val user = userDatabase[loginRequest.email]

            if (user != null && user.password == loginRequest.password && user.role == loginRequest.role) {
                val fakeToken = "fake-jwt-token-for-${loginRequest.email}"
                call.respond(LoginResponse(token = fakeToken, message = "Login successful", role = user.role))
            } else {
                call.respond(LoginResponse(token = null, message = "Invalid credentials or role", role = null))
            }
        }

        //  E-WASTE AND SERIAL NUMBER ROUTES 

        post("/check-serial") {
            val request = call.receive<SerialCheckRequest>()
            val serialNumber = request.serialNumber

            // A serial number is invalid if it's stolen OR fails the Luhn check.
            if (stolenSerialNumbers.contains(serialNumber) || !isValidLuhn(serialNumber)) {
                call.respond(MessageResponse("This item may be flagged as stolen or invalid."))
            } else {
                call.respond(MessageResponse("Serial number appears to be valid."))
            }
        }

        post("/listings") {
            try {
                val listing = call.receive<Listing>()
                listingsDatabase.add(listing)
                println("Received listing: $listing")
                println("Current listings in DB: $listingsDatabase")
                call.respond(MessageResponse("Listing submitted successfully"))
            } catch (e: Exception) {
                println("Error receiving listing: ${e.message}")
                call.respond(MessageResponse("Failed to submit listing."))
            }
        }

        //  MPESA CALLBACK 
        post("/mpesa-callback") {
            try {
                val callback = call.receive<MpesaCallback>()
                println("M-Pesa callback received: $callback")
                call.respond(MessageResponse("Callback received successfully."))
            } catch (e: Exception) {
                println("Error receiving M-Pesa callback: ${e.message}")
                call.respond(MessageResponse("Failed to process callback."))
            }
        }
    }
}
