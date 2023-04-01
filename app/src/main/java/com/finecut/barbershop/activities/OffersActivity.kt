package com.finecut.barbershop.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.finecut.barbershop.adapters.OffersAdapter
import com.finecut.barbershop.databinding.ActivityOffersBinding
import com.finecut.barbershop.models.Offers
import com.finecut.barbershop.utils.BaseActivity
import com.finecut.barbershop.utils.FirebaseData
import com.google.firebase.database.DatabaseError

class OffersActivity : BaseActivity() {

    private lateinit var offersBinding: ActivityOffersBinding

    private lateinit var offersAdapter: OffersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        offersBinding = ActivityOffersBinding.inflate(layoutInflater)
        val view = offersBinding.root

        setContentView(view)
        setupActionAndSideMenuBar(this, offersBinding.tbOffers, true, view)

        offersBinding.rvOffers.layoutManager = LinearLayoutManager(this@OffersActivity)


        FirebaseData.DBHelper.getOffersFromDatabase(object : FirebaseData.DBHelper.OffersCallback {
            override fun onSuccess(offers: ArrayList<Offers>) {

                offersAdapter = OffersAdapter(this@OffersActivity, offers)
                offersBinding.rvOffers.adapter = offersAdapter
            }

            override fun onFailure(error: DatabaseError) {
                Log.e("Database Error: ", error.toString())
            }
        })
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
