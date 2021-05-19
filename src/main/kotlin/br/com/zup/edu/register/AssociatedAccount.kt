package br.com.zup.edu.register

import javax.persistence.Embeddable

@Embeddable
class AssociatedAccount(
    val institution: String,
    val holderName: String,
    val holderCpf: String,
    val agency: String,
    val accountNumber: String
)

