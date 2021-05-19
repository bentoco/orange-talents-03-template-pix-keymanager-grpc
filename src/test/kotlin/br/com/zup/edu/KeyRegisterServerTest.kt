package br.com.zup.edu

import br.com.zup.edu.client.ClientDetails
import br.com.zup.edu.client.ClientHolder
import br.com.zup.edu.client.ClienteInstitution
import br.com.zup.edu.client.FetchClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false, rollback = false)
internal class KeyRegisterServerTest {

    @Inject
    lateinit var client: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub

    @Inject
    lateinit var repository: KeyRegisterRepository

    @Inject
    lateinit var mockFetchClient: FetchClient

    private val CUSTOMER_ID = UUID.randomUUID().toString()
    private val CUSTOMER_TYPE_ACCOUNT = TypeAccount.CONTA_CORRENTE
    private val CUSTOMER_KEY_VALUE = "foo@mail.com"

    private val institution = ClienteInstitution("name", "ispb")
    private val holder = ClientHolder("ID", "NOME", CUSTOMER_ID)
    private val details = ClientDetails(
        CUSTOMER_TYPE_ACCOUNT, institution, "AGENCY", "NUMBER", holder
    )

    @BeforeEach
    fun setup() {
        Mockito
            .`when`(mockFetchClient.fetchAccountsByType(CUSTOMER_ID, CUSTOMER_TYPE_ACCOUNT.toString()))
            .thenReturn(details)
    }

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    @Test
    internal fun `find key register by key value`() {
        val account = AssociatedAccount(
            "INSTITUTION",
            "FOO",
            "CPF",
            "AGENCY",
            "NUMBER"
        )

        val validKeyRegister = KeyRegister(
            CUSTOMER_ID,
            TypeKey.EMAIL,
            CUSTOMER_KEY_VALUE,
            TypeAccount.CONTA_POUPANCA,
            account
        )

        repository.save(validKeyRegister)
        val result = repository.existsByKeyValue(CUSTOMER_KEY_VALUE)
        assertTrue(result)
    }

    @Test
    internal fun `find key register by key value and type key`() {
        val account = AssociatedAccount(
            "INSTITUTION",
            "FOO",
            "CPF",
            "AGENCY",
            "NUMBER"
        )

        val validKeyRegister = KeyRegister(
            CUSTOMER_ID,
            TypeKey.EMAIL,
            CUSTOMER_KEY_VALUE,
            TypeAccount.CONTA_POUPANCA,
            account
        )

        repository.save(validKeyRegister)
        val result = repository.existsByUserIdAndTypeKeyEquals(CUSTOMER_ID, TypeKey.EMAIL)
        assertTrue(result)
    }

    @Test
    internal fun `register random key`() {
        val result = client.registerKey(
            RegisterKeyRequest
                .newBuilder()
                .setUserId(CUSTOMER_ID)
                .setTypeKey(TypeKey.RANDOM_KEY)
                .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                .build()
        )
        with(result) {
            assertEquals(1, repository.count())
            assertNotNull(pixId)
        }
    }

    @Test
    internal fun `register valid cpf key`() {
        val result = client.registerKey(
            RegisterKeyRequest
                .newBuilder()
                .setUserId(CUSTOMER_ID)
                .setTypeKey(TypeKey.CPF)
                .setKeyValue("12345678910")
                .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                .build()
        )
        with(result) {
            assertEquals(1, repository.count())
            assertNotNull(pixId)
            assertEquals(CUSTOMER_ID, repository.findAll()[0].userId)
        }
    }

    @Test
    internal fun `register valid email key`() {
        val result = client.registerKey(
            RegisterKeyRequest
                .newBuilder()
                .setUserId(CUSTOMER_ID)
                .setTypeKey(TypeKey.EMAIL)
                .setKeyValue("foo@bar.com")
                .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                .build()
        )
        with(result) {
            assertEquals(1, repository.count())
            assertNotNull(pixId)
            assertEquals(CUSTOMER_ID, repository.findAll()[0].userId)
        }
    }

    @Test
    internal fun `register valid phone_number key`() {
        val result = client.registerKey(
            RegisterKeyRequest
                .newBuilder()
                .setUserId(CUSTOMER_ID)
                .setTypeKey(TypeKey.PHONE_NUMBER)
                .setKeyValue("+5511941661148")
                .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                .build()
        )
        with(result) {
            assertEquals(1, repository.count())
            assertNotNull(pixId)
            assertEquals(CUSTOMER_ID, repository.findAll()[0].userId)
        }
    }

    companion object {
        fun getRequest() = {

        }
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = ["0", "234", "666.666.666-66", "666666666-66"])
    internal fun `must not register invalid cpf`(cpf: String) {

        val exception = assertThrows<StatusRuntimeException>() {
            client.registerKey(
                RegisterKeyRequest
                    .newBuilder()
                    .setUserId(CUSTOMER_ID)
                    .setTypeKey(TypeKey.CPF)
                    .setKeyValue(cpf)
                    .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                    .build()
            )
        }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT, status.code.toStatus())
            assertEquals("invalid input key value", status.description)
        }
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = ["foo", "foomailcom", "12345_12412", "@"])
    internal fun `must not register invalid email`(email: String) {

        val exception = assertThrows<StatusRuntimeException>() {
            client.registerKey(
                RegisterKeyRequest
                    .newBuilder()
                    .setUserId(CUSTOMER_ID)
                    .setTypeKey(TypeKey.EMAIL)
                    .setKeyValue(email)
                    .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                    .build()
            )
        }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT, status.code.toStatus())
            assertEquals("invalid input key value", status.description)
        }
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = ["1", "234", "111111", "+55119416611119991"])
    internal fun `must not register invalid phone_number`(phone: String) {

        val exception = assertThrows<StatusRuntimeException>() {
            client.registerKey(
                RegisterKeyRequest
                    .newBuilder()
                    .setUserId(CUSTOMER_ID)
                    .setTypeKey(TypeKey.PHONE_NUMBER)
                    .setKeyValue(phone)
                    .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                    .build()
            )
        }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT, status.code.toStatus())
            assertEquals("invalid input key value", status.description)
        }
    }

    @Test
    internal fun `must not register random key without null key value`() {
        val exception = assertThrows<StatusRuntimeException>() {
            client.registerKey(
                RegisterKeyRequest
                    .newBuilder()
                    .setUserId(CUSTOMER_ID)
                    .setTypeKey(TypeKey.RANDOM_KEY)
                    .setKeyValue("ANYTHING")
                    .setTypeAccount(TypeAccount.CONTA_CORRENTE)
                    .build()
            )
        }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT, status.code.toStatus())
            assertEquals("value key must be null for random key type", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(FetchClient::class)
    fun fetchClient(): FetchClient {
        return Mockito.mock(FetchClient::class.java)
    }
}