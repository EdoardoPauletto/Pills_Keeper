package com.uninsubria.pillskeeper

data class Upload(var name:String?, var mImageUrl:String?) {
    constructor() : this("", "")

    fun set(utente: Upload?) {
        name = utente?.name
        mImageUrl = utente?.mImageUrl
    }
}
