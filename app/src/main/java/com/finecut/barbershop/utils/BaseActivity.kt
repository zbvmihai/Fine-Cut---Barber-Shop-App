package com.finecut.barbershop.utils

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.finecut.barbershop.R
import com.finecut.barbershop.activities.*
import com.finecut.barbershop.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso


open class BaseActivity : AppCompatActivity() {

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private lateinit var auth: FirebaseAuth

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    protected fun setupActionAndSideMenuBar(
        context: Context,
        toolbar: Toolbar,
        backButton: Boolean,
        rootView: FrameLayout
    ) {
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(backButton)
        supportActionBar?.setHomeAsUpIndicator(
            ContextCompat.getDrawable(
                this,
                R.drawable.back_button
            )
        )
        supportActionBar?.setDisplayShowCustomEnabled(true)
        val hamburgerButton = ImageButton(context)
        hamburgerButton.setImageResource(R.drawable.ic_hamburger)
        hamburgerButton.background =
            ResourcesCompat.getDrawable(resources, android.R.color.transparent, null)

        val layoutParams = ActionBar.LayoutParams(
            ActionBar.LayoutParams.WRAP_CONTENT,
            ActionBar.LayoutParams.MATCH_PARENT,
            Gravity.END or Gravity.CENTER_VERTICAL
        ).apply {
            rightMargin = (16 * resources.displayMetrics.density).toInt()
        }

        supportActionBar?.setCustomView(hamburgerButton, layoutParams)

        val drawerContainer = FrameLayout(this)

        val drawerView =
            LayoutInflater.from(this).inflate(R.layout.drawer_layout, drawerContainer, false)
        drawerContainer.addView(drawerView)

        val screenWidth = resources.displayMetrics.widthPixels
        drawerView.layoutParams.width = screenWidth

        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
        val actionBarHeight =
            TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)

        val windowHeight = resources.displayMetrics.heightPixels
        val drawerHeight = windowHeight - actionBarHeight

        val constraintLayoutWidth = (0.7 * screenWidth).toInt()
        val constraintLayout = drawerView.findViewById<ConstraintLayout>(R.id.clDrawer)
        val constraintLayoutParams = constraintLayout.layoutParams
        constraintLayoutParams.width = constraintLayoutWidth
        constraintLayout.layoutParams = constraintLayoutParams

        val drawerParams = FrameLayout.LayoutParams(
            constraintLayoutWidth, drawerHeight
        )
        drawerParams.gravity = Gravity.BOTTOM
        drawerContainer.layoutParams = drawerParams

        drawerContainer.translationX = screenWidth.toFloat()

        val overlayView = FrameLayout(context)
        overlayView.setBackgroundColor(Color.TRANSPARENT)

        val overlayLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        rootView.addView(overlayView, overlayLayoutParams)
        overlayView.visibility = View.GONE

        rootView.addView(drawerContainer)

        val animOpen = ObjectAnimator.ofFloat(
            drawerContainer,
            "translationX",
            screenWidth.toFloat(),
            screenWidth.toFloat() - constraintLayoutWidth
        )
        animOpen.duration = 300

        val animClose = ObjectAnimator.ofFloat(
            drawerContainer,
            "translationX",
            screenWidth.toFloat() - constraintLayoutWidth,
            screenWidth.toFloat()
        )
        animClose.duration = 300

        var isDrawerOpen = false

        drawerContainer.setOnTouchListener { _, _ ->
            isDrawerOpen
        }

