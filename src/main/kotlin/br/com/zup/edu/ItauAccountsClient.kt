package br.com.zup.edu

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.net.http.HttpResponse

@Client("\${itau.accounts:url}")
interface ItauAccountsClient {

    @Get("/api/v1/clientes/{userId}/contas{?typeAccount}")
    fun fetchAccountsByType(
        @PathVariable userId: String,
        @QueryValue typAccount: String
    ): HttpResponse<ItauAccountDataResponse>
}

data class ItauAccountDataResponse(
    val tipo: String,
    val instituicao: InstitutionResponse,
    val agencia: String,
    val numero: String,
    val titular: HolderResponse
) {
    fun toModel(): AssociatedAccount {
        return AssociatedAccount(
            institution = this.instituicao.nome,
            holderName = this.titular.nome,
            holderCpf = this.titular.cpf,
            agency = this.agencia,
            accountNumber = this.numero
        )
    }
}

data class InstitutionResponse(val nome: String, val ispb: String)
data class HolderResponse(val nome: String, val cpf: String)
