package com.finecut.barbershop.models

import android.os.Parcel
import android.os.Parcelable

data class Users(
    val id: String = "",
    val firstName: String = "",
    val surname: String = "",
    val email: String = "",
    val phoneNumber: Long = 0,
    val isClient: Int = 1,
    val image: String = "",
    val bookings: List<Bookings> = emptyList(),
    val points: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.createTypedArrayList(Bookings.CREATOR)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(firstName)
        parcel.writeString(surname)
        parcel.writeString(email)
        parcel.writeLong(phoneNumber)
        parcel.writeInt(isClient)
        parcel.writeString(image)
        parcel.writeTypedList(bookings)
        parcel.writeString(points)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Users> {
        override fun createFromParcel(parcel: Parcel): Users {
            return Users(parcel)
        }

        override fun newArray(size: Int): Array<Users?> {
            return arrayOfNulls(size)
        }
    }
}
