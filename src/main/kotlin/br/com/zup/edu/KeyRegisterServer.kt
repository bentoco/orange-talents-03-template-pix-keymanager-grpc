package br.com.zup.edu

import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException

/**
 * Premises to generate key
 *
 * 1 - need to a valid keyValue (validated by regex or null if random key)
 * 2 - must not exists a generated key for same userId and keytype
 *
 * To-do
 *
 * 1 - if random keytype check already exists and generate
 * 2 - if not null and keytype != random, valid already exists and generate
 * 3 - if null and keytype != random return error
 * 4 - persist key
 */

@Singleton
@Transactional
class KeyRegisterServer(
    @Inject val repository: KeyRegisterRepository,  //2
    @Inject val keyValeuValidator: KeyValueValidator,
    @Inject val itauClient: ItauAccountsClient
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {
    override fun registerKey(request: RegisterKeyRequest?, responseObserver: StreamObserver<RegisterKeyResponse>?) {

        //2 - fetch data at itau external service
        val response = itauClient.fetchAccountsByType(newKey?.userId!!, newKey.typeAccount!!.name)
        val account = response.body()?.toModel() ?: throw IllegalStateException("account not found")


        if (request?.keyValue == null && request?.typeKey == TypeKey) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("invalid input data")
                    .asRuntimeException()
            )
            return
        }

        val validatorResult: Boolean = keyValueValidator.validator(request!!.keyValue)
        //1
        if (!validatorResult && !request!!.typeKey.equals(TypeKey.RANDOM_KEY)) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("invalid input key value")
                    .asRuntimeException()
            )
            return
        }

        val existsKey = repository.existsByUserIdAndTypeKeyEquals(request.userId, request.typeKey)
        //1
        if (existsKey) {
            responseObserver?.onError(
                Status.ALREADY_EXISTS
                    .withDescription("key value and type already register")
                    .asRuntimeException()
            )
            return
        }

        //1
        when (request.typeKey) {
            TypeKey.RANDOM_KEY -> {
                val randomKey = randomKeyGanerator(request)
                persistKey(randomKey, repository, responseObserver)
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
                persistKey(key, repository, responseObserver)
                responseObserver?.onNext(
                    RegisterKeyResponse
                        .newBuilder()
                        .setPixId(request.keyValue)
                        .build()
                )
                responseObserver?.onCompleted()
                return
            }
        }
    }
}

private fun randomKeyGanerator(request: RegisterKeyRequest): KeyRegister {
    return KeyRegister(
        userId = request.userId,
        typeKey = request.typeKey,
        keyValue = UUID.randomUUID().toString(),
        typeAccount = request.typeAccount
    )
}

private fun keyGenerator(request: RegisterKeyRequest): KeyRegister {
    return KeyRegister(
        userId = request.userId,
        typeKey = request.typeKey,
        keyValue = request.keyValue,
        typeAccount = request.typeAccount
    )
}
