package br.com.zup.edu.remove

import br.com.zup.edu.KeyRepository
import br.com.zup.edu.bcb.BcbClient
import br.com.zup.edu.bcb.DeletePixKeyRequest
import br.com.zup.edu.shared.ForbiddenException
import br.com.zup.edu.shared.NotFoundClientException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyRemoveService(
    @Inject private val repository: KeyRepository,
    @Inject private val bcbClient: BcbClient,
) {

    fun validKey(key: RegisterToRemove): Boolean {
        val possibleKey = repository.findByKeyValue(key.pixId)
        if (possibleKey.isEmpty)
            throw NotFoundClientException("key not found")

        if (possibleKey.get().userId != key.userId)
            throw  ForbiddenException("forbidden to perform operation")

        return true
    }

    fun removeKey(request: RegisterToRemove) {
        val removeKeyRequest = request.toBcbRequest()
        val response = bcbClient.removePixKeyBcb(removeKeyRequest.key, removeKeyRequest)
        when (response.status.code) {
            200 -> repository.deleteByKeyValue(request.pixId)
            403 -> throw ForbiddenException("forbidden to perform operation")
            404 -> throw NotFoundClientException("pix key not found")
            else -> throw Exception("unexpected error")
        }
    }

    private fun RegisterToRemove.toBcbRequest(): DeletePixKeyRequest {
        val account = repository.findByKeyValue(this.pixId)
        return DeletePixKeyRequest(
            participant = account.get().account.institutionIspb,
            key = this.pixId
        )
    }
}




