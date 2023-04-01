package com.finecut.barbershop.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.finecut.barbershop.R
import com.finecut.barbershop.databinding.ActivityAddReviewBinding
import com.finecut.barbershop.models.Barbers
import com.finecut.barbershop.models.Reviews
import com.finecut.barbershop.utils.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class AddReviewActivity : BaseActivity() {

    private lateinit var addReviewBinding: ActivityAddReviewBinding

    private lateinit var barber: Barbers

    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        addReviewBinding = ActivityAddReviewBinding.inflate(layoutInflater)
        val view = addReviewBinding.root
        setContentView(view)

        setupActionAndSideMenuBar(this,addReviewBinding.tbAddReview,true,view)
        getAndSetData()

        addReviewBinding.btnAddReviewSave.setOnClickListener {
            saveReview()
        }
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

    @SuppressLint("SetTextI18n")
    private fun getAndSetData(){

        barber = intent.getParcelableExtra("barber")!!
        Log.e("barberData", barber.toString())

        addReviewBinding.addReviewTbTitle.text = "Rate ${barber.name}"
        addReviewBinding.tvAddReviewBarberName.text = barber.name

        Picasso.get().load(barber.image.ifEmpty { getString(R.string.userImagePlaceHolder) })
            .into(addReviewBinding.ivAddReviewBarberImage, object :
                Callback {
                override fun onSuccess() {
                    addReviewBinding.pbAddReviewImage.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    if (e != null) {
                        e.localizedMessage?.let { Log.e("Error Loading Image: ", it) }
                    }
                }
            })
    }

    private fun calculateAverageRating(newRating: Float): Float {
        val reviewsList = ArrayList<Reviews>()
        val ratingsList = ArrayList<Float>()
        var sum = 0F
        for (review in barber.reviews){
            reviewsList.add(review)
        }
        for (review in reviewsList) {
            ratingsList.add(review.rating)
        }

        ratingsList.add(newRating)
        for (rating in ratingsList) {
            sum += rating
        }
        return sum / ratingsList.size
    }


    private fun saveReview(){

        val barberReviewsRef: DatabaseReference = firebaseDatabase.getReference("Barbers").child(barber.id).child("reviews")
        val barberRatingRef: DatabaseReference = firebaseDatabase.getReference("Barbers").child(barber.id).child("rating")

        val userId = auth.currentUser?.uid ?: ""
        val rating = addReviewBinding.rbAddReviewBarberRating.rating
        val comment = addReviewBinding.etAddReviewComment.text.toString()

        val review = Reviews(userId,rating,comment)
        val ratingAverage = calculateAverageRating(rating)

            barberReviewsRef.child(userId).setValue(review).addOnSuccessListener {

                barberRatingRef.setValue(ratingAverage).addOnFailureListener {e ->

                    Toast.makeText(applicationContext, "Failed to save the average rating!", Toast.LENGTH_SHORT).show()
                    e.localizedMessage?.let { it -> Log.e("DatabaseError: ", it) }
                }

                Toast.makeText(applicationContext, "Review saved successfully.", Toast.LENGTH_SHORT).show()

                finish()

                }.addOnFailureListener { e ->

                Toast.makeText(applicationContext, "Failed to save the review!", Toast.LENGTH_SHORT).show()
                e.localizedMessage?.let { it -> Log.e("DatabaseError: ", it) }
            }
    }

}