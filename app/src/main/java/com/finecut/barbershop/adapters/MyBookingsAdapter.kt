package com.finecut.barbershop.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.finecut.barbershop.R
import com.finecut.barbershop.activities.AddReviewActivity
import com.finecut.barbershop.databinding.BookingsCardBinding
import com.finecut.barbershop.models.Barbers
import com.finecut.barbershop.models.Bookings
import com.finecut.barbershop.utils.FirebaseData
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

// This bookings adapter class take a list of bookings and populate the views of the bookings card
// in the recycler view of the My Bookings Activity.
class MyBookingsAdapter(private var context: Context,
    private var bookingsList: ArrayList<Bookings>
    ):RecyclerView.Adapter<MyBookingsAdapter.BookingsViewHolder>() {

    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    inner class BookingsViewHolder(val adapterBookings: BookingsCardBinding)
        : RecyclerView.ViewHolder(adapterBookings.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingsViewHolder {
        val binding = BookingsCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BookingsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingsViewHolder, position: Int) {

        val bookingDate = "${bookingsList[holder.adapterPosition].date}-${bookingsList[holder.adapterPosition].timeslot.replace(":", "-")}"

        val userBookingRef: DatabaseReference = firebaseDatabase.getReference("Users")
            .child(bookingsList[holder.adapterPosition].userId)
            .child("Bookings").child(bookingDate)

        val barberBookingRef: DatabaseReference = firebaseDatabase.getReference("Barbers")
            .child(bookingsList[holder.adapterPosition].barberId)
            .child("Bookings").child(bookingDate)

        // This block of code retrieve the selected barber from the database
        // and populate the views of the booking card.
        // Depending on the booking status data is displayed accordingly
        FirebaseData.DBHelper.getBarberFromDatabase(bookingsList[holder.adapterPosition].barberId, object: FirebaseData.DBHelper.BarberCallback{
            @SuppressLint("SetTextI18n")
            override fun onSuccess(barber: Barbers) {

                holder.adapterBookings.ivBarberImage.visibility = View.VISIBLE
                holder.adapterBookings.tvMyBookingsBarberName.visibility = View.VISIBLE
                holder.adapterBookings.tvMyBookingsDateTime.visibility = View.VISIBLE
                holder.adapterBookings.tvMyBookingsServiceName.visibility = View.VISIBLE
                holder.adapterBookings.tvMyBookingsAmountPaid.visibility = View.VISIBLE
                holder.adapterBookings.pbBookingCard.visibility = View.GONE

                holder.adapterBookings.tvMyBookingsDateTime.text = "${bookingsList[holder.adapterPosition].date}-${bookingsList[holder.adapterPosition].timeslot}"
                holder.adapterBookings.tvMyBookingsServiceName.text = bookingsList[holder.adapterPosition].service
                holder.adapterBookings.tvMyBookingsAmountPaid.text = bookingsList[holder.adapterPosition].totalPaid

                holder.adapterBookings.tvMyBookingsBarberName.text = barber.name

                when(bookingsList[holder.adapterPosition].bookStatus){

                    0 -> {holder.adapterBookings.tvBookingStatus.text = "Booked"}
                    1 -> {
                        holder.adapterBookings.tvBookingStatus.text = "Confirmed"
                        holder.adapterBookings.tvBookingStatus.setTextColor(ContextCompat.getColor(context, R.color.green))

                    }
                    2 -> {
                        holder.adapterBookings.btnCancelBooking.visibility = View.GONE
                        holder.adapterBookings.tvBookingStatus.text = "Completed"
                        holder.adapterBookings.tvBookingStatus.setTextColor(ContextCompat.getColor(context, R.color.salmon_pink))
                    }
                }

                Picasso.get().load(barber.image.ifEmpty { context.getString(R.string.userImagePlaceHolder)})
                    .into(holder.adapterBookings.ivBarberImage)

                holder.adapterBookings.btnCancelBooking.setOnClickListener {

                    val alertDialogBuilder = AlertDialog.Builder(context)
                    alertDialogBuilder.setTitle("Cancel Booking")
                    alertDialogBuilder.setMessage("Are you sure you want to cancel this booking?")

                    alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                        userBookingRef.removeValue().addOnSuccessListener {
                            Toast.makeText(
                                context.applicationContext,
                                "Booking canceled!",
                                Toast.LENGTH_SHORT
                            ).show()

                            bookingsList.removeAt(holder.adapterPosition)
                            notifyItemRemoved(holder.adapterPosition)

                        }.addOnFailureListener {
                            it.localizedMessage?.let { it2 -> Log.e("Database Error:", it2) }
                        }

                        barberBookingRef.removeValue()
                    }
                    alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()

                }

                if(bookingsList[holder.adapterPosition].bookStatus == 2){

                    holder.adapterBookings.llBookingsCard.setOnClickListener {

                        val intent = Intent(context,AddReviewActivity::class.java)
                        intent.putExtra("barber",barber)
                        context.startActivity(intent)
                    }
                }
            }

            override fun onFailure(error: DatabaseError) {
                Log.e("Database Error: ", error.details)
            }
        })
    }

    override fun getItemCount(): Int {
        return bookingsList.size
    }
}