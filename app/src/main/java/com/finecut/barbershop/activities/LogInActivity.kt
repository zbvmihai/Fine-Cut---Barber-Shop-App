package com.finecut.barbershop.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.finecut.barbershop.databinding.ActivityLogInBinding

class LogInActivity : AppCompatActivity() {

    private lateinit var logInBinding: ActivityLogInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logInBinding = ActivityLogInBinding.inflate(layoutInflater)
        val view = logInBinding.root
        setContentView(view)

        logInBinding.tvBtnRegister.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }
        logInBinding.tvBtnSignUpNow.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }
    }
}