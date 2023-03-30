package com.finecut.barbershop.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.finecut.barbershop.adapters.BarbersAdapter
import com.finecut.barbershop.databinding.ActivityMainBinding
import com.finecut.barbershop.models.Barbers
import com.finecut.barbershop.utils.BaseActivity
import com.finecut.barbershop.utils.FirebaseData.DBHelper
import com.google.firebase.database.DatabaseError

class MainActivity : BaseActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private var backPressedTime: Long = 0

    private lateinit var barbersAdapter: BarbersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        setupActionAndSideMenuBar(this,mainBinding.tbMain,false,view)
        setupOnBackPressedCallback()
        retrieveBarbersFromDatabase()
    }

    override fun onResume() {
        super.onResume()
        retrieveBarbersFromDatabase()
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

    private fun retrieveBarbersFromDatabase(){

        DBHelper.getBarbersFromDatabase(object:
            DBHelper.BarbersCallback {
            override fun onSuccess(barbersList: ArrayList<Barbers>) {

                barbersAdapter = BarbersAdapter(this@MainActivity,barbersList)
                mainBinding.rvMain.layoutManager = LinearLayoutManager(this@MainActivity)
                mainBinding.rvMain.adapter = barbersAdapter
                mainBinding.pbMain.visibility = View.GONE
            }

            override fun onFailure(error: DatabaseError) {
                Log.e("Database Error: ", error.toString())
            }
        })
    }
}


