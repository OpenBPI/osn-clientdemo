package cn.leiyu.osn_clientdemo.beans

import android.os.Parcel
import android.os.Parcelable

/**
 * 联系人实体对象
 */
open class UserBean: LoginBean{

    /**
     * 联系人ID
     */
    var _id: Long
    /**
     * 昵称
     */
    var nickName: String
//    get() {
//        return if(field == "")loginName else field
//    }
    /**
     * 签名
     */
    var sign: String?
    /**
     * 备注
     */
    var remark: String?
    /**
     * 标签颜色
     */
    var lableColor: Int

    constructor(loginId: Long,
                loginName: String,
                _id: Long = -1,
                nickName: String = "",
                address: String,
                sign: String? = "",
                remark: String? = "",
                lableColor: Int = 0): super(loginId, loginName, address){
        this._id = _id
        this.nickName = nickName
        this.sign = sign
        this.remark = remark
        this.lableColor = lableColor
    }

    protected constructor(parcel: Parcel) : super(parcel){
        this._id = parcel.readLong()
        this.nickName = parcel.readString()!!
        this.address = parcel.readString()!!
        this.sign = parcel.readString()
        this.remark = parcel.readString()
        this.lableColor = parcel.readInt()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeLong(_id)
        parcel.writeString(nickName)
        parcel.writeString(address)
        parcel.writeString(sign)
        parcel.writeString(remark)
        parcel.writeInt(lableColor)
    }

    companion object CREATOR : Parcelable.Creator<UserBean> {
        override fun createFromParcel(parcel: Parcel): UserBean {
            return UserBean(parcel)
        }

        override fun newArray(size: Int): Array<UserBean?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * 登陆用户实体
 */
open class LoginBean(open var loginId: Long,
                     open val loginName: String,
                     /**
                      * 地址
                      */
                     var address: String): Parcelable{
    protected constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(loginId)
        parcel.writeString(loginName)
        parcel.writeString(address)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoginBean> {
        override fun createFromParcel(parcel: Parcel): LoginBean {
            return LoginBean(parcel)
        }

        override fun newArray(size: Int): Array<LoginBean?> {
            return arrayOfNulls(size)
        }
    }

}
