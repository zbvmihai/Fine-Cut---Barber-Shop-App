package com.finecut.barbershop.activities

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finecut.barbershop.databinding.ActivitySignUpBinding
import com.finecut.barbershop.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var signUpBinding: ActivitySignUpBinding

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val myReference = database.reference.child("Users")

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = signUpBinding.root
        setContentView(view)



        signUpBinding.btnSignUp.setOnClickListener {

            val userFirstName = signUpBinding.etFirstNameSignUp.text.toString()
            val userSurname = signUpBinding.etSurnameSignUp.text.toString()
            val userEmail = signUpBinding.etSignUpEmail.text.toString()
            val userPhoneNumber = signUpBinding.etPhoneSignUp.text.toString()
            val userPassword = signUpBinding.etSignUpPassword.text.toString()
            val userConfirmPassword = signUpBinding.etSignUpConfirmPassword.text.toString()

            if(validateForm(userFirstName,userSurname,userEmail,
                    userPhoneNumber,userPassword,userConfirmPassword)){

                signUpWithFirebase(userFirstName,userSurname,userEmail,userPhoneNumber,userPassword,userConfirmPassword)
                finish()
            }
        }
    }

    private fun signUpWithFirebase(userFirstName: String, userSurname: String,userEmail: String,
                                   userPhoneNumber: String,userPassword: String,
                                   userConfirmPassword: String){

        if (verifyPassword(userPassword, userConfirmPassword)) {
            auth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener { task ->

                if (task.isSuccessful){

                    userId = auth.currentUser?.uid!!
                    Toast.makeText(applicationContext, "Account created successfully!" , Toast.LENGTH_SHORT).show()
                    addUserToDatabase(userFirstName,userSurname,userEmail,userPhoneNumber.toLong())

                }else{
                    Toast.makeText(applicationContext, "Failed to create an account!", Toast.LENGTH_SHORT).show()
                    Log.e("Error:",task.exception.toString())
                }
            }
        } else {
            if (userPassword != userConfirmPassword) {
                Toast.makeText(applicationContext, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Password must be 8 characters long and contain at least 1 number and a capital letter", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyPassword(password: String, confirmPassword: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        return password == confirmPassword && passwordPattern.matches(password)
    }

    private fun validateForm(firstName: String,surname:String, email: String,phoneNumber:String,
                             password: String,confirmPassword: String): Boolean {

        return when {
            TextUtils.isEmpty(firstName) -> {
                Toast.makeText(applicationContext, "Please enter first name!", Toast.LENGTH_SHORT).show()
                false
            }
            TextUtils.isEmpty(surname) -> {
                Toast.makeText(applicationContext, "Please enter surname!", Toast.LENGTH_SHORT).show()
                false
            }
            TextUtils.isEmpty(email) -> {
                Toast.makeText(applicationContext, "Please enter an email!", Toast.LENGTH_SHORT).show()
                false
            }
            TextUtils.isEmpty(phoneNumber) -> {
                Toast.makeText(applicationContext, "Please enter a phone number!", Toast.LENGTH_SHORT).show()
                false
            }
            TextUtils.isEmpty(password) -> {
                Toast.makeText(applicationContext, "Please enter a password!", Toast.LENGTH_SHORT).show()
                false
            }
            TextUtils.isEmpty(confirmPassword) -> {
                Toast.makeText(applicationContext, "Please confirm your password", Toast.LENGTH_SHORT).show()
                false
            }
            else -> {
                true
            }
        }
    }

    private fun addUserToDatabase(firstName: String,surname:String, email: String,phoneNumber:Long){

        val user = Users(userId,firstName,surname,email,phoneNumber)

        myReference.child(userId).setValue(user).addOnCompleteListener { task ->

            if (task.isSuccessful){
                Toast.makeText(applicationContext,"User added to database!",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(applicationContext,"Failed to add user to database!",Toast.LENGTH_SHORT).show()
                Log.e("Error:",task.exception.toString())
            }
        }
    }
}