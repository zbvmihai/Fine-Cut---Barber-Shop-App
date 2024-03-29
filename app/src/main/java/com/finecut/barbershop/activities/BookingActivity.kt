@file:Suppress("DEPRECATION")

package com.finecut.barbershop.activities

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.finecut.barbershop.R
import com.finecut.barbershop.databinding.ActivityBookingBinding
import com.finecut.barbershop.models.Barbers
import com.finecut.barbershop.models.Bookings
import com.finecut.barbershop.models.Offers
import com.finecut.barbershop.models.Users
import com.finecut.barbershop.utils.BaseActivity
import com.finecut.barbershop.utils.BookingReminderBroadcast
import com.finecut.barbershop.utils.FirebaseData
import com.finecut.barbershop.utils.FollowUpReminderBroadcast
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class BookingActivity : BaseActivity() {

    private lateinit var barber: Barbers

    private var timeSlotsList: List<String> = emptyList()
    private var offersList : List<Offers> = emptyList()

    private var userPoints: Long = 0
    private var usedPoints: Long = 0
    private var pointsUsed: Boolean = false

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var bookingBinding: ActivityBookingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookingBinding = ActivityBookingBinding.inflate(layoutInflater)
        val view = bookingBinding.root
        setContentView(view)

        setupActionAndSideMenuBar(this,bookingBinding.tbBooking,true,view)
        loadOffers()
        getAndSetData()
        getUserPoints()

        // When See reviews button is clicked, Reviews Activity is opened
        // and the barber object is passed to it.
        bookingBinding.btnSeeReviews.setOnClickListener {
            val intent = Intent(this@BookingActivity,ReviewsActivity::class.java)
            intent.putExtra("barber",barber)
            startActivity(intent)
        }
        // When the select date edit text is clicked the pickDate function is executed
        // and the calendar will be opened on the user screen to select a date
        bookingBinding.etBookingPickDate.setOnClickListener {
            pickDate()
        }

        // When Apply Discount button is clicked, the discount code field is checked to not be empty,
        // if the field is not empty, the validity of the code is checked and if it is ok,
        // the discount is applied to the total.
        bookingBinding.btnApplyDiscount.setOnClickListener {
            val discountCode = bookingBinding.etDiscountCode.text.toString().trim().capitalize(
                Locale.ROOT)
            if (discountCode.isNotEmpty()) {
                applyDiscount(discountCode)
            } else {
                Toast.makeText(this@BookingActivity, "Please enter a discount code.", Toast.LENGTH_SHORT).show()
            }
        }

        // When the Use Points button is clicked, the amount of points of the user is checked,
        // if the user have more than 10 points, for every 10 points,
        // 1 pound will be extracted from the total.
        bookingBinding.btnUsePoints.setOnClickListener {
            if (!pointsUsed) {
                if (userPoints >= 10) {
                    val discount = calculatePointsDiscount(userPoints)
                    val originalPrice = bookingBinding.etTotalPrice.text.toString().replace("£", "").toDouble()
                    val newPrice = originalPrice - discount
                    bookingBinding.etTotalPrice.text = String.format("%.2f£", newPrice)
                    usedPoints = (discount * 10).toLong()
                    Toast.makeText(this, "$usedPoints points used! You now have ${userPoints - usedPoints} points!", Toast.LENGTH_LONG).show()

                    pointsUsed = true // Mark points as used
                    bookingBinding.btnUsePoints.isEnabled = false // Disable the button
                } else {
                    Toast.makeText(this, "You don't have enough points! Points: $userPoints", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "You have already used your points!", Toast.LENGTH_LONG).show()
            }
        }

        // When Book button is click, the save booking function is executed.
        bookingBinding.btnBook.setOnClickListener {
            saveBooking(usedPoints)
        }
    }

    // This function take the barber object passed from Barbers Adapter,
    // fill the view of the Booking Activity with the barber information,
    // updating the services and time slots available for the selected barber
    private fun getAndSetData() {

        barber = intent.getParcelableExtra("barber")!!

        bookingBinding.bookingTbTitle.text = barber.name
        bookingBinding.rbBookingBarberRating.rating = barber.rating

        Picasso.get().load(barber.image.ifEmpty { getString(R.string.userImagePlaceHolder) })
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

        val timeSlotsAdapter = ArrayAdapter(this, R.layout.spinner_item, timeSlotsList)
        timeSlotsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        bookingBinding.spinnerTimeSlot.adapter = timeSlotsAdapter

        val services = barber.services
        val servicesList = arrayListOf<String>()

        for (service in services) {
            servicesList.add(service.name)

            val servicesAdapter = ArrayAdapter(this, R.layout.spinner_item, servicesList)
            servicesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            bookingBinding.spinnerService.adapter = servicesAdapter
            bookingBinding.spinnerService.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                @SuppressLint("SetTextI18n")
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedService = services[position]
                    val serviceTotal = selectedService.cost
                    bookingBinding.etTotalPrice.text = serviceTotal + "£"
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    bookingBinding.etTotalPrice.text = ""
                }
            }
        }
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        bookingBinding.etBookingPickDate.text = currentDate

        val availableTimeSlots = getAvailableTimeSlots(barber.bookings, currentDate)
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

    // This function check the available slots and remove the booked ones from the list,
    // so in the app will be displayed only available ones.
    private fun getAvailableTimeSlots(bookings: List<Bookings>, selectedDate: String): List<String> {

        val allTimeSlots = listOf(
            "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00",
            "16:00", "17:00", "18:00", "19:00", "20:00")

        val filteredBookings = bookings.filter { it.date == selectedDate }
        val bookedTimeSlots = filteredBookings.map { it.timeslot }

        return allTimeSlots.filterNot { bookedTimeSlots.contains(it) }
    }

    // This function update the available timeslots in the timeslots spinner.
    private fun updateTimeSlotsSpinner(availableTimeSlots: List<String>) {

        timeSlotsList = availableTimeSlots
        val timeSlotsAdapter = ArrayAdapter(this, R.layout.spinner_item, availableTimeSlots)
        timeSlotsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        bookingBinding.spinnerTimeSlot.adapter = timeSlotsAdapter
    }

    // This function open and set up the date picker. When a date is picked,
    // the edit text with the date and the time slot spinner will be updated,
    private fun pickDate() {

        val builder = MaterialDatePicker.Builder.datePicker()
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Date(selection)
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) // Change the format here
            val formattedDate = dateFormat.format(selectedDate)
            bookingBinding.etBookingPickDate.text = formattedDate

            val availableTimeSlots = getAvailableTimeSlots(barber.bookings, formattedDate)
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

    // This function will load the available offers
    // to be able to check the codes when the user enter them.
    private fun loadOffers(){
        FirebaseData.DBHelper.getOffersFromDatabase(object: FirebaseData.DBHelper.OffersCallback{
            override fun onSuccess(offers: ArrayList<Offers>) {
                offersList = offers
                Log.d("OffersLoaded", "Loaded offers: $offersList")
            }

            override fun onFailure(error: DatabaseError) {
                offersList = emptyList()
                Log.e("DatabaseError", error.details)
            }
        })
    }


    // This function check the offer code availability and calculate the total with the entered code.
    @SuppressLint("SetTextI18n")
    private fun applyDiscount(discountCode: String) {
        val selectedOffer = offersList.firstOrNull { it.code == discountCode }

        if (selectedOffer != null) {
            val originalPrice = bookingBinding.etTotalPrice.text.toString().replace("£", "").toDouble()
            val discountAmount = originalPrice * selectedOffer.deduction / 100
            val newPrice = originalPrice - discountAmount
            bookingBinding.etTotalPrice.text = String.format("%.2f£", newPrice)
            Toast.makeText(this, "Discount applied: ${selectedOffer.code}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Invalid discount code. Please try again.", Toast.LENGTH_SHORT).show()
            bookingBinding.etDiscountCode.setText("")
        }
    }


    // This function collect all the data entered by the user from the booking page,
    // arrange it in a booking object and save the booking under barber and user node.
    private fun saveBooking(usedPoints: Long = 0) {

        val userId = auth.currentUser?.uid ?: ""
        val barberId = barber.id
        val date = bookingBinding.etBookingPickDate.text.toString()
        val timeslot = bookingBinding.spinnerTimeSlot.selectedItem.toString()
        val service = bookingBinding.spinnerService.selectedItem.toString()
        val bookStatus = 0
        val discountCode = bookingBinding.etDiscountCode.text.toString().trim()
        val offer = discountCode.ifEmpty { "" }
        val totalPaid = bookingBinding.etTotalPrice.text.toString()

        val formattedTimeslot = timeslot.replace(":", "-")
        val pointsUsed = if (usedPoints > 0) "$offer,Points: $usedPoints" else offer

        val booking = Bookings(userId, barberId, date, timeslot, service, bookStatus, pointsUsed, totalPaid)

        // Save booking under Users
        FirebaseData.DBHelper.usersRef.child(userId).child("Bookings").child("$date-$formattedTimeslot").setValue(booking)
            .addOnSuccessListener {

                scheduleBookingReminder(date, timeslot, userId)
                scheduleFollowUpReminder(date, timeslot, userId)
                // Save booking under Barbers
                FirebaseData.DBHelper.barbersRef.child(barberId).child("Bookings").child("$date-$formattedTimeslot").setValue(booking)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "Booking saved successfully.", Toast.LENGTH_SHORT).show()
                        finish()

                        val totalPrice = bookingBinding.etTotalPrice.text.toString().replace("£", "").toDouble()

                        addPointsToUser(booking.userId, totalPrice,usedPoints)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@BookingActivity, "Failed to save booking!", Toast.LENGTH_SHORT).show()
                        e.localizedMessage?.let { it -> Log.e("DatabaseError: ", it) }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@BookingActivity, "Failed to save booking!", Toast.LENGTH_SHORT).show()
                e.localizedMessage?.let { Log.e("DatabaseError: ", it) }
            }
    }


    // This function calculate how many points user have, used and how many will get for the booking,
    // and update the points field under the current user node in the database.
    private fun addPointsToUser(userId: String, totalPrice: Double,usedPoints: Long = 0) {
        val pointsToAdd = (totalPrice * 0.10).roundToInt()

        FirebaseData.DBHelper.getCurrentUserFromDatabase(userId, object : FirebaseData.DBHelper.CurrentUserCallback {
            override fun onSuccess(currentUser: Users) {
                val currentPoints = currentUser.points
                val newPoints = currentPoints + pointsToAdd - usedPoints

                // Update the user's points
                FirebaseData.DBHelper.usersRef.child(userId).child("points").setValue(newPoints)
                    .addOnSuccessListener {
                        Log.d("PointsUpdate", "Successfully added $pointsToAdd points to user $userId.")
                        Toast.makeText(applicationContext, "You now have $newPoints points!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("PointsUpdateError", "Failed to add points to user $userId: ${e.localizedMessage}")
                    }
            }

            override fun onFailure(error: DatabaseError) {
                Log.e("UserNotFound", "User not found with ID: $userId, $error")
            }
        })
    }

    // This function retrieve the points of the current user from the database
    private fun getUserPoints() {
        val userId = auth.currentUser?.uid ?: ""
        FirebaseData.DBHelper.getCurrentUserFromDatabase(userId, object : FirebaseData.DBHelper.CurrentUserCallback {
            override fun onSuccess(currentUser: Users) {
                userPoints = currentUser.points

            }
            override fun onFailure(error: DatabaseError) {
                Log.e("DatabaseError", error.toString())
            }
        })
    }


    // This function calculate how much to subtract from the total based on user points.
    private fun calculatePointsDiscount(points: Long): Double {
        val pointsToUse = (points / 10) * 10
        return pointsToUse.toDouble() / 10
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

    // This function will schedule a notification to remind the user he has 1 hour left to the appointment.
    @SuppressLint("InlinedApi")
    private fun scheduleBookingReminder(date: String, timeslot: String, userId: String) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val appointmentDateTime = dateFormat.parse("$date $timeslot")

        if (appointmentDateTime != null) {
            val reminderDateTime = Calendar.getInstance().apply {
                time = appointmentDateTime

                add(Calendar.HOUR_OF_DAY, -1)
            }

            val intent = Intent(this, BookingReminderBroadcast::class.java).apply {
                putExtra("title", "Appointment Reminder")
                putExtra("message", "Your appointment is in 1 hour!")
            }

            val requestCode = userId.hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderDateTime.timeInMillis, pendingIntent)
        } else {
            Log.e("Error", "Failed to parse date and time for the reminder.")
        }
    }


    // This function will schedule a notification to remind the user
    // that he did not booked a new appointment in last 2 weeks.
    @SuppressLint("InlinedApi")
    private fun scheduleFollowUpReminder(date: String, timeslot: String, userId: String) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val appointmentDateTime = dateFormat.parse("$date $timeslot")

        if (appointmentDateTime != null) {
            val followUpDateTime = Calendar.getInstance().apply {
                time = appointmentDateTime

                add(Calendar.WEEK_OF_YEAR, 2)
            }

            val intent = Intent(this, FollowUpReminderBroadcast::class.java).apply {
                putExtra("title", "We Miss You!")
                putExtra("message", "It's been 2 weeks since your last appointment. Would you like to schedule a new one?")
            }

            val requestCode = (userId + "follow_up").hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, followUpDateTime.timeInMillis, pendingIntent)
        } else {
            Log.e("Error", "Failed to parse date and time for the follow-up reminder.")
        }
    }
}