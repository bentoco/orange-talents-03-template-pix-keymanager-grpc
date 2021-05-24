package br.com.zup.edu.shared

import io.grpc.BindableService
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
@InterceptorBean(ErrorHandler::class)
internal class ExceptionHandlerInterceptor : MethodInterceptor<BindableService, Any?> {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {
        try {
            context.proceed()
        } catch (e: Exception) {
            LOGGER.error(e.message)
            e.printStackTrace()

            val statusError = when (e) {
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException()
                is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message).asRuntimeException()
                is ForbiddenException -> Status.PERMISSION_DENIED.withDescription(e.message).asRuntimeException()
                is NotFoundClientException -> Status.NOT_FOUND.withDescription(e.message).asRuntimeException()
                is RegisterAlreadyExistsException -> Status.ALREADY_EXISTS.withDescription(e.message)
                    .asRuntimeException()
                else -> Status.UNKNOWN.withDescription("unexpected error happened").asRuntimeException()
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusError)
        }
        return null
    }
}