package com.finecut.barbershop.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finecut.barbershop.databinding.BarbersCardBinding
import com.finecut.barbershop.models.Barbers
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class BarbersAdapter(
    private var barbersList: ArrayList<Barbers>
): RecyclerView.Adapter<BarbersAdapter.BarbersViewHolder>() {

    inner class BarbersViewHolder(val adapterBinding: BarbersCardBinding)
        : RecyclerView.ViewHolder(adapterBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarbersViewHolder {

        val binding = BarbersCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BarbersViewHolder(binding)

    }

    override fun onBindViewHolder(holder: BarbersViewHolder, position: Int) {

        holder.adapterBinding.tvBarberName.text = barbersList[holder.adapterPosition].barberName
        holder.adapterBinding.tvBarberDescription.text = barbersList[holder.adapterPosition].barberDescription
        holder.adapterBinding.rbBarberRating.rating = barbersList[holder.adapterPosition].barberRating.toFloat()

        val barberImageUrl = barbersList[holder.adapterPosition].barberImage

        Picasso.get().load(barberImageUrl).into(holder.adapterBinding.ivBarberImage, object : Callback{
            override fun onSuccess() {
                holder.adapterBinding.pbBarberImage.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
                if (e != null) {
                    e.localizedMessage?.let { Log.e("Error Loading Image: ", it) }
                }
            }
        })

    }

    override fun getItemCount(): Int {
        return barbersList.size
    }
}