package br.com.zup.edu.consult

import br.com.zup.edu.ConsultKeyRequest
import br.com.zup.edu.ConsultKeyRequest.FilterCase.*
import java.lang.IllegalArgumentException
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ConsultKeyRequest.toModel(validator: Validator): Filter {
    val filter = when (filterCase) {
        PIX_ID -> pixId.let {
            Filter.ByPixId(userId = it.userId, pixId = it.pixId)
        }
        KEY -> Filter.ByKey(key)
        FILTER_NOT_SET -> Filter.Invalid()
    }

    val violations = validator.validate(filter)
    if (violations.isNotEmpty()) {
        throw IllegalArgumentException("invalid pix key or not informed")
    }
    return filter
}