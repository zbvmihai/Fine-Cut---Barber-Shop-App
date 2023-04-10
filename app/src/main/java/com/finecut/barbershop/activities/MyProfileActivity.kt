package com.finecut.barbershop.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.finecut.barbershop.R
import com.finecut.barbershop.databinding.ActivityMyProfileBinding
import com.finecut.barbershop.models.Users
import com.finecut.barbershop.utils.BaseActivity
import com.finecut.barbershop.utils.FirebaseData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class MyProfileActivity : BaseActivity() {

    private lateinit var myProfileBinding: ActivityMyProfileBinding
    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        myProfileBinding = ActivityMyProfileBinding.inflate(layoutInflater)
        val view = myProfileBinding.root
        setContentView(view)

        val currentUserId = auth.currentUser?.uid

        setupActionAndSideMenuBar(this,myProfileBinding.tbBooking,true,view)

        // This block of code retrieve the authenticated user details from the database
        // and update the views in My Profile Activity.
        FirebaseData.DBHelper.getCurrentUserFromDatabase(currentUserId!!, object: FirebaseData.DBHelper.CurrentUserCallback{
            @SuppressLint("SetTextI18n")
            override fun onSuccess(currentUser: Users) {

                myProfileBinding.tvMyProfileUserName.visibility = View.VISIBLE
                myProfileBinding.tvMyProfilePoints.visibility = View.VISIBLE
                myProfileBinding.pbMyProfileUserNamePoints.visibility = View.GONE

                myProfileBinding.tvMyProfileUserName.text = "${currentUser.firstName} ${currentUser.surname}"
                myProfileBinding.tvMyProfilePoints.text = "Loyalty Points: ${currentUser.points} "

                Picasso.get().load(currentUser.image.ifEmpty { getString(R.string.userImagePlaceHolder)})
                    .into(myProfileBinding.ivMyProfileUserImage, object : Callback{
                        override fun onSuccess() {
                            myProfileBinding.pbMyProfileUserImage.visibility = View.GONE
                        }
                        override fun onError(e: Exception?) {
                            if (e != null) {
                                e.localizedMessage?.let { Log.e("Load Image Error:" , it) }
                            }
                        }
                    })
            }
            override fun onFailure(error: DatabaseError) {
                Log.e("Database Error: ", error.toString())
            }
        })

        // When My Bookings button is clicked the user will be redirected to the My Bookings Activity
        myProfileBinding.btnMyBookings.setOnClickListener {
            startActivity(Intent(this,MyBookingsActivity::class.java))
        }

        // When Edit Details button is clicked, Edit Details Activity is opened.
        myProfileBinding.btnMyBookingsEditDetails.setOnClickListener {
            startActivity(Intent(this,EditDetailsActivity::class.java))
        }

        // When Sign Out button is clicked, the user will be signed out
        // and the sign in activity will open
        myProfileBinding.btnMyBookingsLogOut.setOnClickListener {

            auth.signOut()
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }
    }

    // This overridden function make the back button to finish current activity
    // and go back to the previous one.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}