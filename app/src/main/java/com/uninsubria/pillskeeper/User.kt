package com.uninsubria.pillskeeper

data class User(var user: String, var email: String, var pwd: String, var cell: String, var emailMedico: String){
    var personeFidate = ArrayList<String>()

    fun addPersonaFidata(persona: String){
        if (personeFidate.size <= 5)
            personeFidate.add(persona)
        //else errore
    }

    fun userMap(): HashMap<String, String>{
        val Mappa = HashMap<String, String>() //faccio chiave,valore con name=...
        Mappa["name"] = user
        Mappa["email"] = email
        Mappa["password"] = pwd
        Mappa["numero di cellulare"] = cell
        Mappa["email medico"] = emailMedico
        /*var count = 0
        for (p in personeFidate){ //forse meglio farlo a parte sotto una cartella specifica
            Mappa["Nome " + count] = p
            count++
        }*/
        return Mappa
    }
}
