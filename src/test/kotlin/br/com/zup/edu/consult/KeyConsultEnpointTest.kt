package br.com.zup.edu.consult

import br.com.zup.edu.*
import br.com.zup.edu.bcb.*
import br.com.zup.edu.register.AssociatedAccount
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
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
internal class KeyConsultEnpointTest(
    private val repository: KeyRepository,
    private val grpcClient: KeyConsultServiceGrpc.KeyConsultServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CUSTOMER_ID = UUID.randomUUID().toString()
        val ACCOUNT = AssociatedAccount(
            "Itau",
            "60701190",
            "0001",
            "0000001",
            CUSTOMER_ID,
            "Foo",
            "12345678910"
        )
    }

    @BeforeEach
    internal fun setup() {
        repository.save(Key(CUSTOMER_ID, TypeKey.EMAIL, "foo@mail.com", TypeAccount.CONTA_POUPANCA, ACCOUNT))
        repository.save(Key(CUSTOMER_ID, TypeKey.CPF, "34323227850", TypeAccount.CONTA_POUPANCA, ACCOUNT))
        repository.save(
            Key(
                UUID.randomUUID().toString(),
                TypeKey.RANDOM,
                "randomkey-1",
                TypeAccount.CONTA_POUPANCA,
                ACCOUNT
            )
        )
        repository.save(Key(CUSTOMER_ID, TypeKey.PHONE, "+55961667748", TypeAccount.CONTA_POUPANCA, ACCOUNT))
    }

    @AfterEach
    internal fun cleanup() {
        repository.deleteAll()
    }

    @Test
    internal fun `must return key by pixId and userId`() {
        val existsKey = repository.findByKeyValue("+55961667748").get()

        val response = grpcClient.consultKey(
            ConsultKeyRequest.newBuilder()
                .setPixId(
                    ConsultKeyRequest.PixIdFilter.newBuilder()
                        .setUserId(existsKey.userId)
                        .setPixId(existsKey.id)
                        .build()
                ).build()
        )

        with(response) {
            assertEquals(existsKey.id, this.pixId)
            assertEquals(existsKey.userId, this.userId)
            assertEquals(existsKey.typeKey.name, this.key.type.name)
            assertEquals(existsKey.keyValue, this.key.key)
        }

    }

    @Test
    internal fun `must not return key by pixId and userId when not found`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultKey(
                ConsultKeyRequest.newBuilder()
                    .setPixId(
                        ConsultKeyRequest.PixIdFilter.newBuilder()
                            .setUserId(UUID.randomUUID().toString())
                            .setPixId(UUID.randomUUID().toString())
                            .build()
                    ).build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("pix key not found", status.description)
        }
    }

    @Test
    internal fun `must return key by value when register exists`() {

        val existsKey = repository.findByKeyValue("foo@mail.com").get()

        val response = grpcClient.consultKey(
            ConsultKeyRequest.newBuilder()
                .setKey("foo@mail.com")
                .build()
        )


        with(response) {
            assertEquals(existsKey.id, this.pixId)
            assertEquals(existsKey.userId, this.userId)
            assertEquals(existsKey.typeKey.name, this.key.type.name)
            assertEquals(existsKey.keyValue, this.key.key)
        }
    }

    @Test
    internal fun `must return key by value when register not found locally but exists at BCB`() {
        val bcbResponse = pixKeyDetailsResponse()

        Mockito.`when`(bcbClient.findByKey(key = "user.from.another.bank@mail.com"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        val response = grpcClient.consultKey(
            ConsultKeyRequest.newBuilder()
                .setKey("user.from.another.bank@mail.com").build()
        )

        with(response) {
            assertEquals("", this.pixId)
            assertEquals("", this.userId)
            assertEquals(bcbResponse.keyType.name, this.key.type.name)
            assertEquals(bcbResponse.key, this.key.key)
        }
    }

    @Test
    internal fun `must not retun key by value when not found locally and BCB`() {
        Mockito.`when`(bcbClient.findByKey(key = "not.existing@mail.com"))
            .thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultKey(
                ConsultKeyRequest.newBuilder()
                    .setKey("not.existing@mail.com").build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("pix key not found", status.description)
        }
    }

    @Test
    internal fun `must not return key when filter is invalid`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultKey(ConsultKeyRequest.newBuilder().build())
        }

        with(exception){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("invalid pix key or not informed", status.description)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun consultBlocking(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyConsultServiceGrpc.KeyConsultServiceBlockingStub {
            return KeyConsultServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun pixKeyDetailsResponse() = PixKeyDetailsResponse(
        TypeKey.EMAIL,
        "user.from.another.bank@mail.com",
        BankAccountResponse("123456789", "0002", "012034", AccountType.CACC),
        OwnerResponse(OwnerType.NATURAL_PERSON, "Bar", "121345204450"),
        LocalDateTime.now().toString()
    )
}