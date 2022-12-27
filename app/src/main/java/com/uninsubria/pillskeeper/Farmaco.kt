package com.uninsubria.pillskeeper

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

data class Farmaco(var name:String, var mImageUrl:String, var qTot: Int, var q: Int, var every: String, var time: String) {
    constructor() : this("","",0,0,"","")

    fun convertImg(img: String = mImageUrl ): StorageReference {
        return FirebaseStorage.getInstance().getReferenceFromUrl("gs://prove-b822e.appspot.com$img")
    }
}
