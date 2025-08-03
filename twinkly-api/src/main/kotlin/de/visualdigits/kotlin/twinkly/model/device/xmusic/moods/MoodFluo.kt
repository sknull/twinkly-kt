package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodFluo(
    override val index: Int,
    override val label: String,
    override val moodLabel: String
): MoodsEffect {

    Shuffle(-1, "Shuffle", "Fluo"),
    Psychedelica(0, "Psychedelica", "Fluo"),
    VuFluo(1, "Vu Fluo", "Fluo"),
    Crossover(2, "Crossover", "Fluo"),
    Amplify(3, "Amplify", "Fluo"),
    Zip(5, "Zip", "Fluo"),
    Sunray(7, "Sunray", "Fluo");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodFluo.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodFluo.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Fluo.index
}