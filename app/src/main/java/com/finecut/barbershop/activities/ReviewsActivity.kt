package com.finecut.barbershop.activities

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.finecut.barbershop.R
import com.finecut.barbershop.adapters.ReviewsAdapter
import com.finecut.barbershop.databinding.ActivityReviewsBinding
import com.finecut.barbershop.models.Barbers
import com.finecut.barbershop.models.Reviews
import com.finecut.barbershop.utils.BaseActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class ReviewsActivity : BaseActivity() {

    private lateinit var reviewsBinding: ActivityReviewsBinding

    private lateinit var reviewsAdapter: ReviewsAdapter

    private lateinit var barber: Barbers
    private var reviewsList: ArrayList<Reviews> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reviewsBinding = ActivityReviewsBinding.inflate(layoutInflater)
        val view = reviewsBinding.root
        setContentView(view)

        setupActionAndSideMenuBar(this,reviewsBinding.tbReviews,true,view)

        // Next lines of code takes the barber object passed from the selected barber
        // in the recycler view, will fill the views in the Reviews activity,
        // the reviews of the selected barber will be passed to the reviews adapter,
        // and the reviews recycler view will display all the reviews of the barber.
        barber = intent.getParcelableExtra("barber")!!

        reviewsBinding.reviewsTbTitle.text = barber.name
        reviewsBinding.rbReviewsBarberRating.rating = barber.rating

        Picasso.get().load(barber.image.ifEmpty { getString(R.string.userImagePlaceHolder) })
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

        for (review in barber.reviews) {
            reviewsList.add(review)
        }

        reviewsAdapter = ReviewsAdapter(this@ReviewsActivity, reviewsList)
        reviewsBinding.rvReviews.layoutManager = LinearLayoutManager(this@ReviewsActivity)
        reviewsBinding.rvReviews.adapter = reviewsAdapter
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
