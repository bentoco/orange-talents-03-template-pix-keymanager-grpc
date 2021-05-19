package br.com.zup.edu

import br.com.zup.edu.client.Account
import br.com.zup.edu.client.ClienteInstitution
import br.com.zup.edu.client.FetchClient
import br.com.zup.edu.register.AssociatedAccount
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false, rollback = false)
internal class KeyRemoveEndpointTest {

    @Inject
    lateinit var clientRemove: KeyRemoveServiceGrpc.KeyRemoveServiceBlockingStub

    @Inject
    lateinit var repository: KeyRepository

    @Inject
    lateinit var mockFetchClient: FetchClient

    private val CUSTOMER_ID = UUID.randomUUID().toString()
    private val INSTITUTION = ClienteInstitution("NAME", "ISPB")
    private val CUSTOMER_KEY_VALUE = "foo@mail.com"

    @BeforeEach
    fun setUp() {
        Mockito
            .`when`(mockFetchClient.fetchAccount(CUSTOMER_ID))
            .thenReturn(Account(CUSTOMER_ID, "NAME", "CPF", INSTITUTION))
    }

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    /**
     * 1 - test invalid user id expect exception
     * 2 - test invalid pix id expect exception
     * 3 - test nullables
     * 4 - test valid values
     */

    @MockBean(FetchClient::class)
    fun fetchClient(): FetchClient {
        return Mockito.mock(FetchClient::class.java)
    }

    @Test
    internal fun `test valid values`() {
        val keyRegister = Key(
            CUSTOMER_ID,
            TypeKey.EMAIL,
            CUSTOMER_KEY_VALUE,
            TypeAccount.CONTA_CORRENTE,
            AssociatedAccount(
                "INSTITUTION",
                "FOO",
                "CPF",
                "AGENCY",
                "NUMBER"
            )
        )
        repository.save(keyRegister)

        val removeKey = clientRemove.removeKey(
            RemoveKeyRequest.newBuilder()
                .setPixId(CUSTOMER_KEY_VALUE)
                .setUserId(CUSTOMER_ID)
                .build()
        )
        assertEquals(removeKey.message, "key removed successful")
        assertTrue(repository.findByUserIdAndKeyValue(CUSTOMER_ID, CUSTOMER_KEY_VALUE).isEmpty)
    }

    @Test
    internal fun `test invalid user id`() {
        val exception = assertThrows<StatusRuntimeException> {
            clientRemove.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setPixId(UUID.randomUUID().toString())
                    .setUserId("INVALID USER ID")
                    .build()
            )
        }
        with(exception) {
            assertEquals(exception.status.description, "account not found")
            assertEquals(Status.NOT_FOUND, status.code.toStatus())
        }

    }

    @Test
    internal fun `test invalid pix id`() {
        val exception = assertThrows<StatusRuntimeException> {
            clientRemove.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setPixId("INVALID USER ID")
                    .setUserId(CUSTOMER_ID)
                    .build()
            )
        }
        with(exception) {
            assertEquals(exception.status.description, "register not found")
            assertEquals(Status.NOT_FOUND, status.code.toStatus())
        }

    }

    @Test
    internal fun `test null pix id`() {
        val exception = assertThrows<StatusRuntimeException> {
            clientRemove.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setPixId("")
                    .setUserId(CUSTOMER_ID)
                    .build()
            )
        }
        with(exception) {
            assertEquals(exception.status.description, "pix and user must not be blank")
            assertEquals(Status.INVALID_ARGUMENT, status.code.toStatus())
        }

    }

    @Test
    internal fun `test null user id`() {
        val exception = assertThrows<StatusRuntimeException> {
            clientRemove.removeKey(
                RemoveKeyRequest.newBuilder()
                    .setPixId(UUID.randomUUID().toString())
                    .setUserId("")
                    .build()
            )
        }
        with(exception) {
            assertEquals(exception.status.description, "pix and user must not be blank")
            assertEquals(Status.INVALID_ARGUMENT, status.code.toStatus())
        }

    }

    @Factory
    class Clients {
        @Singleton
        fun removeBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyRemoveServiceGrpc.KeyRemoveServiceBlockingStub? {
            return KeyRemoveServiceGrpc.newBlockingStub(channel)
        }
    }
}