package br.com.zup.edu

import javax.inject.Singleton

@Singleton
class KeyValueValidator {
    fun validator(value: String?): Boolean {
        val regexCPF         = value!!.matches("^[0-9]{11}\$".toRegex())
        val regexPhoneNumber = value.matches("^\\+[1-9][0-9]\\d{1,14}$".toRegex())
        val regexEmail       = value.matches("^[A-Za-z0-9+_.-]+@(.+)$".toRegex())

        when(true){
            regexCPF -> return true
            regexPhoneNumber -> return true
            regexEmail -> return true
        }
        return false
    }
}
