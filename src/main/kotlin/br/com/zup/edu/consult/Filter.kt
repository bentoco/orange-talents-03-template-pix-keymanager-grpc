package br.com.zup.edu.consult

import br.com.zup.edu.KeyRepository
import br.com.zup.edu.bcb.BcbClient
import br.com.zup.edu.shared.NotFoundClientException
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filter {

    @Introspected
    data class ByPixId(
        @field:NotBlank val userId: String,
        @field:NotBlank val pixId: String
    ) : Filter() {
        override fun doFilter(repository: KeyRepository, bcbClient: BcbClient): PixKeyInfo {
            return repository.findById(pixId)
                .filter { it.belongTo(userId) }
                .map(PixKeyInfo::of)
                .orElseThrow {
                    NotFoundClientException("pix key not found")
                }
        }
    }

    @Introspected
    data class ByKey(
        @field:NotBlank @field:Size(max = 77) val key: String
    ) : Filter() {

        private val LOGGER = LoggerFactory.getLogger(this.javaClass)

        override fun doFilter(repository: KeyRepository, bcbClient: BcbClient): PixKeyInfo {
            return repository.findByKeyValue(key)
                .map(PixKeyInfo::of)
                .orElseGet {
                    LOGGER.info("pix key $key not found at BCB Service")

                    val response = bcbClient.findByKey(key)
                    when (response.status) {
                        HttpStatus.OK -> response.body()!!.toModel()
                        else -> throw NotFoundClientException("pix key not found")
                    }
                }
        }
    }

    @Introspected
    class Invalid() : Filter() {
        override fun doFilter(repository: KeyRepository, bcbClient: BcbClient): PixKeyInfo {
            throw IllegalArgumentException("invalid pix key or not informed")
        }
    }

    /*
     Must return key found or throw exception error when key not found
     */
    abstract fun doFilter(repository: KeyRepository, bcbClient: BcbClient): PixKeyInfo
}
