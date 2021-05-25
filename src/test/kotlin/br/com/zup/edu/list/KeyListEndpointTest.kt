package br.com.zup.edu.list

import br.com.zup.edu.*
import br.com.zup.edu.consult.KeyConsultEnpointTest
import br.com.zup.edu.register.AssociatedAccount
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyListEndpointTest(
    private val repository: KeyRepository,
    private val grpcClient: KeyListServiceGrpc.KeyListServiceBlockingStub
) {

    companion object {
        val CUSTOMER_ID = UUID.randomUUID().toString()
        val ACCOUNT = AssociatedAccount(
            "Itau",
            "60701190",
            "0001",
            "0000001",
            KeyConsultEnpointTest.CUSTOMER_ID,
            "Foo",
            "12345678910"
        )
    }

    @BeforeEach
    internal fun setup() {
        repository.save(
            Key(
                CUSTOMER_ID,
                TypeKey.EMAIL,
                "foo@mail.com",
                TypeAccount.CONTA_POUPANCA,
                ACCOUNT
            )
        )
        repository.save(
            Key(
                CUSTOMER_ID,
                TypeKey.CPF,
                "34323227850",
                TypeAccount.CONTA_POUPANCA,
                ACCOUNT
            )
        )
        repository.save(
            Key(
                UUID.randomUUID().toString(),
                TypeKey.RANDOM,
                "randomkey-1",
                TypeAccount.CONTA_POUPANCA,
                ACCOUNT
            )
        )
        repository.save(
            Key(
                CUSTOMER_ID,
                TypeKey.PHONE,
                "+55961667748",
                TypeAccount.CONTA_POUPANCA,
                ACCOUNT
            )
        )
    }

    @AfterEach
    internal fun cleanup() {
        repository.deleteAll()
    }

    @Test
    internal fun `must return a key list of user id informed`() {
        val response = grpcClient.listKey(ListKeyRequest.newBuilder().setUserId(CUSTOMER_ID).build())
        with(response.keysList) {
            assertThat(this, hasSize(3))
            assertThat(
                this.map { Pair(it.type, it.key) }.toList(),
                containsInAnyOrder(
                    Pair(TypeKey.EMAIL, "foo@mail.com"),
                    Pair(TypeKey.CPF, "34323227850"),
                    Pair(TypeKey.PHONE, "+55961667748")
                )
            )
        }
    }

    @Test
    internal fun `must not return list of keys when users do not have`() {
        val randomUserId = UUID.randomUUID().toString()
        val response = grpcClient.listKey(ListKeyRequest.newBuilder().setUserId(randomUserId).build())
        assertEquals(0, response.keysCount)
    }

    @Test
    internal fun `must not return all keys when user id was invalid`() {
        val invalidUserId = ""


        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.listKey(ListKeyRequest.newBuilder().setUserId(invalidUserId).build())
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("user_id must not be blank", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun listBlocking(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyListServiceGrpc.KeyListServiceBlockingStub {
            return KeyListServiceGrpc.newBlockingStub(channel)
        }
    }
}