package br.com.zup.edu.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}")
interface BcbClient {

    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Post("/api/v1/pix/keys")
    fun registerPixKeyBcb(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Delete("/api/v1/pix/keys/{key}")
    fun removePixKeyBcb(@QueryValue key: String, @Body request: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>

    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Get("/api/v1/pix/keys/{key}")
    fun findByKey(@QueryValue key: String): HttpResponse<PixKeyDetailsResponse>
}
