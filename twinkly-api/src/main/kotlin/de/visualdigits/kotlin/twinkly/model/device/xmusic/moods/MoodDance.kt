package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodDance(
    override val index: Int,
    override val label: String
): MoodsEffect {

    Shuffle(-1, "Shuffle"),
    Nova(0, "Nova"),
    Omega(1, "Omega"),
    DiamondWeave(2, "Diamond Weave"),
    Oscillator(3, "Oscillator"),
    March(5, "March"),
    PsycoMarch(6, "Psyco March"),
    Chevron(7, "Chevron");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodDance.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodDance.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Dance.index
}