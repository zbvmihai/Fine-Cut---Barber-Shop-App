package com.finecut.barbershop.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.finecut.barbershop.databinding.ActivityMyBookingsBinding

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var myBookingsBinding: ActivityMyBookingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        myBookingsBinding = ActivityMyBookingsBinding.inflate(layoutInflater)
        val view = myBookingsBinding.root
        setContentView(view)
    }
}