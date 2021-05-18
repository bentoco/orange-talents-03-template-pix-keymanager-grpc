package br.com.zup.edu

import io.micronaut.core.annotation.Introspected

@Introspected
data class KeyRegisterRequest(
    val userId: String?,
    val typeKey: TypeKey?,
    val keyValue: String?,
    val typeAccount: TypeAccount?
) {

    fun toModel(account: AssociatedAccount): KeyRegister {
        return KeyRegister(
            userId = this.userId!!,
            typeKey = TypeKey.valueOf(this.typeKey!!.name),
            keyValue = this.keyValue,
            typeAccount = TypeAccount.valueOf(this.typeAccount!!.name),
            account = account
        )
    }
}
