package br.com.zup.edu

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Inject


@MicronautTest
internal class KeyRegisterRepositoryTest {

    @Inject
    lateinit var repository: KeyRegisterRepository

    private val CUSTOMER_ID = UUID.randomUUID().toString()
    private val CUSTOMER_KEY_VALUE = "foo@mail.com"


    @BeforeEach
    internal fun setup() {

    }

    @AfterEach
    internal fun teardown() {
        repository.deleteAll()
    }

    @Test
    internal fun `find key register by key value`() {
        val result = repository.existsByKeyValue(CUSTOMER_KEY_VALUE)
        assertTrue(result)
    }

    @Test
    internal fun `find key register by key value and type key`() {
        val result = repository.existsByUserIdAndTypeKeyEquals(CUSTOMER_ID, TypeKey.EMAIL)
        assertTrue(result)
    }
}