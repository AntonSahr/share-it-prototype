package de.shareit.shareitcore.application.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriUtils
import java.math.BigDecimal

@Service
class GeocodingService {
    private val rest = RestTemplate()

    data class NominatimResult(
        val lat: String,
        val lon: String,
        val display_name: String
    )

    fun geocode(address: String): Pair<BigDecimal, BigDecimal>? {
        // Nominatim-Endpoint:
        val url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${UriUtils.encode(address, "UTF-8")}"
        val response = rest.getForObject(url, Array<NominatimResult>::class.java) ?: return null
        if (response.isEmpty()) return null

        val lat = response[0].lat.toBigDecimalOrNull()
        val lon = response[0].lon.toBigDecimalOrNull()
        return if (lat != null && lon != null) Pair(lat, lon) else null
    }
}
