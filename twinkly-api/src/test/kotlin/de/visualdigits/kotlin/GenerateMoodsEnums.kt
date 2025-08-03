package de.visualdigits.kotlin

import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import de.visualdigits.kotlin.twinkly.model.moods.Moods
import de.visualdigits.kotlin.twinkly.model.moods.MoodsEffects
import de.visualdigits.kotlin.twinkly.model.moods.MoodsEffectsNames
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled("only for code generation")
class GenerateMoodsEnums {

    @Test
    fun testGenerateEnums() {
        val mapper = jacksonMapperBuilder().build()

        val targetDirectory = File("./src/main/kotlin/de/visualdigits/kotlin/twinkly/model/device/xmusic/moods")

        val moods = mapper.readValue(File(ClassLoader.getSystemResource("moods/moods.json").toURI()), Moods::class.java)
        val moodsEffects = mapper.readValue(File(ClassLoader.getSystemResource("moods/moods-effects.json").toURI()), MoodsEffects::class.java)
        val moodsEffectsNames = mapper.readValue(File(ClassLoader.getSystemResource("moods/moods-effects-names.json").toURI()), MoodsEffectsNames::class.java)

        File(targetDirectory, "MoodsEffect.kt").writeText("""package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods

interface MoodsEffect {

    val index: Int

    val label: String

    val moodLabel: String
    
    fun moodIndex(): Int
}
""")

        File(targetDirectory, "Moods.kt").writeText("""package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
@OptIn(ExperimentalStdlibApi::class)
enum class Moods(
    val index: Int,
    val label: String,
    val icon: String,
    val color: String,
    val effects: Map<Int, MoodsEffect>
) {

    ${moods.sortedBy { mood -> mood.index }.joinToString(",\n    ") { mood ->
            val moodName = mood.name!!.replace(" ", "")
            "$moodName(${mood.index}, \"$moodName\", \"${mood.icon}\", \"${mood.color}\", Mood$moodName.entries.associate { e -> Pair(e.index, e) })" 
    }};
    
    companion object {
        fun fromIndex(index: Int) = Moods.entries.find { e -> index == e.index }

        fun fromLabel(label: String) = Moods.entries.find { e -> label == e.label }
    }

    fun effectFromIndex(effectIndex: Int) = effects[effectIndex]
}""")

        moods.forEach { mood ->
            val moodName = mood.name!!.replace(" ", "")
            val effectSet = moodsEffects.effectsMap[mood.uuid?.twod]!!
            val effectNames = effectSet.uniqueIds
                .mapIndexed { idx, uuid -> Pair(idx, moodsEffectsNames[uuid]!!) }
                .distinctBy { en -> en.second }
                .filter { e -> e.second.isNotBlank() }
            File(targetDirectory, "Mood$moodName.kt").writeText("""package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class Mood$moodName(
    override val index: Int,
    override val label: String,
    override val moodLabel: String
): MoodsEffect {

    Shuffle(-1, "Shuffle", "$moodName"),
    ${effectNames.joinToString(",\n    ") { en -> "${en.second.replace(" ", "")}(${en.first}, \"${en.second}\", \"$moodName\")" }};

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = Mood$moodName.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = Mood$moodName.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.$moodName.index
}""")
        }
    }
}
