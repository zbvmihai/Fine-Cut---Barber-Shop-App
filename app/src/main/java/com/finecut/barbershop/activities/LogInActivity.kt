package com.finecut.barbershop.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finecut.barbershop.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth

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

    private fun signIn(userEmail: String, userPassword: String) {

        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "User Signed In!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
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