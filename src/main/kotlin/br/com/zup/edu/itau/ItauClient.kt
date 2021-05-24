package br.com.zup.edu.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.accounts.url}")
interface ItauClient {

    @Get("/api/v1/clientes/{userId}/contas")
    fun fetchAccountsByType(
        @PathVariable userId: String,
        @QueryValue tipo: String
    ): HttpResponse<AccountDataResponse>

    @Get("/api/v1/clientes/{userId}")
    fun fetchAccount(@PathVariable userId: String): HttpResponse<CustomerDataResponse>
}

