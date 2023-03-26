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

    private var timeSlots: List<String> = listOf("")

    private lateinit var bookingBinding: ActivityBookingBinding

    private lateinit var timeSlotsAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookingBinding = ActivityBookingBinding.inflate(layoutInflater)
        val view = bookingBinding.root
        setContentView(view)


        timeSlotsAdapter = ArrayAdapter(this, R.layout.spinner_item, mutableListOf())
        timeSlotsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        bookingBinding.spinnerTimeSlot.adapter = timeSlotsAdapter

        getAndSetData()

    }

    private fun getAndSetData() {

        barbersList = intent.getParcelableArrayListExtra("barbersList")!!
        position = intent.getIntExtra("position", 0)
        timeSlots = barbersList[position].timeslots
            .filter { it.availability != 1 } // Filter out booked slots
            .map { it.time } // Get the time of available slots

        timeSlotsAdapter.clear()
        timeSlotsAdapter.addAll(timeSlots)
        timeSlotsAdapter.notifyDataSetChanged()

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