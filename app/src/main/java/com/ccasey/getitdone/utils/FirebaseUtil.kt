package com.ccasey.getitdone.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*


object FirebaseUtil {
    private const val RC_SIGN_IN = 123
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var activity: AppCompatActivity
    lateinit var authListener: FirebaseAuth.AuthStateListener

    fun init(activity: AppCompatActivity) {
        this.activity = activity
        authListener = FirebaseAuth.AuthStateListener {
            if (firebaseAuth.currentUser != null) {
                if(!firebaseAuth.currentUser!!.isEmailVerified) {
                    Toast.makeText(activity.applicationContext, "Please verify your email: ${firebaseAuth.currentUser}.", Toast.LENGTH_SHORT).show()
                    sendEmailVerification(firebaseAuth.currentUser)
                }
            }
        }
    }

    fun addListener() { firebaseAuth.addAuthStateListener { authListener } }

    fun detachListener() { firebaseAuth.removeAuthStateListener(authListener) }

    fun registerEmail(email: String, password: String, onCompleteLoader:() -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                println("SUCCESS!: ${firebaseAuth.currentUser?.uid}")
                firebaseAuth.signOut()
                sendEmailVerification(task.result?.user)
                onCompleteLoader.invoke()

            } else {
                // If sign in fails, display a message to the user.
            }
        }
    }

    fun login(email: String, password: String, onCompleteLoader: () -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if(task.isSuccessful) {
                if(task.result?.user?.isEmailVerified!!) {
                    println("SUCCCESS SIGNIN: ${firebaseAuth.currentUser?.email}")
                    onCompleteLoader.invoke()
                } else {
                    firebaseAuth.signOut()
                    sendEmailVerification(task.result?.user)
                    Toast.makeText(activity.applicationContext, "Please verify your email: ${task.result?.user?.email}.", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            println("SIGNIN FAILURE: ${it.message}")
        }
    }

    fun sendEmailVerification(user: FirebaseUser?) {
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                Toast.makeText(activity.applicationContext, "Verification Email has been sent.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity.applicationContext, "An issue occurred. Verification Email was not sent.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*private fun signIn() {
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
    }*/
}