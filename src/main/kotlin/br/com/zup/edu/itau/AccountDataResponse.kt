package br.com.zup.edu.itau

import br.com.zup.edu.TypeAccount
import br.com.zup.edu.register.AssociatedAccount

data class AccountDataResponse(
    val tipo: TypeAccount,
    val instituicao: InstitutionResponse,
    val agencia: String,
    val numero: String,
    val titular: AccountOwnerResponse
) {
    fun toModel(): AssociatedAccount {
        return AssociatedAccount(
            institutionName = instituicao.nome,
            institutionIspb = instituicao.ispb,
            branch = agencia,
            accountNumber = numero,
            ownerId = titular.id,
            ownerName = titular.nome,
            ownerCpf = titular.cpf
        )
    }
}

data class InstitutionResponse(
    val nome: String,
    val ispb: String
)

data class AccountOwnerResponse(
    val id: String,
    val nome: String,
    val cpf: String
)
