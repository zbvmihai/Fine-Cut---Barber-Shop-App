package com.finecut.barbershop.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        requestNotificationPermission()

        setupActionAndSideMenuBar(this,mainBinding.tbMain,false,view)
        setupOnBackPressedCallback()
        retrieveBarbersFromDatabase()
    }

    override fun onResume() {
        super.onResume()
        retrieveBarbersFromDatabase()
    }

    // This function change the behaviour of the back button, if the button is pressed once,
    // it will display a message to inform the user that he have to press the back button twice
    // in case he want to close the app. If the back button is pressed twitch within 2 seconds,
    // the app will close.
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

    // This function retrieve all the barbers from the database and
    // set the barbers adapter of the recycler view to display the all the barbers in the app.
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

    // This function setup the notifications channels,
    // so the user will be able to control what kind of notifications to receive.
    private fun setUpNotificationChannels() {
        val channelIdBookingStatus = "booking_status_channel"
        val channelNameBookingStatus = "Booking Status Channel"
        val channelIdReminders = "reminders_channel"
        val channelNameReminders = "Reminders Channel"
        val channelIdOffers = "offers_channel"
        val channelNameOffers = "Offers Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channelBookingStatus = NotificationChannel(channelIdBookingStatus, channelNameBookingStatus, importance)
            val channelReminders = NotificationChannel(channelIdReminders, channelNameReminders, importance)
            val channelOffers = NotificationChannel(channelIdOffers, channelNameOffers, importance)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelBookingStatus)
            notificationManager.createNotificationChannel(channelReminders)
            notificationManager.createNotificationChannel(channelOffers)
        }
    }

    // This function will request notifications permission if the app is running on Android 13+
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            } else {
                setUpNotificationChannels()
            }
        } else {
            setUpNotificationChannels()
        }
    }

    // This function will setup the notification channels if the permission is granted.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpNotificationChannels()
            } else {
                Toast.makeText(applicationContext, "Permission denied. Notifications won't be enabled.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


