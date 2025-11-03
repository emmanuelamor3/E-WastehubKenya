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

// --- DATA CLASSES ---
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

// --- IN-MEMORY DATABASES ---
// In a real app, you'd use a proper database (like PostgreSQL, MongoDB, etc.)

val userDatabase = mutableMapOf<String, SignupRequest>()
val listingsDatabase = mutableListOf<Listing>()
val stolenSerialNumbers = setOf("SN123456", "SN654321", "SN987654") // Example flagged serial numbers

fun main() {
    embeddedServer(Netty, port = 3000, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        // --- AUTHENTICATION ROUTES ---

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

        // --- E-WASTE AND SERIAL NUMBER ROUTES ---

        post("/check-serial") {
            val request = call.receive<SerialCheckRequest>()
            if (stolenSerialNumbers.contains(request.serialNumber)) {
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
    }
}
