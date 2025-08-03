package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods
            
@OptIn(ExperimentalStdlibApi::class)
enum class Moods(
    val index: Int,
    val label: String,
    val effects: List<MoodsEffect>
) {

    Dance(0, "Dance", MoodDance.entries),
    Classic(1, "Classic", MoodClassic.entries),
    Ambient(2, "Ambient", MoodAmbient.entries),
    Fluo(3, "Fluo", MoodFluo.entries),
    Chillout(4, "Chillout", MoodChillout.entries),
    Pop(5, "Pop", MoodPop.entries);
    
    companion object {
        fun fromIndex(index: Int) = Moods.entries.find { e -> index == e.index }

        fun fromLabel(label: String) = Moods.entries.find { e -> label == e.label }
    }
}