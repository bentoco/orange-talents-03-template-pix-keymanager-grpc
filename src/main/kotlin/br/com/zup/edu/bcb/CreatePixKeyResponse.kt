package br.com.zup.edu.bcb

import br.com.zup.edu.TypeKey

data class CreatePixKeyResponse(
    val keyType: TypeKey,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: String
)