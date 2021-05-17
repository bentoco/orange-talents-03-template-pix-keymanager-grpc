package br.com.zup.edu

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "keys")
class KeyRegister(

    @field:NotBlank
    @field:Column(name = "userId")
    val userId: String,

    @field:NotBlank
    @field:Column(name = "typeKey")
    @field:Enumerated(EnumType.STRING)
    val typeKey: TypeKey,

    @field:Column(name = "keyValue")
    val keyValue: String,

    @field:NotBlank
    @field:Column(name = "typeAccount")
    @field:Enumerated(EnumType.STRING)
    val typeAccount: TypeAccount
) {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: String = UUID.randomUUID().toString()
}