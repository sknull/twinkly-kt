package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html


class LMMarker(
    var id: Int? = null,
    var name: String? = null,
    var colorOff: String? = null,
    var colorOn: String? = null,
    var state: Boolean? = null,

    /** Determines whether the button should stay split up (true) or should be consolidated into on toggle button (false).  */
    var separate: Boolean? = null,

    /** Determines to which actor id this marker belongs (if any).  */
    var actorId: String? = null,

    /** Determines the actor state to which this marker belongs (if any).  */
    var markerState: String? = null
)
