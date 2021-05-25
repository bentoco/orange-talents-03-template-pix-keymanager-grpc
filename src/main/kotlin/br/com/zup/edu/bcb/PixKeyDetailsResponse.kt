package br.com.zup.edu.bcb

import br.com.zup.edu.TypeAccount
import br.com.zup.edu.TypeKey
import br.com.zup.edu.consult.PixKeyInfo
import br.com.zup.edu.register.AssociatedAccount

data class PixKeyDetailsResponse(
    val keyType: TypeKey,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: String
) {
    fun toModel(): PixKeyInfo {
        return PixKeyInfo(
            type = keyType,
            key = key,
            accountType = when(this.bankAccount.accountType){
                AccountType.CACC -> TypeAccount.CONTA_CORRENTE
                AccountType.SVGS -> TypeAccount.CONTA_POUPANCA
            },
            account = AssociatedAccount(
                institutionName = "ITAÚ UNIBANCO S.A.",
                ownerName = owner.name,
                ownerCpf = owner.taxIdNumber,
                branch = bankAccount.branch,
                accountNumber = bankAccount.accountNumber,
            )
        )
    }
}