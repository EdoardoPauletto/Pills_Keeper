package com.uninsubria.pillskeeper

data class User(var user: String, var email: String, var pwd: String, var cell: String, var emailMedico: String){
    constructor() : this("","","","","") //vuoto, serve per inizializzarlo

    var personeFidate = ArrayList<ContattiFidati>()

    fun addPersonaFidata(persona: ContattiFidati){
        if (personeFidate.size <= 5)
            personeFidate.add(persona)
        //else errore
    }
}
