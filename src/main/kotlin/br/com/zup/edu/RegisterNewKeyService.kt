package br.com.zup.edu

import br.com.zup.edu.handler.PixKeyAlreadyExistsException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RegisterNewKeyService(
    @Inject val repository: PixKeyRepository,
    @Inject val itauClient: ItauAccountsClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun register(@Valid newKey: RegisterNewKey?): PixKey {
        //1 - verify if key already exists
        if (repository.findByKeyValue(newKey?.keyValue))
            throw PixKeyAlreadyExistsException("pix key '${newKey?.keyValue}' already exists")

        //2 - fetch data at itau external service
        val response = itauClient.fetchAccountsByType(newKey?.userId!!, newKey.typeAccount!!.name)
        val account = response.body()?.toModel() ?: throw IllegalStateException("account not found")

        //3 - persist data
        val key = newKey.toModel(account)
        repository.save(key)
        return key
    }
}