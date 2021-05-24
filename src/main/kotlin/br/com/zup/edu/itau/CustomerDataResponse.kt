package br.com.zup.edu.itau

data class CustomerDataResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstitutionResponse
)