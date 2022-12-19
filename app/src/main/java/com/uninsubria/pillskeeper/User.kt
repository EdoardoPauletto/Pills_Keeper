package com.uninsubria.pillskeeper

data class User(var name: String, var surname: String, var email: String, var password: String, var tel: String, var emailMedico: String){
    constructor() : this("","","","","","") //vuoto, serve per inizializzarlo

    var personeFidate = ArrayList<ContattiFidati>()

    fun addPersonaFidata(persona: ContattiFidati){
        if (personeFidate.size < 5)
            personeFidate.add(persona)
        //else errore
    }
}
