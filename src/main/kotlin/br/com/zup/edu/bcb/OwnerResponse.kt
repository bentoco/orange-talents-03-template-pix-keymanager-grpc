package br.com.zup.edu.bcb

data class OwnerResponse(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
)
