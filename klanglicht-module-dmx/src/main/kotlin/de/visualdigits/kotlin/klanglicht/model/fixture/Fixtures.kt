package de.visualdigits.kotlin.klanglicht.model.fixture

import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import java.io.File
import java.nio.file.Paths

class Fixtures(
    val fixtures: List<Fixture>
) {
    companion object {
        private val mapper = jacksonMapperBuilder().build()

        private var fixtures: Fixtures? = null

        fun load(klanglichtDir: File): Fixtures {
            if (fixtures == null) {
                fixtures = Fixtures(Paths.get(klanglichtDir.canonicalPath, "fixtures").toFile()
                        .listFiles()
                        ?.map { file -> Fixture.load(klanglichtDir, file.name) }
                        ?:listOf()
                )
            }
            return fixtures!!
        }
    }

    fun getFixture(manufacturer: String, model: String): Fixture? {
        return fixtures.find { fixture -> fixture.manufacturer == manufacturer && fixture.model == model }
    }
}
