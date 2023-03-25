package com.finecut.barbershop.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.finecut.barbershop.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    private lateinit var splashBinding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashBinding = ActivityIntroBinding.inflate(layoutInflater)
        val view = splashBinding.root
        setContentView(view)
    }
}