package br.com.zup.edu.register

import br.com.zup.edu.KeyManagerServiceGrpc
import br.com.zup.edu.RegisterKeyRequest
import br.com.zup.edu.RegisterKeyResponse
import br.com.zup.edu.TypeKey
import br.com.zup.edu.shared.ErrorHandler
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

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
@ErrorHandler
@Singleton
@Validated
class KeyRegisterServer(
    @Inject private val service: KeyRegisterService
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun registerKey(request: RegisterKeyRequest, responseObserver: StreamObserver<RegisterKeyResponse>?) {

        LOGGER.info("New request: $request")

        val newKey = request.toKey()

        val response = service.registerKey(newKey)

        responseObserver?.onNext(
            RegisterKeyResponse.newBuilder()
                .setPixId(response.keyValue)
                .setUserId(response.userId)
                .build()
        )
        responseObserver?.onCompleted()
        return
    }
}

/**
 * Extension methods
 */

fun RegisterKeyRequest.toKey(): NewKey {
    return NewKey(
        userId = userId,
        typeKey = typeKey,
        keyValue = when (typeKey) {
            TypeKey.UNKNOWN_TYPE_KEY -> null
            TypeKey.RANDOM -> null
            else -> keyValue
        },
        typeAccount = typeAccount
    )
}
