package br.com.zup.edu.register

import br.com.zup.edu.Key
import br.com.zup.edu.TypeAccount
import br.com.zup.edu.TypeKey
import br.com.zup.edu.bcb.CreatePixKeyResponse
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
data class NewKey(
    @field:NotBlank val userId: String,
    @field:NotBlank val typeKey: TypeKey,
    @field:Size(max = 77) val keyValue: String?,
    @field:NotBlank val typeAccount: TypeAccount
) {
    fun toModel(bcbResponse: CreatePixKeyResponse, account: AssociatedAccount): Key {
        return Key(
            userId = this.userId,
            typeKey = bcbResponse.keyType,
            keyValue = bcbResponse.key,
            typeAccount = TypeAccount.valueOf(this.typeAccount.name),
            account = account
        )
    }
}
