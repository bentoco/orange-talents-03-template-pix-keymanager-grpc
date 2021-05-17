package br.com.zup.edu

import io.grpc.stub.StreamObserver
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
 * 1 - if random keytype check already exists and generate
 * 2 - if not null and keytype != random, valid already exists and generate
 * 3 - if null and keytype random must register
 * 4 -
 */

@Singleton
class KeyRegisterServer(
    @Inject val service: RegisterNewKeyService
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun registerKey(
        request: RegisterKeyRequest,
        responseObserver: StreamObserver<RegisterKeyResponse>
    ) {

        val newKey = request.toModel()
        val createdKey = service.register(newKey)

        responseObserver?.onNext(
            RegisterKeyResponse
                .newBuilder()
                .setUserId(createdKey.userId.toString())
                .setPixId(createdKey.id)
                .build()
        )
        responseObserver?.onCompleted()

    }
}

private fun RegisterKeyRequest.toModel(): RegisterNewKey {
    return RegisterNewKey(
        userId = userId,
        typeKey = when (typeKey) {
            TypeKey.UNKNOWN_TYPE_KEY -> null
            else -> TypeKey.valueOf(typeKey.name)
        },
        keyValue = keyValue,
        typeAccount = when (typeAccount) {
            TypeAccount.UNKNOWN_TYPE_ACCOUNT -> null
            else -> TypeAccount.valueOf(typeAccount.name)
        }
    )
}
