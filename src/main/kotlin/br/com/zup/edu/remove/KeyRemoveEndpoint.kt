package br.com.zup.edu.remove

import br.com.zup.edu.KeyRemoveServiceGrpc
import br.com.zup.edu.RemoveKeyRequest
import br.com.zup.edu.RemoveKeyResponse
import br.com.zup.edu.bcb.DeletePixKeyRequest
import br.com.zup.edu.shared.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class KeyRemoveEndpoint(@Inject private val service: KeyRemoveService) :
    KeyRemoveServiceGrpc.KeyRemoveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun removeKey(request: RemoveKeyRequest?, responseObserver: StreamObserver<RemoveKeyResponse>?) {

        logger.error("New request: $request")

        val keyToRemove = request!!.toKey()
        service.validKey(keyToRemove)
        service.removeKey(keyToRemove)

        responseObserver?.onNext(
            RemoveKeyResponse
                .newBuilder()
                .setMessage("key removed successful")
                .setPixId(keyToRemove.pixId)
                .setUserId(keyToRemove.userId)
                .build()
        )
        responseObserver?.onCompleted()
    }
}

private fun RemoveKeyRequest.toKey(): RegisterToRemove {
    if (userId.isBlank() || pixId.isBlank())
        throw IllegalArgumentException("pix and user must not be blank")
    return RegisterToRemove(
        userId = this.userId,
        pixId = this.pixId
    )
}

data class RegisterToRemove(
    val pixId: String,
    val userId: String
)

