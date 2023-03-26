package com.finecut.barbershop.models

import android.os.Parcel
import android.os.Parcelable

data class Barbers(
    val barberId: String = "",
    val barberImage: String = "",
    val barberName: String = "",
    val barberRating: Double = 5.0,
    val barberDescription: String = "",
    val timeslots: List<Timeslot> = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.createTypedArrayList(Timeslot.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(barberId)
        parcel.writeString(barberImage)
        parcel.writeString(barberName)
        parcel.writeDouble(barberRating)
        parcel.writeString(barberDescription)
        parcel.writeTypedList(timeslots)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Barbers> {
        override fun createFromParcel(parcel: Parcel): Barbers {
            return Barbers(parcel)
        }

        override fun newArray(size: Int): Array<Barbers?> {
            return arrayOfNulls(size)
        }
    }
}
