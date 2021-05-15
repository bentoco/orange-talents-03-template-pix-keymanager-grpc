package br.com.zup.edu

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface KeyRegisterRepository: JpaRepository<KeyRegister, String>{

    /**
     * @param userId
     * @param typeKey
     * @return check if already exists genereted keyVale to the type
     */
    fun existsByUserIdAndTypeKeyEquals(userId: String, typeKey: RegisterKeyRequest.TypeKey): Boolean
}