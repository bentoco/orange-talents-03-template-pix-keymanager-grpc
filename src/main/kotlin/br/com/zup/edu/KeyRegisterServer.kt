package br.com.zup.edu

import br.com.zup.edu.client.FetchClient
import br.com.zup.edu.shared.ErrorHandler
import br.com.zup.edu.shared.RegisterAlreadyExistsException
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.IllegalArgumentException

/**
 * Premises to generate key
 *
 * 1 - need to a valid keyValue (validated by regex or null if random key)
 * 2 - must not exists a generated key for same userId and keytype
 *
 * To-do
 *
 * 1 - check UNKNOWN TYPES and nullables values
 * 2 - check if already exists key generated
 * 3 - fetch account at external itau service
 * 4 - valid the key value
 */
@AllOpen
@ErrorHandler
@Singleton
class KeyRegisterServer(
    @Inject val repository: KeyRegisterRepository,
    @Inject val keyValueValidator: KeyValueValidator,
    @Inject val itauClient: FetchClient
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun registerKey(request: RegisterKeyRequest?, responseObserver: StreamObserver<RegisterKeyResponse>?) {

        LOGGER.info("New request: $request")

        if (request!!.typeKey == TypeKey.UNKNOWN_TYPE_KEY || request.typeAccount == TypeAccount.UNKNOWN_TYPE_ACCOUNT)
            throw IllegalArgumentException("invalid input data")

        if (request.typeKey == TypeKey.RANDOM_KEY && !request.keyValue.isNullOrEmpty())
            throw IllegalArgumentException("value key must be null for random key type")

        val existsKey = when (request.typeKey) {
            TypeKey.RANDOM_KEY -> repository.existsByUserIdAndTypeKeyEquals(request.userId, request.typeKey)
            else -> repository.existsByKeyValue(request.keyValue)
        }
        if (existsKey) throw RegisterAlreadyExistsException("key value and type already register")

        val validatorResult: Boolean = keyValueValidator.validator(request.keyValue)
        if (!validatorResult && !request.typeKey.equals(TypeKey.RANDOM_KEY))
            throw IllegalArgumentException("invalid input key value")

        val account = submitForConsult(request)
        when (request.typeKey) {
            TypeKey.RANDOM_KEY -> {
                val randomKey = randomKeyGanerator(request)
                val toPersist = randomKey.toModel(account)
                repository.save(toPersist)
                responseObserver?.onNext(
                    RegisterKeyResponse
                        .newBuilder()
                        .setPixId(randomKey.keyValue)
                        .build()
                )
                responseObserver?.onCompleted()
                return
            }
            else -> {
                val key = keyGenerator(request)
                val toPersist = key.toModel(account)
                repository.save(toPersist)
                responseObserver?.onNext(
                    RegisterKeyResponse
                        .newBuilder()
                        .setPixId(request.keyValue)
                        .setUserId(request.userId)
                        .build()
                )
                responseObserver?.onCompleted()
                return
            }
        }
    }

    private fun submitForConsult(request: RegisterKeyRequest): AssociatedAccount {
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

/**
 * Extension methods
 */

private fun randomKeyGanerator(request: RegisterKeyRequest): KeyRegisterRequest {
    return KeyRegisterRequest(
        userId = request.userId,
        typeKey = request.typeKey,
        keyValue = UUID.randomUUID().toString(),
        typeAccount = request.typeAccount
    )
}

private fun keyGenerator(request: RegisterKeyRequest): KeyRegisterRequest {
    return KeyRegisterRequest(
        userId = request.userId,
        typeKey = request.typeKey,
        keyValue = request.keyValue,
        typeAccount = request.typeAccount
    )
}
