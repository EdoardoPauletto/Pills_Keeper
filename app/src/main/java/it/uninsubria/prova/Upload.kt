package it.uninsubria.prova

import android.net.Uri

data class Upload(var name:String?, var mImageUrl:String?) {
    //prova .set
    constructor() : this("","")
    fun set(utente: Upload?) {
        name = utente?.name
        mImageUrl = utente?.mImageUrl
    }
    //prova

    //originale per togleire spazi vuoti
    /*init {
        if (name.trim() == "") {
            name = "No Name"
        }
    }*/
}
/*package it.uninsubria.prova;

import android.net.Uri;

public class Upload {
    private String mName;
    private String mImageUrl;

    //costruttore vuoto
    public Upload(String trim, Uri uploadSessionUri){
        //SERVE
    }

    //costruttore
    public Upload(String name, String mImageUrl){
        //trim per rimuovere spazi vuoti
        if(name.trim().equals("")){
            name = "No Name";
        }
        mName = name;
        mImageUrl = mImageUrl;
    }

    public String getName() {
        return mName;
    }

    public void  setmName(String name){
        mName = name;
    }

    public String getmImageUrl(){
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        mImageUrl = mImageUrl;
    }
}*/
