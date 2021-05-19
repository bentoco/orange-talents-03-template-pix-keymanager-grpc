package br.com.zup.edu.register

import br.com.zup.edu.Key
import br.com.zup.edu.TypeAccount
import br.com.zup.edu.TypeKey
import io.micronaut.core.annotation.Introspected

@Introspected
data class KeyRegisterRequest(
    val userId: String?,
    val typeKey: TypeKey?,
    val keyValue: String?,
    val typeAccount: TypeAccount?
) {

    fun toModel(account: AssociatedAccount): Key {
        return Key(
            userId = this.userId!!,
            typeKey = TypeKey.valueOf(this.typeKey!!.name),
            keyValue = this.keyValue,
            typeAccount = TypeAccount.valueOf(this.typeAccount!!.name),
            account = account
        )
    }
}
