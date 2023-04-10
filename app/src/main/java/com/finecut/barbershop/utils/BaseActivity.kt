package com.finecut.barbershop.utils

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
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
import com.finecut.barbershop.BuildConfig
import com.finecut.barbershop.R
import com.finecut.barbershop.activities.*
import com.finecut.barbershop.models.Users
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

// This open class can be inherited by any activity withing this project.
// it contains the most important functions of the application,
// and has the purpose to maintain a clean code architecture.
open class BaseActivity : AppCompatActivity() {

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var auth: FirebaseAuth

    // This function will be one of the first functions used in every activity.
    // This function set up the action bar and the side menu layout
    // that will be displayed if the hamburger menu button is clicked
    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    protected fun setupActionAndSideMenuBar(
        context: Context,
        toolbar: Toolbar,
        backButton: Boolean,
        rootView: FrameLayout
    ) {
        // Next lines of code set up the action bar with the back and hamburger button.
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

        // Next lines of code setup the side menu (drawer menu) to be displayed on 70% of the screen
        // and under action bar. If the hamburger menu is clicked the menu drawer is displayed with
        // an animation from right to left. If the hamburger button is clicked again or
        // the user click next to the menu, will close with an animation from left to right.
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

        // This block of code update the views in the menu layout with the authenticated user details.
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

        // If My Profile button is clicked, the menu drawer will close,
        // and the user will be redirected to the My Profile Activity.
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

        // If My Bookings button is clicked, is checked if the user is already in the
        // My bookings activity and if is not, the menu drawer will close,
        // and the user will be redirected to the My Bookings Activity.
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

        // If Offers button is clicked, is checked if the user is already in the offers activity and
        // if is not, the menu drawer will close, and the user will be redirected to the Offers.
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

    // If the Rate Us button is clicked, google maps with the barber place will be opened in the
    // google map app or if the user do not have google maps installed it will open in the browser.
        btnRateUs.setOnClickListener {
            val apiKey = BuildConfig.PLACES_API_KEY
            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, apiKey)
            }

            val placesClient = Places.createClient(this)

            val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

            val placeId = "ChIJzdK8Dz8bdkgRj698y3CmyZo"
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    val location = place.latLng
                    if (location != null) {
                        val gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}&query_place_id=$placeId")

                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        startActivity(mapIntent)
                    } else {
                        Log.e("Place Error:", "Place not found: LatLng is null")
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Place Error:", "Place not found: ${exception.message}")
                }
        }

        // If Rate App button is clicked, google play store will open with the application page.
        btnRateApp.setOnClickListener {

            val packageName = applicationContext.packageName
            val uri = Uri.parse("market://details?id=$packageName")
            val goToMarketIntent = Intent(Intent.ACTION_VIEW, uri)

            try {
                startActivity(goToMarketIntent)
            } catch (e: ActivityNotFoundException) {
                val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                val browserIntent = Intent(Intent.ACTION_VIEW, webUri)
                startActivity(browserIntent)
            }
        }

        // If the user click Sign Out button, will be signed out and redirected to the Log In Page.
        btnLogOut.setOnClickListener {

            auth.signOut()
            context.startActivity(Intent(context, LogInActivity::class.java))
            finish()
        }
    }
}

