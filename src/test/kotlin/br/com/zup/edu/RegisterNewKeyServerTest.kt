package br.com.zup.edu

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

/**
 * 1 - register random key [x]
 * 2 - register cpf, email, phone_number keys [x]
 * 3 - try to register a repeated random key [X]
 * 4 - try to register a repeated cpf, email, phone_number key
 */

@MicronautTest(transactional = false)
internal class RegisterNewKeyServerTest(
    val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub,
    val keyRepository: PixKeyRepository
) {
    @Test
    internal fun `register random key`() {
        //scenario
        keyRepository.deleteAll()

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
            assertEquals(1, keyRepository.count())
            assertNotNull(pixId)
        }
    }

    @ParameterizedTest(name = "should register cpf, email, phone_number keys")
    @MethodSource("getRequest")
    internal fun `register cpf email phone_number keys`(registerNewKey: RegisterNewKey) {
        keyRepository.deleteAll()

        val result = grpcClient.registerKey(
            RegisterKeyRequest
                .newBuilder()
                .setUserId(registerNewKey.id)
                .setTypeKey(registerNewKey.typeKey)
                .setKeyValue(registerNewKey.keyValue)
                .setTypeAccount(registerNewKey.typeAccount)
                .build()
        )
        with(result) {
            assertEquals(1, keyRepository.count())
            assertNotNull(pixId)
        }
    }

    @Test
    internal fun `must not register random key repeated`() {
        val userId = UUID.randomUUID().toString()

        grpcClient.registerKey(
            RegisterKeyRequest
                .newBuilder()
                .setUserId(userId)
                .setTypeKey(TypeKey.RANDOM_KEY)
                .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                .build()
        )

        val exception = assertThrows<StatusRuntimeException>("should throw an exception") {
            grpcClient.registerKey(
                RegisterKeyRequest
                    .newBuilder()
                    .setUserId(userId)
                    .setTypeKey(TypeKey.RANDOM_KEY)
                    .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                    .build()
            )
        }
        with(exception) {
            assertEquals(Status.ALREADY_EXISTS, status.code.toStatus())
            assertEquals("key value and type already register", status.description)
        }
    }

    @ParameterizedTest(name = "must not register cpf, email, phone_number keys")
    @MethodSource("getRequest")
    internal fun `must not register cpf email phone_number keys`(registerNewKey: RegisterNewKey) {
        keyRepository.deleteAll()

        grpcClient.registerKey(
            RegisterKeyRequest
                .newBuilder()
                .setUserId(registerNewKey.id)
                .setTypeKey(registerNewKey.typeKey)
                .setKeyValue(registerNewKey.keyValue)
                .setTypeAccount(registerNewKey.typeAccount)
                .build()
        )

        val exception = assertThrows<StatusRuntimeException>() {
            grpcClient.registerKey(
                RegisterKeyRequest
                    .newBuilder()
                    .setUserId(registerNewKey.id)
                    .setTypeKey(registerNewKey.typeKey)
                    .setKeyValue(registerNewKey.keyValue)
                    .setTypeAccount(registerNewKey.typeAccount)
                    .build()
            )
        }
        with(exception) {
            assertEquals(Status.ALREADY_EXISTS, status.code.toStatus())
            assertEquals("key value and type already register", status.description)
        }
    }


    companion object {
        @JvmStatic
        fun getRequest() = listOf<RegisterNewKey>(
            RegisterNewKey(
                UUID.randomUUID().toString(),
                TypeKey.CPF,
                "12312312312",
                TypeAccount.CONTA_CORRENTE
            ),
            RegisterNewKey(
                UUID.randomUUID().toString(),
                TypeKey.EMAIL,
                "foo@mail.com",
                TypeAccount.CONTA_CORRENTE
            ),
            RegisterNewKey(
                UUID.randomUUID().toString(),
                TypeKey.PHONE_NUMBER,
                "+55999999999",
                TypeAccount.CONTA_CORRENTE
            )
        )

    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }
}