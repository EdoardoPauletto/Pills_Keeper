package com.uninsubria.pillskeeper

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

data class Upload(var name:String, var mImageUrl:String) {
    constructor() : this("", "")

    fun convertImg(img: String = mImageUrl ): StorageReference {
        return FirebaseStorage.getInstance().getReferenceFromUrl("gs://prove-b822e.appspot.com$img")
    }
}
