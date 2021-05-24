package br.com.zup.edu.register

import br.com.zup.edu.*
import br.com.zup.edu.bcb.*
import br.com.zup.edu.itau.*
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class KeyRegisterServerTest(
    @Inject val repository: KeyRepository,
    @Inject val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
) {

    /**
     *  1 - happy path
     *  2 - invalid key
     *  3 - validations
     */

    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    private val CUSTOMER_ID = UUID.randomUUID().toString()
    private val INSTITUTION = InstitutionResponse("Itau", "60701190")
    private val CUSTOMER_KEY_VALUE = "test@mail.com"
    private val CREATE_PIX_BCB_REQUEST = CreatePixKeyRequest(
        TypeKey.EMAIL, CUSTOMER_KEY_VALUE,
        BankAccountRequest("60701190", "0001", "000000", AccountType.CACC),
        OwnerRequest(OwnerType.NATURAL_PERSON, "NAME", "00000000000")
    )

    private val CREATE_PIX_BCB_RESPONSE = CreatePixKeyResponse(
        TypeKey.EMAIL, CUSTOMER_KEY_VALUE,
        BankAccountResponse("60701190", "0001", "000000", AccountType.CACC),
        OwnerResponse(OwnerType.NATURAL_PERSON, "NAME", "00000000000"),
        "2020202020"
    )

    @BeforeEach
    internal fun setup() {

    }

    @AfterEach
    internal fun teardown() {
        repository.deleteAll()
    }

    @Test
    internal fun `must register new pix key`() {
        Mockito.`when`(itauClient.fetchAccountsByType(CUSTOMER_ID, TypeAccount.CONTA_POUPANCA.toString()))
            .thenReturn(
                ClientDetails(
                    TypeAccount.CONTA_POUPANCA,
                    INSTITUTION,
                    "0001",
                    "000000",
                    ClientHolder("id", "name", "cpf")
                )
            )

        Mockito
            .`when`(bcbClient.registerPixKeyBcb(CREATE_PIX_BCB_REQUEST))
            .thenReturn(HttpResponse.created(CREATE_PIX_BCB_RESPONSE))

        val result = grpcClient.registerKey(
            RegisterKeyRequest.newBuilder()
                .setUserId(CUSTOMER_ID)
                .setTypeKey(TypeKey.EMAIL)
                .setKeyValue(CUSTOMER_KEY_VALUE)
                .setTypeAccount(TypeAccount.CONTA_POUPANCA)
                .build()
        )

        assertEquals(result.userId, CUSTOMER_ID)
    }

    @MockBean(ItauClient::class)
    fun fetchClient(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        fun registerBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }
}