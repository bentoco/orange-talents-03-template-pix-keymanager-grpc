package br.com.zup.edu.register

import br.com.zup.edu.*
import br.com.zup.edu.bcb.*
import br.com.zup.edu.itau.ItauClient
import br.com.zup.edu.shared.NotFoundClientException
import br.com.zup.edu.shared.RegisterAlreadyExistsException
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class KeyRegisterService(
    @Inject private val repository: KeyRepository,
    @Inject private val keyValueValidator: KeyValueValidator,
    @Inject private val itauClient: ItauClient,
    @Inject private val bcbClient: BcbClient,
) {
    fun registerKey(
        newKey: NewKey
    ): Key {

        keyRegisterValid(newKey)

        val account = submitForConsult(newKey)

        val request = newKey.toBcb(account)

        val response = bcbClient.registerPixKeyBcb(request)

        when (response.status.code) {
            201 -> {
                val key = newKey.toModel(response.body()!!, account)
                repository.save(key)
                return key
            }
            422 -> throw RegisterAlreadyExistsException("the informed pix key already exists")
            else -> throw Exception("unexpected error")
        }
    }

    private fun submitForConsult(request: NewKey): AssociatedAccount {
        val response = itauClient.fetchAccountsByType(request.userId, request.typeAccount.toString())
        when (response.status.code) {
            200 -> return response.body()!!.toModel()
            404 -> throw NotFoundClientException("account not found")
            else -> throw Exception("unexpected error")
        }
    }

    private fun keyRegisterValid(@Valid request: NewKey): Boolean {
        if (request.typeKey == TypeKey.UNKNOWN_TYPE_KEY || request.typeAccount == TypeAccount.UNKNOWN_TYPE_ACCOUNT)
            throw IllegalArgumentException("invalid input data")

        val existsKey = when (request.typeKey) {
            TypeKey.RANDOM -> repository.existsByUserIdAndTypeKeyEquals(request.userId, request.typeKey)
            else -> repository.existsByKeyValue(request.keyValue!!)
        }
        if (existsKey) throw RegisterAlreadyExistsException("key value and type already register")

        val validatorResult: Boolean = keyValueValidator.validator(request.keyValue, request.typeKey)
        if (!validatorResult && request.typeKey != TypeKey.RANDOM)
            throw IllegalArgumentException("invalid input key value")

        return true
    }
}

fun NewKey.toBcb(account: AssociatedAccount): CreatePixKeyRequest {
    val bankAccountRequest = BankAccountRequest(
        participant = account.institutionIspb,
        branch = account.branch,
        accountNumber = account.accountNumber,
        accountType = when (typeAccount) {
            TypeAccount.CONTA_CORRENTE -> AccountType.CACC
            TypeAccount.CONTA_POUPANCA -> AccountType.SVGS
            else -> throw IllegalArgumentException("type account must no be blank")
        }
    )

    val ownerRequest = OwnerRequest(
        type = OwnerType.NATURAL_PERSON,
        name = account.ownerName,
        taxIdNumber = account.ownerCpf
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

