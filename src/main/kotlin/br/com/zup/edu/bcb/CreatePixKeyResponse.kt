package br.com.zup.edu.bcb

import br.com.zup.edu.TypeKey

data class CreatePixKeyResponse(
    val keyType: TypeKey,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: String
)

data class BankAccountResponse(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

data class OwnerResponse(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
)
