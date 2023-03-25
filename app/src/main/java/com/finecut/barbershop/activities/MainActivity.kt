package com.finecut.barbershop.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.finecut.barbershop.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)


    }
}