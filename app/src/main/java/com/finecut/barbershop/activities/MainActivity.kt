package com.finecut.barbershop.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.finecut.barbershop.databinding.ActivityMainBinding
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        setupActionBar()
        setupOnBackPressedCallback()
    }

    private fun setupActionBar() {
        setSupportActionBar(mainBinding.tbMain)

        val actionBar = supportActionBar

        actionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupOnBackPressedCallback() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    isEnabled = false
                    finish()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Press back again to close the app",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
}


//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//
//                Toast.makeText(this@MainActivity,"Back Clicked",Toast.LENGTH_SHORT).show()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//}
