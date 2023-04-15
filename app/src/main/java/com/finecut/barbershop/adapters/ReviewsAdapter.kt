package com.finecut.barbershop.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finecut.barbershop.R
import com.finecut.barbershop.databinding.ReviewsCardBinding
import com.finecut.barbershop.models.Reviews
import com.finecut.barbershop.models.Users
import com.finecut.barbershop.utils.FirebaseData
import com.google.firebase.database.DatabaseError
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

//T his reviews adapter class take a list of reviews and populate the views of the review card
// in the recycler view of the Reviews activity.
class ReviewsAdapter(private var context: Context,
private var reviewsList: ArrayList<Reviews>)
    :RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>(){

    inner class ReviewsViewHolder(val adapterBinding: ReviewsCardBinding)
        : RecyclerView.ViewHolder(adapterBinding.root)
    //This function inflate the views of the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        val binding = ReviewsCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ReviewsViewHolder(binding)
    }


    //This function bind the views of the recycler view with the given data set
    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {

        FirebaseData.DBHelper.getCurrentUserFromDatabase(reviewsList[holder.adapterPosition].userId, object : FirebaseData.DBHelper.CurrentUserCallback {
            @SuppressLint("SetTextI18n")
            override fun onSuccess(currentUser: Users) {
                holder.adapterBinding.tvClientName.text = "${currentUser.firstName} ${currentUser.surname}"
                holder.adapterBinding.rbClientRating.rating = reviewsList[holder.adapterPosition].rating

                Picasso.get().load(currentUser.image.ifEmpty { context.getString(R.string.userImagePlaceHolder) })
                    .into(holder.adapterBinding.ivClientImage, object :
                        Callback {
                        override fun onSuccess() {
                            holder.adapterBinding.pbClientImage.visibility = View.GONE
                        }

                        override fun onError(e: Exception?) {
                            if (e != null) {
                                e.localizedMessage?.let { Log.e("Error Loading Image: ", it) }
                            }
                        }
                    })

                holder.adapterBinding.tvClientDescription.text = reviewsList[holder.adapterPosition].comment
            }

            override fun onFailure(error: DatabaseError) {
                Log.e("Database Error: ",error.toString())
            }
        })
    }

    //This function count and return the size of the list
    override fun getItemCount(): Int {
        return reviewsList.size
    }
}