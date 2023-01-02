package com.uninsubria.pillskeeper

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

data class Farmaco(var name:String, var mImageUrl:String, var qTot: Double, var q: Double, var every: String, var time: String, var day: ArrayList<Boolean>) : java.io.Serializable {//serializable serve per putExtra
    constructor() : this("","",0.0, 0.0,"","", ArrayList<Boolean>(7))

    fun convertImg(img: String = mImageUrl ): StorageReference {
        return FirebaseStorage.getInstance().getReferenceFromUrl("gs://prove-b822e.appspot.com$img")
    }
}
