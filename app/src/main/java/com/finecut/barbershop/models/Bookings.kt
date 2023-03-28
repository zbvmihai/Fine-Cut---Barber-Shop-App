package com.finecut.barbershop.models

import android.os.Parcel
import android.os.Parcelable

data class Bookings(
    val userId: String = "",
    val barberId: String = "",
    val date: String = "",
    val timeslot: String = "",
    val service: String = "",
    val bookStatus: Int = 0,
    val offer: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(barberId)
        parcel.writeString(date)
        parcel.writeString(timeslot)
        parcel.writeString(service)
        parcel.writeInt(bookStatus)
        parcel.writeString(offer)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Bookings> {
        override fun createFromParcel(parcel: Parcel): Bookings {
            return Bookings(parcel)
        }

        override fun newArray(size: Int): Array<Bookings?> {
            return arrayOfNulls(size)
        }
    }
}
