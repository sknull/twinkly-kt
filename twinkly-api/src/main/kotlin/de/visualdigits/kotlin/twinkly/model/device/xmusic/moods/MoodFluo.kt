package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodFluo(
    override val index: Int,
    val label: String
): MoodsEffect {

    Shuffle(-1, "Shuffle"),
    Psychedelica(0, "Psychedelica"),
    VuFluo(1, "Vu Fluo"),
    Crossover(2, "Crossover"),
    Amplify(3, "Amplify"),
    Zip(5, "Zip"),
    Sunray(7, "Sunray");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodFluo.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodFluo.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Fluo.index
}