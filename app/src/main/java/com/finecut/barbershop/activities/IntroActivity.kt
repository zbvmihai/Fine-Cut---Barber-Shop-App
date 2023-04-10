package com.finecut.barbershop.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.finecut.barbershop.databinding.ActivityIntroBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IntroActivity : AppCompatActivity() {

    private lateinit var splashBinding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashBinding = ActivityIntroBinding.inflate(layoutInflater)
        val view = splashBinding.root
        setContentView(view)

        // This block of code launch a coroutine main thread to wait for 2 seconds and start the
        // Log In Activity
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // Wait for 2 seconds
            startActivity(Intent(this@IntroActivity, LogInActivity::class.java))
            finish() // Close current activity
        }
    }
}