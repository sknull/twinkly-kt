package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html

import com.fasterxml.jackson.annotation.JsonIgnore
import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.feign.LightmanagerClient
import org.apache.commons.lang3.StringUtils

class LMActor(
    var id: Int? = null,
    var name: String? = null,
    var markers: MutableMap<String, LMMarker?> = mutableMapOf(),
    var actorOff: String? = null,
    var actorOn: String? = null,
    var colorOff: String? = null,
    var colorOn: String? = null,
    var isDimmer: Boolean? = null,
    var requests: MutableMap<String, LMRequest> = mutableMapOf(),
    var requestsBySmkState: MutableMap<Int, LMDefaultRequest> = mutableMapOf()
) : HtmlRenderable {

    fun addRequest(key: String, request: LMRequest) {
        requests[key] = request
        if (request is LMDefaultRequest) {
            val drq = request
            if (drq.hasSmk()) {
                drq.smk?.get(1)?.let { requestsBySmkState[it] = drq }
            }
        }
    }

    override fun toHtml(configHolder: ConfigHolder): String {
        val sb = StringBuilder()
        sb.append("      <div class=\"panel\">\n")
        renderLabel(sb, name)
        renderRequests(sb)
        // fixme - yet not working reliable enough
//        renderSlider(sb, configHolder);
        sb.append("      </div><!-- actor -->\n")
        return sb.toString()
    }

    private fun renderSlider(sb: StringBuilder, configHolder: ConfigHolder) {
        val lightmanagerUrl = configHolder.preferences?.serviceMap?.get("lmair")?.url
        if (isDimmer == true) {
            val actorId = id
            val drq = getRequestBySmkState(1)
            var requestTemplate: String? = ""
            if (drq != null) {
                requestTemplate = drq.requestTemplate()
            }
            sb.append("<div class=\"slidercontainer\">\n")
                .append("  <input type=\"range\" min=\"1\" max=\"16\" value=\"8\" class=\"slider\" id=\"slider-")
                .append(actorId)
                .append("\"")
                .append("/>\n")
                .append("  <script>document.getElementById(\"slider-")
                .append(actorId)
                .append("\").oninput = function() { request('")
                .append(lightmanagerUrl)
                .append("/control', 'POST', '")
                .append(requestTemplate)
                .append("'.replace('\${level}', this.value)); }</script>\n")
                .append("</div>\n")
        }
    }

    private fun renderRequests(sb: StringBuilder) {
        var markerIsOn: Boolean? = false
        var hasSeparateMarkers: Boolean? = false
        var isSeparate: Boolean? = false
        var colorOff: String? = ""
        var colorOn: String? = ""
        if (markers.containsKey("unified")) {
            val marker = markers["unified"]
            markerIsOn = marker?.state
            colorOff = marker?.colorOff
            colorOn = marker?.colorOn
            isSeparate = marker?.separate
        }
        else if (!markers.isEmpty()) {
            hasSeparateMarkers = true
        }
        if (StringUtils.isEmpty(colorOff)) {
            colorOff =
                if (StringUtils.isNotEmpty(colorOff)) colorOff else LightmanagerClient.COLOR_OFF
        }
        if (StringUtils.isEmpty(colorOn)) {
            colorOn = if (StringUtils.isNotEmpty(colorOn)) colorOn else LightmanagerClient.COLOR_ON
        }
        val lmRequests = requests.values.toList()
        if (!lmRequests.isEmpty() && lmRequests.first() is LMDefaultRequest) {
            if (hasSeparateMarkers == true || isSeparate == true) {
                sb.append("        <div class=\"double-button\">\n")
                //                Collections.reverse(lmRequests);
                for (request in lmRequests) {
                    val marker = markers[(request as LMDefaultRequest).name]
                    if (marker != null) {
                        colorOff = marker.colorOff
                        colorOn = marker.colorOn
                        markerIsOn = marker.state
                    }
                    renderRequest(
                        sb,
                        "half-button",
                        markerIsOn,
                        colorOff,
                        colorOn,
                        request,
                        false,
                        isSeparate,
                        hasSeparateMarkers
                    )
                }
                sb.append("        </div><!-- double-button -->\n")
            }
            else {
                val rq0 = lmRequests.first() as LMDefaultRequest
                val smkState0 = determineSmkState(rq0)
                var rq: LMRequest? = null
                rq = if (markerIsOn == true && !smkState0 || markerIsOn != true && smkState0) {
                    rq0
                }
                else if (lmRequests.size > 1) {
                    lmRequests.get(1)
                }
                else {
                    rq0
                }
                renderRequest(sb, "button", markerIsOn, colorOff, colorOn, rq, true, false, false)
            }
        }
    }

    private fun renderRequest(
        sb: StringBuilder,
        styleClass: String,
        markerIsOn: Boolean?,
        colorOff: String?,
        colorOn: String?,
        rq: LMRequest?,
        unified: Boolean?,
        isSeparate: Boolean?,
        hasSeparateMarkers: Boolean?
    ) {
        if (rq is LMDefaultRequest) {
            val drq = rq
            val isOn = determineSmkState(drq)
            sb.append("        <div class=\"")
                .append(styleClass)
                .append("\"")
            if (isSeparate == true) {
                if (isOn) {
                    sb.append(" style=\"background-color: ").append(colorOn).append(";\"")
                }
                else {
                    sb.append(" style=\"background-color: ").append(colorOff).append(";\"")
                }
            }
            else if (hasSeparateMarkers == true) {
                sb.append(" style=\"background-color: ").append(colorOn).append(";\"")
            }
            else if (markerIsOn == true && (isOn || unified == true)) {
                sb.append(" style=\"background-color: ").append(colorOn).append(";\"")
            }
            else {
                if (colorOff?.contains(",") == true) {
                    sb.append(" style=\"background: -moz-linear-gradient(left, ")
                        .append(colorOff)
                        .append("); background: -webkit-linear-gradient(left, ")
                        .append(colorOff)
                        .append("); background: linear-gradient(to right, ")
                        .append(colorOff)
                        .append(");\"")
                }
                else {
                    sb.append(" style=\"background-color: ")
                        .append(colorOff)
                        .append(";\"")
                }
            }
            sb.append("><input")
            if (hasSeparateMarkers == true) {
                if (markerIsOn == true) {
                    sb.append(" class='on'")
                }
                else {
                    sb.append(" class='off'")
                }
            }
            else if (isSeparate == true) {
                if (isOn) {
                    sb.append(" class='on'")
                }
                else {
                    sb.append(" class='off'")
                }
            }
            sb.append(" type=\"submit\" value=\"")
                .append(drq.name)
                .append("\" onclick=\"request('")
                .append(drq.uri)
                .append("'")
            val type = drq.type
            if (type == RequestType.GET || type == RequestType.PUT || type == RequestType.POST) {
                sb.append(",'")
                    .append(type.name)
                    .append("'")
            }
            sb.append(");\"/></div>\n")
        }
        //                        else if (rq instanceof LMCamRequest) {
//                            LMCamRequest crq = (LMCamRequest) rq;
//                        }
    }

    fun addMarker(marker: LMMarker?) {
        var markerState = marker?.markerState!!
        if (StringUtils.isEmpty(markerState)) {
            markerState = "unified"
        }
        markers[markerState] = marker
    }

    private fun determineSmkState(drq: LMDefaultRequest): Boolean {
        return drq.hasSmk() && drq.smk?.get(1) == 1
    }

    @JsonIgnore
    fun getRequestByName(key: String): LMRequest? {
        return requests[key]
    }

    @JsonIgnore
    fun getRequestBySmkState(state: Int): LMDefaultRequest? {
        return requestsBySmkState[state]
    }
}
