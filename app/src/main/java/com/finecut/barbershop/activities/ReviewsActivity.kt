package com.finecut.barbershop.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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

        for (review in barber.reviews) {
            reviewsList.add(review)
        }

        reviewsAdapter = ReviewsAdapter(this@ReviewsActivity, reviewsList)
        reviewsBinding.rvReviews.layoutManager = LinearLayoutManager(this@ReviewsActivity)
        reviewsBinding.rvReviews.adapter = reviewsAdapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupActionBar() {
        setSupportActionBar(reviewsBinding.tbReviews)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(
            ContextCompat.getDrawable(
                this,
                R.drawable.back_button
            )
        )
        supportActionBar?.setDisplayShowCustomEnabled(true)
        val hamburgerButton = ImageButton(this)
        hamburgerButton.setImageResource(R.drawable.ic_hamburger)
        hamburgerButton.background = ResourcesCompat.getDrawable(resources, android.R.color.transparent, null)

        val layoutParams = ActionBar.LayoutParams(
            ActionBar.LayoutParams.WRAP_CONTENT,
            ActionBar.LayoutParams.MATCH_PARENT,
            Gravity.END or Gravity.CENTER_VERTICAL
        )

        supportActionBar?.setCustomView(hamburgerButton, layoutParams)

        val drawerContainer = FrameLayout(this)

        val drawerView =
            LayoutInflater.from(this).inflate(R.layout.drawer_layout, drawerContainer, false)
        drawerContainer.addView(drawerView)

        val screenWidth = resources.displayMetrics.widthPixels
        val drawerWidth = (0.7 * screenWidth).toInt()
        drawerView.layoutParams.width = drawerWidth

        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
        val actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)

        val windowHeight = resources.displayMetrics.heightPixels
        val drawerHeight = windowHeight - actionBarHeight

        val drawerParams = FrameLayout.LayoutParams(
            drawerWidth, drawerHeight
        )
        drawerParams.gravity = Gravity.BOTTOM or Gravity.END
        drawerContainer.layoutParams = drawerParams

        drawerContainer.translationX = screenWidth.toFloat()

        reviewsBinding.root.addView(drawerContainer)

        val animOpen = AnimatorSet()
        val translationYOpen = ObjectAnimator.ofFloat(drawerContainer, "translationY", windowHeight.toFloat() - drawerHeight)
        val translationXOpen = ObjectAnimator.ofFloat(drawerContainer, "translationX", screenWidth.toFloat() - drawerWidth)
        animOpen.play(translationYOpen).before(translationXOpen)
        animOpen.duration = 300

        val animClose = AnimatorSet()
        val translationXClose = ObjectAnimator.ofFloat(drawerContainer, "translationX", screenWidth.toFloat())
        animClose.play(translationXClose)
        animClose.duration = 300

        var isDrawerOpen = false

        reviewsBinding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isDrawerOpen) {
                val drawerRect = Rect()
                drawerContainer.getHitRect(drawerRect)
                if (!drawerRect.contains(event.x.toInt(), event.y.toInt())) {
                    animClose.start()
                    isDrawerOpen = false
                }
            }
            false
        }

        hamburgerButton.setOnClickListener {
            if (isDrawerOpen) {
                animClose.start()
            } else {
                animOpen.start()
            }
            isDrawerOpen = !isDrawerOpen
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
}
