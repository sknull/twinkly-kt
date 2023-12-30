package de.visualdigits.kotlin.klanglicht.model.lightmanager.json

import com.fasterxml.jackson.annotation.JsonProperty


class ActuatorProperties(
    var ntype: Int? = null,
    var index: Int? = null,
    var system: Int? = null,
    @JsonProperty("bemerkung") val comment: String? = null,
    var ip: String? = null, // since 10.6.4
    var mac: String? = null, // since 10.6.4
    var typ: String? = null, // since 10.6.4
    var paramon: String? = null,
    var paramoff: String? = null,
    var sequences: Int? = null,
    var btnnameon: String? = null,
    var btnnameoff: String? = null,
    var deviceid: String? = null,
    var marker: String? = null,
    var url: String? = null,
    var url2: String? = null,
    var sunset: Boolean? = null,
    var httptype: Int? = null,
    var httptype2: Int? = null,
    var ntypenew: Int? = null // since 10.7.2
)