        overlayView.setOnTouchListener { _, event ->
            if (isDrawerOpen && event.action == MotionEvent.ACTION_DOWN) {
                animClose.start()
                isDrawerOpen = false
                overlayView.visibility = View.GONE
                onBackPressedCallback.isEnabled = false
            }
            true
        }

        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (isDrawerOpen) {
                    animClose.start()
                    isDrawerOpen = false
                    overlayView.visibility = View.GONE
                    isEnabled = false
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        hamburgerButton.setOnClickListener {
            if (isDrawerOpen) {
                animClose.start()
                overlayView.visibility = View.GONE
            } else {
                animOpen.start()
                overlayView.visibility = View.VISIBLE
            }
            isDrawerOpen = !isDrawerOpen
            onBackPressedCallback.isEnabled = isDrawerOpen
        }

        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isDrawerOpen) {
                val drawerRect = Rect()
                drawerContainer.getHitRect(drawerRect)
                if (!drawerRect.contains(event.x.toInt(), event.y.toInt())) {
                    animClose.start()
                    isDrawerOpen = false
                }
            } else if (event.action == MotionEvent.ACTION_DOWN && !isDrawerOpen) {
                val overlayRect = Rect()
                overlayView.getHitRect(overlayRect)
                if (overlayRect.contains(event.x.toInt(), event.y.toInt())) {
                    return@setOnTouchListener true
                }
            }
            false
        }

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid!!

        FirebaseData.DBHelper.getCurrentUserFromDatabase(userId, object :
            FirebaseData.DBHelper.CurrentUserCallback {
            override fun onSuccess(currentUser: Users) {

                val userName = drawerView.findViewById<TextView>(R.id.tvUserName)
                val userImage = drawerView.findViewById<ImageView>(R.id.ivUserProfileImage)

                userName.text = "${currentUser.firstName} ${currentUser.surname}"

                Picasso.get().load(currentUser.image.ifEmpty { getString(R.string.userImagePlaceHolder) })
                    .into(userImage, object :
                        Callback {
                        override fun onSuccess() {
                            val userImageProgressBar = drawerView.findViewById<ProgressBar>(R.id.pbUserImageDrawer)
                            userImageProgressBar.visibility = View.GONE
                        }
                        override fun onError(e: Exception?) {
                            if (e != null) {
                                e.localizedMessage?.let { Log.e("Error Loading Image: ", it) }
                            }
                        }
                    })
            }

            override fun onFailure(error: DatabaseError) {
                Log.e("Database Error: ", error.toString())
            }
        })

        val btnMyProfile = drawerView.findViewById<Button>(R.id.btnMyProfile)
        val btnMyBookings = drawerView.findViewById<Button>(R.id.btnMyBookings)
        val btnOffers = drawerView.findViewById<Button>(R.id.btnOffers)
        val btnRateUs = drawerView.findViewById<Button>(R.id.btnRateUs)
        val btnRateApp = drawerView.findViewById<Button>(R.id.btnRateApp)
        val btnLogOut = drawerView.findViewById<Button>(R.id.btnLogOut)

        btnMyProfile.setOnClickListener {
            context.startActivity(Intent(context,MyProfileActivity::class.java))
            animClose.start()
            isDrawerOpen = false
            overlayView.visibility = View.GONE
            onBackPressedCallback.isEnabled = false
            if (this::class.java != MainActivity::class.java) {
                finish()
            }
        }

        btnMyBookings.setOnClickListener {
            if (this::class.java == MyBookingsActivity::class.java) {
                Toast.makeText(applicationContext,"You are already in My Bookings",Toast.LENGTH_SHORT).show()
            }else{
                startActivity(Intent(this, MyBookingsActivity::class.java))
                animClose.start()
                isDrawerOpen = false
                overlayView.visibility = View.GONE
                onBackPressedCallback.isEnabled = false
                if (this::class.java != MainActivity::class.java) {
                    finish()
                }
            }
        }

        btnOffers.setOnClickListener {
            if (this::class.java == OffersActivity::class.java) {
                Toast.makeText(applicationContext,"You are already in Offers",Toast.LENGTH_SHORT).show()
            }else{
                startActivity(Intent(this, OffersActivity::class.java))
                animClose.start()
                isDrawerOpen = false
                overlayView.visibility = View.GONE
                onBackPressedCallback.isEnabled = false
                if (this::class.java != MainActivity::class.java) {
                    finish()
                }
            }
        }

        btnRateUs.setOnClickListener {
            Toast.makeText(applicationContext, "Rate Us clicked", Toast.LENGTH_SHORT).show()
        }

        btnRateApp.setOnClickListener {
            Toast.makeText(applicationContext, "Rate App clicked", Toast.LENGTH_SHORT).show()
        }

        btnLogOut.setOnClickListener {

            auth.signOut()

            context.startActivity(Intent(context, LogInActivity::class.java))
            finish()
        }
    }
}

