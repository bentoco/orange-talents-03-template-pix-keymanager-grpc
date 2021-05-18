package br.com.zup.edu

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "keys")
class KeyRegister(

    @field:Column(name = "userId")
    val userId: String,

    @field:Column(name = "typeKey")
    @field:Enumerated(EnumType.STRING)
    val typeKey: TypeKey,

    @field:Column(name = "keyValue")
    val keyValue: String?,

    @field:Column(name = "typeAccount")
    @field:Enumerated(EnumType.STRING)
    val typeAccount: TypeAccount,

    val account: AssociatedAccount
) {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: String = UUID.randomUUID().toString()
}