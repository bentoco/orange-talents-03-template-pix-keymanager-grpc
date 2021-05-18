package br.com.zup.edu.client

import br.com.zup.edu.AllOpen
import com.fasterxml.jackson.annotation.JsonProperty

@AllOpen
data class ClienteInstitution(
    @param:JsonProperty("nome") val nome: String,
    @param:JsonProperty("ispb") val ispb: String
)
