package br.com.zup.edu.register

import br.com.zup.edu.*
import br.com.zup.edu.bcb.*
import br.com.zup.edu.shared.ErrorHandler
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Premises to generate key
 *
 * 1 - need to a valid keyValue (validated by regex or null if random key)
 * 2 - must not exists a generated key for same userId and keytype
 *
 * To-do
 *
 * 1 - check UNKNOWN TYPES and nullables values
 * 2 - check if already exists key generated
 * 3 - fetch account at external itau service
 * 4 - valid the key value
 */
@ErrorHandler
@Singleton
@Validated
class KeyRegisterServer(
    @Inject private val repository: KeyRepository,
    @Inject private val service: KeyRegisterService
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun registerKey(request: RegisterKeyRequest, responseObserver: StreamObserver<RegisterKeyResponse>?) {

        LOGGER.info("New request: $request")

        val newKey = request.toKey()

        /**
         *Validates the data informed for registration
         */
        service.keyRegisterValid(newKey)

        /**
         * Consult accounts on the Itau external service
         */
        val account = service.submitForConsult(newKey)

        /**
         * Transforms into object to BCB Request
         */
        val toBcbRequest = newKey.toBcb(account)

        /**
         * Send request to BCB external service
         */
        val bcbResponse = service.createPixKeyBcb(toBcbRequest)

        val key = newKey.toModel(bcbResponse, account)
        repository.save(key)
        responseObserver?.onNext(
            RegisterKeyResponse.newBuilder()
                .setPixId(key.keyValue)
                .setUserId(key.userId)
                .build()
        )
        responseObserver?.onCompleted()
        return
    }
}

/**
 * Extension methods
 */

private fun RegisterKeyRequest.toKey(): NewKey {
    return NewKey(
        userId = userId,
        typeKey = typeKey,
        keyValue = when (typeKey) {
            TypeKey.UNKNOWN_TYPE_KEY -> null
            TypeKey.RANDOM -> null
            else -> keyValue
        },
        typeAccount = typeAccount
    )
}

private fun NewKey.toBcb(account: AssociatedAccount): CreatePixKeyRequest {
    val bankAccountRequest = BankAccountRequest(
        participant = account.ispb,
        branch = account.agency,
        accountNumber = account.accountNumber,
        accountType = when (typeAccount) {
            TypeAccount.CONTA_CORRENTE -> AccountType.CACC
            TypeAccount.CONTA_POUPANCA -> AccountType.SVGS
            else -> throw IllegalArgumentException("type account must no be blank")
        }
    )

    val ownerRequest = OwnerRequest(
        type = OwnerType.NATURAL_PERSON,
        name = account.holderName,
        taxIdNumber = account.holderCpf
    )

    return CreatePixKeyRequest(
        keyType = this.typeKey,
        key = when (typeKey) {
            TypeKey.RANDOM -> null
            TypeKey.UNKNOWN_TYPE_KEY -> null
            else -> this.keyValue
        },
        bankAccountRequest,
        ownerRequest
    )
}

