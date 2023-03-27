@file:Suppress("DEPRECATION")

package com.finecut.barbershop.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finecut.barbershop.R
import com.finecut.barbershop.databinding.ActivityBookingBinding
import com.finecut.barbershop.models.Barbers
import com.finecut.barbershop.models.Bookings
import com.google.android.material.datepicker.MaterialDatePicker
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class BookingActivity : AppCompatActivity() {

    private var barbersList: ArrayList<Barbers> = arrayListOf()
    private var position: Int = 0
    private var timeSlotsList: List<String> = emptyList()

    private lateinit var bookingBinding: ActivityBookingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookingBinding = ActivityBookingBinding.inflate(layoutInflater)
        val view = bookingBinding.root
        setContentView(view)


        val timeSlotsAdapter = ArrayAdapter(this, R.layout.spinner_item, timeSlotsList)
        timeSlotsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        bookingBinding.spinnerTimeSlot.adapter = timeSlotsAdapter

        getAndSetData()

        bookingBinding.editTextDate.setOnClickListener {
            pickDate()
        }

    }

    private fun getAndSetData() {

        barbersList = intent.getParcelableArrayListExtra("barbersList")!!
        position = intent.getIntExtra("position", 0)

        bookingBinding.bookingTbTitle.text = barbersList[position].name
        bookingBinding.rbBookingBarberRating.rating = barbersList[position].rating

        Picasso.get().load(barbersList[position].image)
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

        val services = barbersList[position].services
        val servicesList = arrayListOf<String>()
        for (service in services) {
            servicesList.add(service.name)

            val servicesAdapter = ArrayAdapter(this, R.layout.spinner_item, servicesList)
            servicesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            bookingBinding.spinnerService.adapter = servicesAdapter
        }
    }

    private fun getAvailableTimeSlots(bookings: List<Bookings>, selectedDate: String): List<String> {
        val allTimeSlots = listOf(
            "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00",
            "16:00", "17:00", "18:00", "19:00", "20:00"
        )


        val filteredBookings = bookings.filter { it.date == selectedDate }
        val bookedTimeSlots = filteredBookings.map { it.timeslot }

        Log.d("BookingsDebug", "All Bookings: $bookings")
        Log.d("BookingsDebug", "Filtered Bookings: $filteredBookings")
        bookings.forEach {
            Log.d("DateFormatDebug", "Booking Date: ${it.date}") // The date format should now match the database
        }

        return allTimeSlots.filterNot { bookedTimeSlots.contains(it) }
    }

    private fun updateTimeSlotsSpinner(availableTimeSlots: List<String>) {
        timeSlotsList = availableTimeSlots
        val timeSlotsAdapter = ArrayAdapter(this, R.layout.spinner_item, availableTimeSlots)
        timeSlotsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        bookingBinding.spinnerTimeSlot.adapter = timeSlotsAdapter
    }

    private fun pickDate() {
        val builder = MaterialDatePicker.Builder.datePicker()
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Date(selection)
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) // Change the format here
            val formattedDate = dateFormat.format(selectedDate)
            Log.d("DateFormatDebug", "Formatted Date: $formattedDate")
            bookingBinding.editTextDate.text = formattedDate

            val availableTimeSlots = getAvailableTimeSlots(barbersList[position].bookings, formattedDate)
            if (availableTimeSlots.isEmpty()) {
                Toast.makeText(
                    this@BookingActivity,
                    "All timeslots are booked for this day, please select another date.",
                    Toast.LENGTH_LONG
                ).show()
                updateTimeSlotsSpinner(emptyList())
            } else {
                updateTimeSlotsSpinner(availableTimeSlots)
            }
        }
        picker.show(supportFragmentManager, picker.toString())
    }

}