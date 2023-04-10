package com.finecut.barbershop.adapters

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.finecut.barbershop.databinding.OffersCardBinding
import com.finecut.barbershop.models.Offers

// This offers adapter class take a list of offers and populate the views of the offers card
// in the recycler view of the Offers activity.
class OffersAdapter(private var context: Context,
                    var offersList: ArrayList<Offers>)
    : RecyclerView.Adapter<OffersAdapter.ReviewsViewHolder>() {

    inner class ReviewsViewHolder(val adapterBinding: OffersCardBinding)
        : RecyclerView.ViewHolder(adapterBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        val binding = OffersCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewsViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {

        holder.adapterBinding.pbOffersCard.visibility = View.GONE

        holder.adapterBinding.tvOfferCode.text = offersList[position].code
        holder.adapterBinding.tvOfferDate.text = "Available until: ${offersList[position].date}"
        holder.adapterBinding.tvOfferDescription.text = offersList[position].description

        holder.adapterBinding.llOffersCard.setOnClickListener {
            copyToClipboard(context, offersList[position].code)
        }
    }

    override fun getItemCount(): Int {
        return offersList.size
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Offer Code", text)
        clipboardManager.setPrimaryClip(clipData)

        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
