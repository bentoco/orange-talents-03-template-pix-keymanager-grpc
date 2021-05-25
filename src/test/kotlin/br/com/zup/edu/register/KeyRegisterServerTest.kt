package br.com.zup.edu.register

import br.com.zup.edu.*
import br.com.zup.edu.bcb.*
import br.com.zup.edu.itau.AccountDataResponse
import br.com.zup.edu.itau.AccountOwnerResponse
import br.com.zup.edu.itau.InstitutionResponse
import br.com.zup.edu.itau.ItauClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyRegisterServerTest(
    @Inject private val repository: KeyRepository,
    @Inject private val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
) {
    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    private val CUSTOMER_ID = UUID.randomUUID().toString()
    private val INSTITUTION = InstitutionResponse("Itau", "60701190")
    private val CUSTOMER_KEY_VALUE = "test@mail.com"

    private fun createPixKeyRequest() = CreatePixKeyRequest(
        TypeKey.EMAIL, CUSTOMER_KEY_VALUE,
        BankAccountRequest("60701190", "0001", "123456", AccountType.SVGS),
        OwnerRequest(OwnerType.NATURAL_PERSON, "Foo", "12345678910")
    )

    private fun createPixKeyResponse() = CreatePixKeyResponse(
        TypeKey.EMAIL, CUSTOMER_KEY_VALUE,
        BankAccountResponse("60701190", "0001", "123456", AccountType.SVGS),
        OwnerResponse(OwnerType.NATURAL_PERSON, "Foo", "12345678910"),
        LocalDateTime.now().toString()
    )

    private fun accountDataResponse() = AccountDataResponse(
        tipo = TypeAccount.CONTA_POUPANCA,
        instituicao = INSTITUTION,
        agencia = "0001",
        numero = "123456",
        titular = AccountOwnerResponse(
            id = CUSTOMER_ID,
            nome = "Foo",
            cpf = "12345678910"
        )
    )

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
    }

    @Test
    internal fun `must register new pix key`() {

        Mockito.`when`(itauClient.fetchAccountsByType(userId = CUSTOMER_ID, tipo = "CONTA_POUPANCA"))
            .thenReturn(HttpResponse.ok(accountDataResponse()))

        Mockito.`when`(bcbClient.registerPixKeyBcb(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        val result = grpcClient.registerKey(
            RegisterKeyRequest.newBuilder()
                .setUserId(CUSTOMER_ID)
                .setTypeKey(TypeKey.EMAIL)
                .setKeyValue(CUSTOMER_KEY_VALUE)
                .setTypeAccount(TypeAccount.CONTA_POUPANCA)
                .build()
        )
        with(result) {
            assertTrue(repository.existsByKeyValue(CUSTOMER_KEY_VALUE))
            assertNotNull(pixId)
        }

    }

    @Test
    internal fun `must not register already registered key`() {
        Mockito.`when`(itauClient.fetchAccountsByType(userId = CUSTOMER_ID, tipo = "CONTA_POUPANCA"))
            .thenReturn(HttpResponse.ok(accountDataResponse()))

        Mockito.`when`(bcbClient.registerPixKeyBcb(createPixKeyRequest()))
            .thenReturn(HttpResponse.unprocessableEntity())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerKey(
                RegisterKeyRequest.newBuilder()
                    .setUserId(CUSTOMER_ID)
                    .setTypeKey(TypeKey.EMAIL)
                    .setKeyValue(CUSTOMER_KEY_VALUE)
                    .setTypeAccount(TypeAccount.CONTA_POUPANCA)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, exception.status.code)
            assertEquals("the informed pix key already exists", exception.status.description)
        }
    }

    @Test
    internal fun `must not register when account not found`() {
        Mockito.`when`(itauClient.fetchAccountsByType(userId = CUSTOMER_ID, tipo = "CONTA_POUPANCA"))
            .thenReturn(HttpResponse.notFound())

        Mockito.`when`(bcbClient.registerPixKeyBcb(createPixKeyRequest()))
            .thenReturn(HttpResponse.badRequest())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerKey(
                RegisterKeyRequest.newBuilder()
                    .setUserId(CUSTOMER_ID)
                    .setTypeKey(TypeKey.EMAIL)
                    .setKeyValue(CUSTOMER_KEY_VALUE)
                    .setTypeAccount(TypeAccount.CONTA_POUPANCA)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, exception.status.code)
            assertEquals("account not found", exception.status.description)
        }

    }

    @Test
    internal fun `must not register invalid input data`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerKey(
                RegisterKeyRequest.newBuilder()
                    .setUserId(CUSTOMER_ID)
                    .setTypeKey(TypeKey.UNKNOWN_TYPE_KEY)
                    .setKeyValue(CUSTOMER_KEY_VALUE)
                    .setTypeAccount(TypeAccount.UNKNOWN_TYPE_ACCOUNT)
                    .build())
        }

        with(exception){
            assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
            assertEquals("invalid input data", exception.status.description)
        }
    }

    @Test
    internal fun `must not register nullables when not a random key`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerKey(
                RegisterKeyRequest.newBuilder()
                    .setUserId(CUSTOMER_ID)
                    .setTypeKey(TypeKey.EMAIL)
                    .setKeyValue("")
                    .setTypeAccount(TypeAccount.CONTA_POUPANCA)
                    .build())
        }

        with(exception){
            assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
            assertEquals("invalid input key value", exception.status.description)
        }
    }

    @MockBean(ItauClient::class)
    fun fetchClient(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun registerBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }
}
