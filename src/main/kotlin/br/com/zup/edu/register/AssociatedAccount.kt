package br.com.zup.edu.register

import javax.persistence.Embeddable

@Embeddable
class AssociatedAccount(
    val institutionName: String,
    val institutionIspb: String,
    val branch: String,
    val accountNumber: String,
    val ownerId: String,
    val ownerName: String,
    val ownerCpf: String
)

