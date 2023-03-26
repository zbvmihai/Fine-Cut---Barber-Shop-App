package com.finecut.barbershop.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.finecut.barbershop.adapters.BarbersAdapter
import com.finecut.barbershop.databinding.ActivityMainBinding
import com.finecut.barbershop.models.Barbers
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private var backPressedTime: Long = 0

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val myReference: DatabaseReference = database.reference.child("Barbers")

    private val barbersList = ArrayList<Barbers>()
    private lateinit var barbersAdapter: BarbersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        setupActionBar()
        setupOnBackPressedCallback()
        retrieveBarbersFromDatabase()
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

    private fun retrieveBarbersFromDatabase(){

        myReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                barbersList.clear()

                for (eachBarber in snapshot.children){

                    val barber = eachBarber.getValue(Barbers::class.java)

                    if (barber !=null){

                        barbersList.add(barber)

                    }

                    barbersAdapter = BarbersAdapter(barbersList)

                    mainBinding.rvMain.layoutManager = LinearLayoutManager(this@MainActivity)

                    mainBinding.rvMain.adapter = barbersAdapter
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database Error: ", error.toString())
            }

        })
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
