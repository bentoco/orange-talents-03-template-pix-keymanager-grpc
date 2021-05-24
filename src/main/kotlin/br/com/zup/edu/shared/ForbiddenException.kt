package br.com.zup.edu.shared

import java.lang.RuntimeException

class ForbiddenException(message: String?) : RuntimeException(message) {
}