package br.com.zup.edu.client

import br.com.zup.edu.AllOpen
import com.fasterxml.jackson.annotation.JsonProperty

@AllOpen
data class ClientHolder(
    @param:JsonProperty("id")val id: String,
    @param:JsonProperty("nome") val nome: String,
    @param:JsonProperty("cpf") val cpf: String
)