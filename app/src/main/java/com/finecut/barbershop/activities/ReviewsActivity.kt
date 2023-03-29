package com.finecut.barbershop.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.finecut.barbershop.R
import com.finecut.barbershop.adapters.ReviewsAdapter
import com.finecut.barbershop.databinding.ActivityReviewsBinding
import com.finecut.barbershop.models.Barbers
import com.finecut.barbershop.models.Reviews
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class ReviewsActivity : AppCompatActivity() {

    private lateinit var reviewsBinding: ActivityReviewsBinding

    private lateinit var reviewsAdapter: ReviewsAdapter

    private lateinit var barber: Barbers
    private var reviewsList: ArrayList<Reviews> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reviewsBinding = ActivityReviewsBinding.inflate(layoutInflater)
        val view = reviewsBinding.root
        setContentView(view)

        setupActionBar()
        barber = intent.getParcelableExtra("barber")!!

        reviewsBinding.reviewsTbTitle.text = barber.name
        reviewsBinding.rbReviewsBarberRating.rating = barber.rating

        Picasso.get().load(barber.image)
            .into(reviewsBinding.ivReviewsBarberImage, object :
                Callback {
                override fun onSuccess() {
                    reviewsBinding.pbReviewsBarberImage.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    if (e != null) {
                        e.localizedMessage?.let { Log.e("Error Loading Image: ", it) }
                    }
                }
            })

        for (review in barber.reviews){
            reviewsList.add(review)
        }

        reviewsAdapter = ReviewsAdapter(this@ReviewsActivity,reviewsList)
        reviewsBinding.rvReviews.layoutManager = LinearLayoutManager(this@ReviewsActivity)
        reviewsBinding.rvReviews.adapter = reviewsAdapter
    }

    private fun setupActionBar() {
        setSupportActionBar(reviewsBinding.tbReviews)

        val actionBar = supportActionBar

        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.back_button))
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