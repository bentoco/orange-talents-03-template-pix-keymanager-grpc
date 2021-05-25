package br.com.zup.edu.consult

import br.com.zup.edu.ConsultKeyRequest
import br.com.zup.edu.ConsultKeyResponse
import br.com.zup.edu.KeyConsultServiceGrpc
import br.com.zup.edu.KeyRepository
import br.com.zup.edu.bcb.BcbClient
import br.com.zup.edu.shared.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@ErrorHandler
class KeyConsultEnpoint(
    @Inject private val validator: Validator,
    @Inject private val repository: KeyRepository,
    @Inject private val bcbClient: BcbClient
) : KeyConsultServiceGrpc.KeyConsultServiceImplBase() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun consultKey(request: ConsultKeyRequest, responseObserver: StreamObserver<ConsultKeyResponse>) {
        LOGGER.info("New request: $request")

        val filter = request.toModel(validator)
        val infoKey = filter.doFilter(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(ConsultKeyResponseConverter().converter(infoKey))
        responseObserver.onCompleted()
    }
}

