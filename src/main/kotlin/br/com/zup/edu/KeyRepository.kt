package br.com.zup.edu

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface KeyRepository : JpaRepository<Key, String> {

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
     * @param key
     * @return a possible key
     */
    fun findByKeyValue(key: String): Optional<Key>

    /**
     * @param userId
     * @return a list of keys
     */
    fun findAllByUserId(userId: String): List<Key>

}