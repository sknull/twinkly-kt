package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html.deserializer

import com.fasterxml.jackson.databind.util.StdConverter
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html.LMParams
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LMParamsInitializer : StdConverter<LMParams, LMParams>() {

    override fun convert(params: LMParams): LMParams {
        params.dateTime = LocalDateTime.parse(
                (params.date + LocalDateTime.now().year).toString() + " " + params.time,
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            )
        return params
    }
}
