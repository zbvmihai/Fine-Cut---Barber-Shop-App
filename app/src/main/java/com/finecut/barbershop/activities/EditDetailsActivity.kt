package com.finecut.barbershop.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.finecut.barbershop.R
import com.finecut.barbershop.databinding.ActivityEditDetailsBinding
import com.finecut.barbershop.models.Users
import com.finecut.barbershop.utils.BaseActivity
import com.finecut.barbershop.utils.FirebaseData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

class EditDetailsActivity : BaseActivity() {

    private lateinit var editDetailsBinding: ActivityEditDetailsBinding
    private var auth = FirebaseAuth.getInstance()
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference : StorageReference = firebaseStorage.reference
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private var currentUserId: String? = null

    private var imageUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editDetailsBinding = ActivityEditDetailsBinding.inflate(layoutInflater)
        val view = editDetailsBinding.root
        setContentView(view)

        auth.currentUser?.uid.let { currentUserID->
            currentUserId = currentUserID
        }

        setupActionAndSideMenuBar(this,editDetailsBinding.tbEditDetails,true,view)

        registerActivityForResult()

        // This block of code will retrieve the authenticated user details from the database
        // and fill the views of the Edit Details activity with the user data
        FirebaseData.DBHelper.getCurrentUserFromDatabase(currentUserId!!, object: FirebaseData.DBHelper.CurrentUserCallback{
            @SuppressLint("SetTextI18n")
            override fun onSuccess(currentUser: Users) {

                editDetailsBinding.tvEditDetailsUserName.visibility = View.VISIBLE
                editDetailsBinding.tvEditDetailsUserName.text = "${currentUser.firstName} ${currentUser.surname}"

                editDetailsBinding.etFirstNameEditDetails.setText(currentUser.firstName)
                editDetailsBinding.etSurnameEditDetails.setText(currentUser.surname)
                editDetailsBinding.etPhoneEditDetails.setText(currentUser.phoneNumber)

                Picasso.get().load(currentUser.image.ifEmpty { getString(R.string.userImagePlaceHolder)})
                    .into(editDetailsBinding.ivEditDetailsUserImage, object : Callback {
                        override fun onSuccess() {
                            editDetailsBinding.pbEditDetails.visibility = View.GONE
                        }
                        override fun onError(e: Exception?) {
                            if (e != null) {
                                e.localizedMessage?.let { Log.e("Load Image Error:" , it) }
                            }
                        }
                    })
            }
            override fun onFailure(error: DatabaseError) {
                Log.e("Database Error: ", error.toString())
            }
        })

        // When the user image view is clicked the image picker will
        // and the user will be able to pick an image from the gallery to update the user profile image
        editDetailsBinding.ivEditDetailsUserImage.setOnClickListener{
            chooseImage()
        }

        // When the Save button is clicked, all the changed information
        // will be updated under authenticated user node in database
        editDetailsBinding.btnEditDetailsSave.setOnClickListener {

            val firstName = editDetailsBinding.etFirstNameEditDetails.text.toString()
            val surname = editDetailsBinding.etSurnameEditDetails.text.toString()
            val phoneNumber = editDetailsBinding.etPhoneEditDetails.text.toString()
            val password = editDetailsBinding.etEditDetailsPassword.text.toString()
            val confirmPassword = editDetailsBinding.etEditDetailsConfirmPassword.text.toString()

            if (imageUri != null) {
                uploadPhoto()
            }

            validateForm(firstName,surname,phoneNumber)
            editInfo()

            if (password.isNotEmpty() && confirmPassword.isNotEmpty()){

                if(isUserRecentlyLoggedIn()){
                    if(verifyPassword(password,confirmPassword)){

                        auth.currentUser?.updatePassword(password)?.addOnSuccessListener {
                            Toast.makeText(applicationContext,"Password has been changed successfully",Toast.LENGTH_SHORT).show()
                        }?.addOnFailureListener {
                            Toast.makeText(applicationContext,"Failed to change the password",Toast.LENGTH_SHORT).show()
                            Log.e("Password Update Error: ",it.toString())
                        }
                    }else {
                        if (password != confirmPassword) {
                            Toast.makeText(applicationContext, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "Password must be 8 characters long and contain at least 1 number and a capital letter", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setMessage("You need to be recently logged in to be able to change your password. Please Log Out and Log In Again. Click yes to Log Out!")
                        .setCancelable(true)
                        .setPositiveButton("Yes") { _, _ ->

                            FirebaseAuth.getInstance().signOut()
                            val intent = Intent(this, LogInActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }
            }
        }
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

    // This function will check if the application have access to the phone storage,
    // if he don't have access, first will ask the user to grant access, if it has access
    // it will open the image picker and the user can select a picture from the gallery.
    private fun chooseImage() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
                )
            } else {

                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                activityResultLauncher.launch(intent)

            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 2
                )
            } else {

                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                activityResultLauncher.launch(intent)
            }
        }
    }

