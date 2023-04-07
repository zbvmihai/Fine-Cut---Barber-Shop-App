package com.finecut.barbershop.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finecut.barbershop.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class LogInActivity : AppCompatActivity() {

    private lateinit var logInBinding: ActivityLogInBinding

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logInBinding = ActivityLogInBinding.inflate(layoutInflater)
        val view = logInBinding.root
        setContentView(view)

        logInBinding.tvBtnRegister.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        logInBinding.tvBtnSignUpNow.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        logInBinding.btnLogIn.setOnClickListener {

            val userEmail = logInBinding.etLoginEmail.text.toString()
            val userPassword = logInBinding.etLoginPassword.text.toString()

            if (validateForm(userEmail,userPassword)){
                signIn(userEmail, userPassword)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser

        if (currentUser !=null){

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signIn(userEmail: String, userPassword: String) {

        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "User Signed In!", Toast.LENGTH_SHORT).show()

                FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                    if (tokenTask.isSuccessful) {
                        val token = tokenTask.result
                        updateFcmToken(token)
                    } else {
                        Log.e(TAG, "Error getting FCM token", task.exception)
                    }
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Failed to sign in the user. Check email or password!",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("Error:", task.exception.toString())
            }
        }
    }

    private fun updateFcmToken(token: String) {
        // Get the user ID of the currently signed-in user.
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Update the FCM token in the user's database node.
            val database = FirebaseDatabase.getInstance().reference
            database.child("Users").child(userId).child("fcmToken").setValue(token)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM token updated for user $userId")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Error updating FCM token for user $userId", it)
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {

        return when {
            TextUtils.isEmpty(email) -> {
                Toast.makeText(applicationContext, "Please enter an email!", Toast.LENGTH_SHORT)
                    .show()
                false
            }

            TextUtils.isEmpty(password) -> {
                Toast.makeText(applicationContext, "Please enter a password!", Toast.LENGTH_SHORT)
                    .show()
                false
            }
            else -> {
                true
            }
        }
    }
}