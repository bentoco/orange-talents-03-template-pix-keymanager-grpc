package br.com.zup.edu.bcb

import br.com.zup.edu.TypeKey

data class CreatePixKeyRequest(
    val keyType: TypeKey,
    val key: String?,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest
)

data class BankAccountRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

data class OwnerRequest(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
)


