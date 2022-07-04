package it.uninsubria.prova

import android.net.Uri

data class Upload(var name:String?, var mImageUrl:String?) {
    //prova .set
    constructor() : this("", "")

    fun set(utente: Upload?) {
        name = utente?.name
        mImageUrl = utente?.mImageUrl
    }
}
