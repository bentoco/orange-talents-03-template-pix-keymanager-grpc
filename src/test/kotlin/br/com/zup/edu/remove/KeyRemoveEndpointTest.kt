package br.com.zup.edu.remove

import br.com.zup.edu.*
import br.com.zup.edu.bcb.*
import br.com.zup.edu.itau.AccountDataResponse
import br.com.zup.edu.itau.AccountOwnerResponse
import br.com.zup.edu.itau.InstitutionResponse
import br.com.zup.edu.itau.ItauClient
import br.com.zup.edu.register.AssociatedAccount
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyRemoveEndpointTest(
    @Inject private val repository: KeyRepository,
    @Inject private val grpcClient: KeyRemoveServiceGrpc.KeyRemoveServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BcbClient

    private val CUSTOMER_ID = UUID.randomUUID().toString()
    private val CUSTOMER_KEY_VALUE = "test@mail.com"

    private val key = Key(
        CUSTOMER_ID,
        TypeKey.EMAIL,
        CUSTOMER_KEY_VALUE,
        TypeAccount.CONTA_POUPANCA,
        AssociatedAccount(
            "Itau",
            "60701190",
            "0001",
            "0000001",
            CUSTOMER_ID,
            "Foo",
            "12345678910"
        )
    )

    private fun deletePixKeyRequest() = DeletePixKeyRequest(
        key = CUSTOMER_KEY_VALUE,
        participant = "60701190"
    )

    private fun deletePixKeyResponse() = DeletePixKeyResponse(
        key = CUSTOMER_KEY_VALUE,
        participant = "60701190",
        deletedAt = LocalDateTime.now().toString()
    )

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    internal fun `must remove a register key`() {

        repository.save(key)

        Mockito.`when`(bcbClient.removePixKeyBcb(key.keyValue.toString(), deletePixKeyRequest()))
            .thenReturn(HttpResponse.ok(deletePixKeyResponse()))

        val result = grpcClient.removeKey(
            RemoveKeyRequest.newBuilder()
                .setUserId(CUSTOMER_ID)
                .setPixId(CUSTOMER_KEY_VALUE)
                .build()
        )

        with(result) {
            assertEquals("key removed successful", result.message)
            assertFalse(repository.existsByKeyValue(CUSTOMER_KEY_VALUE))
        }

    }

    @Test
    internal fun `must not register when user id is invalid`() {
        repository.save(key)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setUserId(UUID.randomUUID().toString())
                    .setPixId(CUSTOMER_KEY_VALUE)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.PERMISSION_DENIED.code, exception.status.code)
            assertEquals("forbidden to perform operation", exception.status.description)
        }
    }

    @Test
    internal fun `must not register an invalid pix id`() {
        repository.save(key)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setUserId(CUSTOMER_ID)
                    .setPixId(UUID.randomUUID().toString())
                    .build()
            )
        }

        with(exception){
            assertEquals(Status.NOT_FOUND.code, exception.status.code)
            assertEquals("key not found", exception.status.description)
        }

    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun removeBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyRemoveServiceGrpc.KeyRemoveServiceBlockingStub? {
            return KeyRemoveServiceGrpc.newBlockingStub(channel)
        }
    }
}