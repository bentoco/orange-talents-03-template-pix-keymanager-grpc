package br.com.zup.edu.remove

import br.com.zup.edu.KeyRepository
import br.com.zup.edu.KeyRemoveServiceGrpc
import br.com.zup.edu.RemoveKeyRequest
import br.com.zup.edu.RemoveKeyResponse
import br.com.zup.edu.client.Account
import br.com.zup.edu.client.FetchClient
import br.com.zup.edu.shared.ErrorHandler
import br.com.zup.edu.shared.NotFoundClientException
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.transaction.SynchronousTransactionManager
import org.slf4j.LoggerFactory
import java.sql.Connection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class KeyRemoveEndpoint
    (
    @Inject val client: FetchClient,
    @Inject val repository: KeyRepository,
    @Inject val transactionManager: SynchronousTransactionManager<Connection>,
) :
    KeyRemoveServiceGrpc.KeyRemoveServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun removeKey(request: RemoveKeyRequest?, responseObserver: StreamObserver<RemoveKeyResponse>?) {

        logger.error("New request: $request")

        val register = request?.toRegister()
        validIfExist(register!!.userId, client)
        transactionManager.executeWrite {
            val keyToRemove = repository.findByUserIdAndKeyValue(register.userId, register.pixId)
            if (keyToRemove.isEmpty)
                throw NotFoundClientException("register not found")
            repository.deleteById(keyToRemove.get().id)
        }
        responseObserver?.onNext(RemoveKeyResponse.newBuilder().setMessage("key removed successful").build())
        responseObserver?.onCompleted()
    }
}

private fun validIfExist(userId: String, client: FetchClient): Account {
    return try {
        client.fetchAccount(userId)
    } catch (e: HttpClientResponseException) {
        println("Status: ${e.status}")
        println("Message: ${e.message}")
        null
    } ?: throw NotFoundClientException("account not found")
}

private fun RemoveKeyRequest.toRegister(): RegisterToRemove {
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

