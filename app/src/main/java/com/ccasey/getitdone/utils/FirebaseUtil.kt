package com.ccasey.getitdone.utils

import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig
import com.google.firebase.auth.FirebaseAuth
import java.util.*


object FirebaseUtil {
    private const val RC_SIGN_IN = 123
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    lateinit var firebaseAuthListener: FirebaseAuth.AuthStateListener
    lateinit var mainActivity: AppCompatActivity
    lateinit var authListener: FirebaseAuth.AuthStateListener

    fun init(activity: AppCompatActivity) {
        mainActivity = activity
        authListener = FirebaseAuth.AuthStateListener {
            if (firebaseAuth.currentUser == null) {
                signIn()
            }
        }
    }

    fun addListener() { firebaseAuth.addAuthStateListener { authListener } }

    fun detachListener() { firebaseAuth.removeAuthStateListener(authListener) }

    private fun signIn() {
        // Choose authentication providers
        val providers = Arrays.asList(
            IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
            IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
        )

// Create and launch sign-in intent
        mainActivity.startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            RC_SIGN_IN
        )
    }

    fun registerEmail(email: String, password: String, onCompleteLoader:() -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task ->
            if(task.isSuccessful) {
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    println("SUCCESS!: ${firebaseAuth.currentUser?.uid}")
                    firebaseAuth.signOut()
                    onCompleteLoader.invoke()

                } else {
                    // If sign in fails, display a message to the user.
                }
            }
        }
    }

    fun login(email: String, password: String, onCompleteLoader: () -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if(task.isSuccessful) {
                println("SUCCCESS SIGNIN: ${firebaseAuth.currentUser?.email}")
                onCompleteLoader.invoke()
            }
        }.addOnFailureListener {
            println("SIGNIN FAILURE: ${it.message}")
        }
    }
}