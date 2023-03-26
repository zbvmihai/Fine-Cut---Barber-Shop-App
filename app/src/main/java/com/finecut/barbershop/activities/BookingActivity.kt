@file:Suppress("DEPRECATION")

package com.finecut.barbershop.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.finecut.barbershop.R
import com.finecut.barbershop.databinding.ActivityBookingBinding
import com.finecut.barbershop.models.Barbers
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class BookingActivity : AppCompatActivity() {

    private var barbersList: ArrayList<Barbers> = arrayListOf()
    private var position: Int = 0

    private lateinit var bookingBinding: ActivityBookingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookingBinding = ActivityBookingBinding.inflate(layoutInflater)
        val view = bookingBinding.root
        setContentView(view)

        val timeSlots = listOf("9:00 AM - 10:00 AM", "10:00 AM - 11:00 AM",
            "11:00 AM - 12:00 PM", "12:00 PM - 1:00 PM", "1:00 PM - 2:00 PM",
            "11:00 AM - 12:00 PM", "12:00 PM - 1:00 PM", "1:00 PM - 2:00 PM",
            "11:00 AM - 12:00 PM", "12:00 PM - 1:00 PM", "1:00 PM - 2:00 PM",
            "2:00 PM - 3:00 PM", "3:00 PM - 4:00 PM", "4:00 PM - 5:00 PM")

        val adapter = ArrayAdapter(this, R.layout.spinner_item, timeSlots)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        bookingBinding.spinnerTimeSlot.adapter = adapter

        getAndSetData()

    }

    private fun getAndSetData() {

        barbersList = intent.getParcelableArrayListExtra("barbersList")!!
        position = intent.getIntExtra("position", 0)

        bookingBinding.bookingTbTitle.text = barbersList[position].barberName
        bookingBinding.rbBookingBarberRating.rating = barbersList[position].barberRating.toFloat()

        Picasso.get().load(barbersList[position].barberImage)
            .into(bookingBinding.ivBookingBarberImage, object :
                Callback {
                override fun onSuccess() {
                    bookingBinding.pbBookingBarberImage.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    if (e != null) {
                        e.localizedMessage?.let { Log.e("Error Loading Image: ", it) }
                    }
                }
            })
    }
}