package com.finecut.barbershop.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.finecut.barbershop.databinding.ActivityReviewsBinding

class ReviewsActivity : AppCompatActivity() {

    private lateinit var reviewsBinding: ActivityReviewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reviewsBinding = ActivityReviewsBinding.inflate(layoutInflater)
        val view = reviewsBinding.root
        setContentView(view)
    }
}