    // This overridden function check the permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            1,2 -> if (grantResults.isNotEmpty() && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                activityResultLauncher.launch(intent)
            }else {
                Toast.makeText(
                    applicationContext,
                    "Permissions Denied. Go to application settings to enable it.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // This function populate the image view with the selected image in the image picker.
    private fun registerActivityForResult() {

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val resultCode = result.resultCode
            val imageData = result.data

            if (resultCode == RESULT_OK && imageData != null) {

                imageUri = imageData.data
                imageUri?.let {

                    Picasso.get().load(it).into(editDetailsBinding.ivEditDetailsUserImage)
                }
            }
        }
    }

    // This function save the image in the cloud storage,
    // and set the image url value in the authenticated user node of the database
    private fun uploadPhoto(){

        val imageReference = storageReference.child("ProfileImages").child(currentUserId!!)

        imageUri?.let { uri ->
            imageReference.putFile(uri).addOnSuccessListener {

                Toast.makeText(applicationContext,"Image Uploaded!",Toast.LENGTH_SHORT).show()

                val myUploadedImageReference = storageReference.child("ProfileImages").child(currentUserId!!)

                myUploadedImageReference.downloadUrl.addOnSuccessListener { url->

                    val imageURL = url.toString()
                    val userImageReference = database.reference.child("Users").child(currentUserId!!).child("image")

                    userImageReference.setValue(imageURL).addOnSuccessListener {

                        Toast.makeText(applicationContext,"User Profile Image Updated",Toast.LENGTH_SHORT).show()

                    }.addOnFailureListener {

                        Toast.makeText(applicationContext,"Failed to Update User Profile Image",Toast.LENGTH_SHORT).show()
                        Log.e("Database Error: ", it.toString())
                    }
                }
            }.addOnFailureListener{

                Toast.makeText(applicationContext,"Failed to Upload User Profile Image to Storage",Toast.LENGTH_SHORT).show()
                Log.e("Storage Error: ", it.toString())
            }
        }
    }

    // This function check if the user filled the text fields with data.
    private fun validateForm(firstName: String,surname:String, phoneNumber:String): Boolean {

        return when {
            TextUtils.isEmpty(firstName) -> {
                Toast.makeText(applicationContext, "Please enter first name!", Toast.LENGTH_SHORT).show()
                false
            }
            TextUtils.isEmpty(surname) -> {
                Toast.makeText(applicationContext, "Please enter surname!", Toast.LENGTH_SHORT).show()
                false
            }

            TextUtils.isEmpty(phoneNumber) -> {
                Toast.makeText(applicationContext, "Please enter a phone number!", Toast.LENGTH_SHORT).show()
                false
            }
            else -> {
                true
            }
        }
    }

    // This function check if the user entered a password of minimum 8 characters,
    // 1 capital letter, a number and a special character,
    // and if the password field match with the confirm password field.
    private fun verifyPassword(password: String, confirmPassword: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        return password == confirmPassword && passwordPattern.matches(password)
    }

    // This function update the user information in the database under authenticated user node.
    private fun editInfo(){

        val userFirstNameReference = database.reference.child("Users").child(currentUserId!!).child("firstName")
        val userSurnameReference = database.reference.child("Users").child(currentUserId!!).child("surname")
        val userPhoneNumberReference = database.reference.child("Users").child(currentUserId!!).child("phoneNumber")

        userFirstNameReference.setValue(editDetailsBinding.etFirstNameEditDetails.text.toString())
        userSurnameReference.setValue(editDetailsBinding.etSurnameEditDetails.text.toString())
        userPhoneNumberReference.setValue(editDetailsBinding.etPhoneEditDetails.text.toString())

    }

    // This function check if the user is signed in recently (under 5 minutes),
    // this is needed in case the user wants to change the password, firebase have a security rule
    // that allow users to change the password only if is recently signed in.
    private fun isUserRecentlyLoggedIn(): Boolean {
        val user = auth.currentUser
        val lastSignInTimestamp = user?.metadata?.lastSignInTimestamp
        val currentTimeMillis = System.currentTimeMillis()

        if (lastSignInTimestamp != null) {
            val timeDifferenceMillis = currentTimeMillis - lastSignInTimestamp
            val timeDifferenceMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis)
            return timeDifferenceMinutes < 5
        }
        return false
    }
}