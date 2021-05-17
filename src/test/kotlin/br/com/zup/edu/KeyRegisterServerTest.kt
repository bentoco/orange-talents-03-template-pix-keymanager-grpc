package br.com.zup.edu

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import javax.inject.Singleton

/**
 * 1 - register random key [x]
 * 2 - register cpf, email, phone_number keys [x]
 * 3 - try to register a repeated random key
 * 4 - try to register a repeated cpf, email, phone_number key
 */

@MicronautTest(transactional = false)
internal class KeyRegisterServerTest(
    val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub,
    val repository: KeyRegisterRepository
) {
    @Test
    internal fun `register random key`() {
        //scenario
        repository.deleteAll()

        //action
        val result = grpcClient.registerKey(
            RegisterKeyRequest
                .newBuilder()
                .setUserId(UUID.randomUUID().toString())
                .setTypeKey(TypeKey.RANDOM_KEY)
                .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                .build()
        )
        //asserts
        with(result) {
            assertEquals(1, repository.count())
            assertNotNull(pixId)
        }
    }

    @ParameterizedTest(name = "should register cpf, email, phone_number keys")
    @MethodSource("getRequest")
    internal fun `register cpf email phone_number keys`(keyRegister: KeyRegister) {
        repository.deleteAll()

        val result = grpcClient.registerKey(
            RegisterKeyRequest
                .newBuilder()
                .setUserId(keyRegister.id)
                .setTypeKey(keyRegister.typeKey)
                .setKeyValue(keyRegister.keyValue)
                .setTypeAccount(keyRegister.typeAccount)
                .build()
        )
        with(result) {
            assertEquals(1, repository.count())
            assertNotNull(pixId)
        }
    }

    companion object {
        @JvmStatic
        fun getRequest() = listOf<KeyRegister>(
            KeyRegister(
                UUID.randomUUID().toString(),
                TypeKey.CPF,
                "12312312312",
                TypeAccount.CONTA_CORRENTE
            ),
            KeyRegister(
                UUID.randomUUID().toString(),
                TypeKey.EMAIL,
                "foo@mail.com",
                TypeAccount.CONTA_CORRENTE
            ),
            KeyRegister(
                UUID.randomUUID().toString(),
                TypeKey.PHONE_NUMBER,
                "+55999999999",
                TypeAccount.CONTA_CORRENTE
            )
        )

    }

    @Test
    internal fun `must not register repeated random key`() {
        val randomKey = KeyRegister(
            UUID.randomUUID().toString(),
            TypeKey.RANDOM_KEY,
            "",
            TypeAccount.CONTA_CORRENTE
        )
        repository.save(randomKey)

        val exception = assertThrows<StatusRuntimeException>(){
            grpcClient.registerKey(
                RegisterKeyRequest
                    .newBuilder()
                    .setUserId(randomKey.id)
                    .setTypeKey(TypeKey.RANDOM_KEY)
                    .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                    .build()
            )
        }
        with(exception){
            assertEquals(Status.ALREADY_EXISTS, status.code.toStatus())
            assertEquals("key value and type already register",status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }
}