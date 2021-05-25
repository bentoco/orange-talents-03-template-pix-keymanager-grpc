package br.com.zup.edu.list

import br.com.zup.edu.*
import br.com.zup.edu.shared.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class KeyListEndpoint(
   @Inject private val repository: KeyRepository
) : KeyListServiceGrpc.KeyListServiceImplBase() {
    override fun listKey(request: ListKeyRequest, responseObserver: StreamObserver<ListKeyResponse>) {
        if(request.userId.isNullOrBlank())
            throw IllegalArgumentException("user_id must not be blank")

        val keys = repository.findAllByUserId(request.userId).map {
            ListKeyResponse.PixKey.newBuilder()
                .setPixId(it.id)
                .setType(TypeKey.valueOf(it.typeKey.name))
                .setKey(it.keyValue)
                .setTypeAccount(TypeAccount.valueOf(it.typeAccount.name))
                .setCreatedAt(it.createdAt.let {
                   val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                       Timestamp.newBuilder()
                           .setSeconds(createdAt.epochSecond)
                           .setNanos(createdAt.nano)
                           .build()
                })
                .build()
        }

        responseObserver.onNext(ListKeyResponse.newBuilder()
            .setUserId(request.userId)
            .addAllKeys(keys)
            .build())
        responseObserver.onCompleted()
    }
}