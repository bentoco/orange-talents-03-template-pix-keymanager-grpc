package br.com.zup.edu

import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class RegisterNewKey(

    //todo: create a validator
    @field:NotBlank
    val userId: String?,

    @field:NotNull
    val typeKey: TypeKey?,

    @field:Size(max = 77)
    val keyValue: String?,

    @field:NotNull
    val typeAccount: TypeAccount?,
) {
    fun toModel(account: AssociatedAccount): PixKey {
        return PixKey(
            userId = UUID.fromString(this.userId),
            typeKey = TypeKey.valueOf(this.typeKey!!.name),
            keyValue = if (this.typeKey == TypeKey.RANDOM_KEY) UUID.randomUUID().toString() else this.keyValue,
            typeAccount = TypeAccount.valueOf(this.typeAccount!!.name),
            account = account
        )
    }
}