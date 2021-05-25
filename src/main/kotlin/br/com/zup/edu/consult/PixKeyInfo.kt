package br.com.zup.edu.consult

import br.com.zup.edu.Key
import br.com.zup.edu.TypeAccount
import br.com.zup.edu.TypeKey
import br.com.zup.edu.register.AssociatedAccount
import java.time.LocalDateTime

data class PixKeyInfo(
    val pixId: String? = null,
    val userId: String? = null,
    val type: TypeKey,
    val key: String,
    val accountType: TypeAccount,
    val account: AssociatedAccount,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun of(key: Key): PixKeyInfo {
            return PixKeyInfo(
                pixId = key.id,
                userId = key.userId,
                type = key.typeKey,
                key = key.keyValue!!,
                accountType = key.typeAccount,
                account = key.account,
                createdAt = key.createdAt
            )
        }
    }
}
