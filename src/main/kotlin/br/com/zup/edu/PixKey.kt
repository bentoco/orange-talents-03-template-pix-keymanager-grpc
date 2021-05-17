package br.com.zup.edu

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "keys")
class PixKey(

    //todo: create a validator
    @field:NotBlank
    @field:Column(name = "userId")
    val userId: UUID,

    @field:NotNull
    @field:Column(name = "typeKey")
    @field:Enumerated(EnumType.STRING)
    val typeKey: TypeKey,

    @field:Size(max = 77)
    @field:Column(name = "keyValue")
    val keyValue: String?,

    @field:NotNull
    @field:Column(name = "typeAccount")
    @field:Enumerated(EnumType.STRING)
    val typeAccount: TypeAccount,

    @field:NotNull
    @field:Column(name = "associatedAccount")
    val account: AssociatedAccount
) {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: String = UUID.randomUUID().toString()
}
