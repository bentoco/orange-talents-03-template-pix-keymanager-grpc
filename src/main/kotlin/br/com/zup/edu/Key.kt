package br.com.zup.edu

import br.com.zup.edu.register.AssociatedAccount
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "keys")
class Key(

    @field:Column(name = "userId", nullable = false)
    val userId: String,

    @field:Column(name = "typeKey", nullable = false)
    @field:Enumerated(EnumType.STRING)
    val typeKey: TypeKey,

    @field:Column(name = "keyValue", unique = true)
    val keyValue: String?,

    @field:Column(name = "typeAccount", nullable = false)
    @field:Enumerated(EnumType.STRING)
    val typeAccount: TypeAccount,

    @field:Column(name = "typeAccount", nullable = false)
    val account: AssociatedAccount
) {
    fun belongTo(userId: String): Boolean {
        if (this.userId == userId) {
            return true
        }
        return false
    }

    @Id
    val id: String = UUID.randomUUID().toString()

    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now()
}