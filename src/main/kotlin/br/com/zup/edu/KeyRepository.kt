package br.com.zup.edu

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface KeyRepository: JpaRepository<Key, String>{

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

    /**
     * @param userId
     * @param keyValue
     * @return key register
     */
    fun findByUserIdAndKeyValue(userId: String, keyValue: String): Optional<Key>

    fun findByKeyValue(pixId: String): Optional<Key>

    fun deleteByKeyValue(pixId: String)
}