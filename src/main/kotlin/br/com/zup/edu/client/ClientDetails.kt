package br.com.zup.edu.client

import br.com.zup.edu.AssociatedAccount
import br.com.zup.edu.AllOpen
import br.com.zup.edu.TypeAccount
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

@AllOpen
data class ClientDetails (
    @param:JsonProperty("tipo") val tipo: TypeAccount,
    @param:JsonProperty("instituicao") val instituicao: ClienteInstitution,
    @param:JsonProperty("agencia") val agencia: String,
    @param:JsonProperty("numero") val numero: String,
    @param:JsonProperty("titular") val titular: ClientHolder
)
{
    fun toModel(): AssociatedAccount {
        return AssociatedAccount(
            institution = this.instituicao.nome,
            holderName = this.titular.nome,
            holderCpf = this.titular.cpf,
            agency = this.agencia,
            accountNumber = this.numero
        )
    }
}