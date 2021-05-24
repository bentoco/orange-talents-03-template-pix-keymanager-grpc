package br.com.zup.edu.bcb

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: String
)