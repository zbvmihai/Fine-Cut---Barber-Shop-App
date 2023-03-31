package com.finecut.barbershop.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.finecut.barbershop.R
import com.finecut.barbershop.adapters.MyBookingsAdapter
import com.finecut.barbershop.databinding.ActivityMyBookingsBinding
import com.finecut.barbershop.models.Bookings
import com.finecut.barbershop.models.Users
import com.finecut.barbershop.utils.BaseActivity
import com.finecut.barbershop.utils.FirebaseData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class MyBookingsActivity : BaseActivity() {

    private lateinit var myBookingsBinding: ActivityMyBookingsBinding

    private lateinit var bookingsAdapter: MyBookingsAdapter

    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        myBookingsBinding = ActivityMyBookingsBinding.inflate(layoutInflater)
        val view = myBookingsBinding.root
        setContentView(view)

        setupActionAndSideMenuBar(this,myBookingsBinding.tbMyBookings,true,view)

        val currentUserId = auth.currentUser?.uid

        FirebaseData.DBHelper.getCurrentUserFromDatabase(currentUserId!!, object: FirebaseData.DBHelper.CurrentUserCallback{
            @SuppressLint("SetTextI18n")
            override fun onSuccess(currentUser: Users) {

                myBookingsBinding.tvMyBookingsUserName.text = "${currentUser.firstName} ${currentUser.surname}"

                Picasso.get().load(currentUser.image.ifEmpty { getString(R.string.userImagePlaceHolder)})
                    .into(myBookingsBinding.ivMyBookingsUserImage, object : Callback {
                        override fun onSuccess() {
                            myBookingsBinding.pbMyBookings.visibility = View.GONE
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

        FirebaseData.DBHelper.getBookingsFromDatabase(currentUserId, object : FirebaseData.DBHelper.BookingsCallback{
            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(bookingsList: ArrayList<Bookings>) {
                Log.d("BookingsListSize", "Size: ${bookingsList.size}")

                bookingsAdapter = MyBookingsAdapter(this@MyBookingsActivity,bookingsList)
                myBookingsBinding.rvMyBookings.layoutManager = LinearLayoutManager(this@MyBookingsActivity)
                myBookingsBinding.rvMyBookings.adapter = bookingsAdapter
                bookingsAdapter.notifyDataSetChanged()

            }

            override fun onFailure(error: DatabaseError) {
                Log.e("Database Error: ", error.toString())
            }

        })
    }

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