package com.finecut.barbershop.utils

import com.finecut.barbershop.models.*
import com.google.firebase.database.*

class FirebaseData {

    object DBHelper {

        private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val usersRef: DatabaseReference = firebaseDatabase.getReference("Users")
        val barbersRef: DatabaseReference = firebaseDatabase.getReference("Barbers")
        private val offersRef: DatabaseReference = firebaseDatabase.getReference("Offers")


        interface CurrentUserCallback {
            fun onSuccess(currentUser: Users)
            fun onFailure(error: DatabaseError)
        }

        fun getCurrentUserFromDatabase(userId: String, callback: CurrentUserCallback) {
            usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val id = snapshot.child("id").getValue(String::class.java) ?: ""
                    val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                    val surname = snapshot.child("surname").getValue(String::class.java) ?: ""
                    val email = snapshot.child("email").getValue(String::class.java) ?: ""
                    val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                    val isClient = snapshot.child("client").getValue(Int::class.java) ?: 1
                    val image = snapshot.child("image").getValue(String::class.java) ?: ""
                    val bookings = snapshot.child("bookings").children.mapNotNull { it.getValue(Bookings::class.java) }
                    val points = snapshot.child("points").getValue(Long::class.java) ?: 0

                    val user = Users(id, firstName, surname, email, phoneNumber, isClient, image, bookings, points)

                    if (id.isNotEmpty()) {
                        callback.onSuccess(user)
                    } else {
                        callback.onFailure(DatabaseError.fromException(Exception("User not found")))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback.onFailure(error)
                }
            })
        }

        interface BarberCallback {
            fun onSuccess(barber: Barbers)
            fun onFailure(error: DatabaseError)
        }

        fun getBarberFromDatabase(barberId: String, callback: BarberCallback) {
            barbersRef.child(barberId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val id = snapshot.child("id").getValue(String::class.java) ?: ""
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val image = snapshot.child("image").getValue(String::class.java) ?: ""
                    val rating = snapshot.child("rating").getValue(Float::class.java) ?: 0F
                    val description = snapshot.child("description").getValue(String::class.java) ?: ""

                    val services = snapshot.child("services").children.mapNotNull { it.getValue(Services::class.java) }
                    val bookings = snapshot.child("bookings").children.mapNotNull { it.getValue(Bookings::class.java) }
                    val reviews = snapshot.child("reviews").children.mapNotNull { it.getValue(Reviews::class.java)}

                    val barber = Barbers(id, name, image, rating, description, services, bookings, reviews)

                    callback.onSuccess(barber)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback.onFailure(error)
                }
            })
        }
        
        interface BarbersCallback {
            fun onSuccess(barbersList: ArrayList<Barbers>)
            fun onFailure(error: DatabaseError)
        }

        fun getBarbersFromDatabase(callback: BarbersCallback) {
            barbersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val barbersList = ArrayList<Barbers>()

                    for (barberSnapshot in snapshot.children) {
                        val id = barberSnapshot.child("id").getValue(String::class.java) ?: ""
                        val name = barberSnapshot.child("name").getValue(String::class.java) ?: ""
                        val image = barberSnapshot.child("image").getValue(String::class.java) ?: ""
                        val rating = barberSnapshot.child("rating").getValue(Float::class.java) ?: 0F
                        val description = barberSnapshot.child("description").getValue(String::class.java) ?: ""

                        val services = barberSnapshot.child("services").children.mapNotNull { it.getValue(Services::class.java) }
                        val bookings = barberSnapshot.child("Bookings").children.mapNotNull { it.getValue(Bookings::class.java) }
                        val reviews = barberSnapshot.child("reviews").children.mapNotNull { it.getValue(Reviews::class.java)}

                        val barber = Barbers(id, name, image, rating, description, services, bookings, reviews)
                        barbersList.add(barber)
                    }

                    callback.onSuccess(barbersList)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback.onFailure(error)
                }
            })
        }

        interface BookingsCallback {
            fun onSuccess(bookingsList: ArrayList<Bookings>)
            fun onFailure(error: DatabaseError)
        }

        fun getBookingsFromDatabase(userId: String,callback: BookingsCallback) {
            val userBookingsRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Bookings")
            userBookingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val bookingsList = ArrayList<Bookings>()
                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(Bookings::class.java)
                        if (booking != null) {
                            bookingsList.add(booking)
                        }
                    }
                    callback.onSuccess(bookingsList)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback.onFailure(error)
                }
            })
        }

        interface OffersCallback {
            fun onSuccess(offers: ArrayList<Offers>)
            fun onFailure(error: DatabaseError)
        }

        fun getOffersFromDatabase(callback: OffersCallback) {

            offersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val offers = ArrayList<Offers>()
                    for (offerSnapshot in snapshot.children) {
                        val offer = offerSnapshot.getValue(Offers::class.java)
                        if (offer != null) {
                            offers.add(offer)
                        }
                    }
                    callback.onSuccess(offers)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback.onFailure(error)
                }
            })
        }
    }
}

