package br.com.zup.edu.itau

import br.com.zup.edu.AllOpen

@AllOpen
data class ClientHolder(
    val id: String,
    val nome: String,
    val cpf: String
)