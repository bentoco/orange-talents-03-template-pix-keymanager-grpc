package br.com.zup.edu.consult

import br.com.zup.edu.ConsultKeyResponse
import br.com.zup.edu.TypeAccount
import br.com.zup.edu.TypeKey
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ConsultKeyResponseConverter {

    fun converter(keyInfo: PixKeyInfo): ConsultKeyResponse {
        return ConsultKeyResponse.newBuilder()
            .setUserId(keyInfo.userId?.toString() ?: "")
            .setPixId(keyInfo.pixId?.toString() ?: "")
            .setKey(
                ConsultKeyResponse.PixKey.newBuilder()
                    .setType(TypeKey.valueOf(keyInfo.type.name))
                    .setKey(keyInfo.key)
                    .setAccount(
                        ConsultKeyResponse.PixKey.AccountInfo.newBuilder()
                            .setType(TypeAccount.valueOf(keyInfo.accountType.name))
                            .setInstitution(keyInfo.account.institutionName)
                            .setOwnerName(keyInfo.account.ownerName)
                            .setOwnerCpf(keyInfo.account.ownerCpf)
                            .setBranch(keyInfo.account.branch)
                            .setAccountNumber(keyInfo.account.accountNumber)
                            .build()
                    )
                    .setCreatedAt(keyInfo.createdAt.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
                    .build()
            )
            .build()
    }
}

