package com.example.e_wastehubkenya

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        // Enable offline persistence for Firestore
        val firestore = FirebaseFirestore.getInstance()
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        firestore.firestoreSettings = settings

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }
}
