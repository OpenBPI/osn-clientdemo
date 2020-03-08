package cn.leiyu.osn_clientdemo.beans

import android.os.Parcel
import android.os.Parcelable

/**
 * 消息实体
 */
data class MsgBean(val _id: Int,
                   val sendId: Int,
                   val peerId: Int,
                   val peerName: String,
                   val peerAddress: String,
                   val peerLableColor: Int,
                   var msg: String,
                   val time: String,
                   val unRead: Int,
                   var sendSuccess: Int = 1)
    : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(_id)
        parcel.writeInt(sendId)
        parcel.writeInt(peerId)
        parcel.writeString(peerName)
        parcel.writeString(peerAddress)
        parcel.writeInt(peerLableColor)
        parcel.writeString(msg)
        parcel.writeString(time)
        parcel.writeInt(unRead)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MsgBean> {
        override fun createFromParcel(parcel: Parcel): MsgBean {
            return MsgBean(parcel)
        }

        override fun newArray(size: Int): Array<MsgBean?> {
            return arrayOfNulls(size)
        }
    }

}