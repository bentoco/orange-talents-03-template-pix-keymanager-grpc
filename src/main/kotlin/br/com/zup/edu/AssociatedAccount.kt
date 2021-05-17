package br.com.zup.edu

data class AssociatedAccount(
    val institution: String,
    val holderName: String,
    val holderCpf: String,
    val agency: String,
    val accountNumber: String
)