package de.visualdigits.kotlin.klanglicht.model.fixture

import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.junit.jupiter.api.Test
import java.io.File

internal class FixtureTest {

    @Test
    fun testReadModel() {
        File(ClassLoader.getSystemResource(".klanglicht/fixtures").toURI())
            .listFiles()
            ?.forEach { file ->
                println("Reading $file ...")
                val fixture = jacksonMapperBuilder().build().readValue(file, Fixture::class.java)
                println(fixture)
            }
    }
}
