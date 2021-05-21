package br.com.zup.edu.register;

import br.com.zup.edu.KeyRepository
import br.com.zup.edu.KeyValueValidator
import br.com.zup.edu.TypeAccount
import br.com.zup.edu.TypeKey
import br.com.zup.edu.bcb.BcbClient
import br.com.zup.edu.bcb.CreatePixKeyRequest
import br.com.zup.edu.bcb.CreatePixKeyResponse
import br.com.zup.edu.itau.FetchClient
import br.com.zup.edu.shared.RegisterAlreadyExistsException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class KeyRegisterService(
    @Inject private val repository: KeyRepository,
    @Inject private val keyValueValidator: KeyValueValidator,
    @Inject private val itauClient: FetchClient,
    @Inject private val bcbClient: BcbClient
) {
    fun keyRegisterValid(@Valid request: NewKey): Boolean {
        if (request.typeKey == TypeKey.UNKNOWN_TYPE_KEY || request.typeAccount == TypeAccount.UNKNOWN_TYPE_ACCOUNT)
            throw IllegalArgumentException("invalid input data")

        if (request.typeKey == TypeKey.RANDOM && !request.keyValue.isNullOrEmpty())
            throw IllegalArgumentException("value key must be null for random key type")

        val existsKey = when (request.typeKey) {
            TypeKey.RANDOM -> repository.existsByUserIdAndTypeKeyEquals(request.userId, request.typeKey)
            else -> repository.existsByKeyValue(request.keyValue!!)
        }
        if (existsKey) throw RegisterAlreadyExistsException("key value and type already register")

        val validatorResult: Boolean = keyValueValidator.validator(request.keyValue, request.typeKey)
        if (!validatorResult && request.typeKey != TypeKey.RANDOM)
            throw IllegalArgumentException("invalid input key value")

        return true
    }

    fun createPixKeyBcb(request: CreatePixKeyRequest): CreatePixKeyResponse {
        return try {
            bcbClient.registerPixKeyBcb(request)
        } catch (e: HttpClientResponseException) {
            println("Status: ${e.status}")
            println("Message: ${e.message}")
            null
        } ?: throw RegisterAlreadyExistsException("the informed pix key exists already")
    }

    fun submitForConsult(request: NewKey): AssociatedAccount {
        val response = try {
            itauClient
                .fetchAccountsByType(request.userId, request.typeAccount.name)
        } catch (e: HttpClientResponseException) {
            println("Status: ${e.status}")
            println("Message: ${e.message}")
            null
        } ?: throw IllegalStateException("account not found")

        return response.toModel()
    }
}
