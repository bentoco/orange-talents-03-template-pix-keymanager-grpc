package br.com.zup.edu

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface KeyRegisterRepository: JpaRepository<KeyRegister, String>{

    /**
     * @param value
     * @return check if already exists genereted key
     */
    fun existsByKeyValue(value: String): Boolean

    /**
     * @param userId
     * @param typeKey
     * @return check if already exists genereted keyVale to the type
     */
    fun existsByUserIdAndTypeKeyEquals(userId: String, typeKey: TypeKey): Boolean
}