package br.com.zup.edu.itau

import br.com.zup.edu.register.AssociatedAccount
import br.com.zup.edu.AllOpen
import br.com.zup.edu.TypeAccount
import com.fasterxml.jackson.annotation.JsonProperty

@AllOpen
data class ClientDetails (
    val tipo: TypeAccount,
    val instituicao: ClienteInstitution,
    val agencia: String,
    val numero: String,
    val titular: ClientHolder
)
{
    fun toModel(): AssociatedAccount {
        return AssociatedAccount(
            institution = this.instituicao.nome,
            holderName = this.titular.nome,
            holderCpf = this.titular.cpf,
            agency = this.agencia,
            accountNumber = this.numero,
            ispb = this.instituicao.ispb
        )
    }
}