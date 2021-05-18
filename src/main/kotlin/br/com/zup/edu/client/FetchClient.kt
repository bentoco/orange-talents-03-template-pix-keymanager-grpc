package br.com.zup.edu.client

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.accounts.url}")
interface FetchClient {

    @Get("/api/v1/clientes/{userId}/contas")
    fun fetchAccountsByType(
        @PathVariable userId: String,
        @QueryValue tipo: String
    ) : ClientDetails?
}
