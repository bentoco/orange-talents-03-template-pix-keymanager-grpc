package br.com.zup.edu.register

import javax.persistence.Embeddable

@Embeddable
class AssociatedAccount(
    val institutionName: String,
    val institutionIspb: String? = null,
    val branch: String,
    val accountNumber: String,
    val ownerName: String,
    val ownerId: String? = null,
    val ownerCpf: String
)

