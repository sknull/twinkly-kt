package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
enum class MoodClassic(
    override val index: Int,
    override val label: String
): MoodsEffect {

    Shuffle(-1, "Shuffle"),
    CandyLine(0, "Candy Line"),
    Swirl(1, "Swirl"),
    Weave(2, "Weave"),
    Fizz(3, "Fizz"),
    VuMeter(4, "Vu Meter"),
    Radiate(5, "Radiate"),
    DiamondTwist(6, "Diamond Twist"),
    PsycoSparkle(7, "Psyco Sparkle");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromIndex(index: Int) = MoodClassic.entries.find { e -> index == e.index }

        @OptIn(ExperimentalStdlibApi::class)
        fun fromLabel(label: String) = MoodClassic.entries.find { e -> label == e.label }
    }
    
    override fun moodIndex(): Int = Moods.Classic.index
}