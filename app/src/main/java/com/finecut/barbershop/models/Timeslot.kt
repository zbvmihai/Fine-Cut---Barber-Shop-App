package com.finecut.barbershop.models

import android.os.Parcel
import android.os.Parcelable

data class Timeslot(
    val time: String = "",
    val availability: Int = 0
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt()
    )

    companion object CREATOR : Parcelable.Creator<Timeslot> {
        override fun createFromParcel(parcel: Parcel): Timeslot {
            return Timeslot(parcel)
        }

        override fun newArray(size: Int): Array<Timeslot?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(time)
        parcel.writeInt(availability)
    }
}
