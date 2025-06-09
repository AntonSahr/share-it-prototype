package de.shareit.shareitcore.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Service interface for geocoding addresses.
 */
interface GeocodingService {
    /**
     * Converts an address into geographic coordinates (latitude, longitude).
     * @param address the address to look up
     * @return Pair of latitude and longitude as BigDecimal, or null if not found
     */
    fun geocodeAddress(address: String): Pair<BigDecimal, BigDecimal>?
}

/**
 * Nominatim-based implementation of GeocodingService using OpenStreetMap.
 * Please adhere to the Nominatim Usage Policy (rate limits, user agent).
 */
@Service
class NominatimGeocodingService(
    webClientBuilder: WebClient.Builder
) : GeocodingService {

    private val log = LoggerFactory.getLogger(NominatimGeocodingService::class.java)
    private val client: WebClient = webClientBuilder
        .baseUrl("https://nominatim.openstreetmap.org")
        .defaultHeader("User-Agent", "shareit-backend")
        .build()

    override fun geocodeAddress(address: String): Pair<BigDecimal, BigDecimal>? {
        log.debug("geocodeAddress called with address='{}'", address)

        val uriSpec = client.get()
        val uri = uriSpec
            .uri { builder ->
                builder.path("/search")
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", "1")
                    .build()
            }
            .retrieve()

        log.debug("Executing request to Nominatim URI")
        val results: Array<NominatimResult>? = try {
            uri.bodyToMono(Array<NominatimResult>::class.java)
                .block()
        } catch (ex: Exception) {
            log.error("Error while calling Nominatim API", ex)
            null
        }

        return results
            ?.firstOrNull()
            ?.also { log.debug("Received geocoding result: lat='{}', lon='{}'", it.lat, it.lon) }
            ?.let {
                Pair(BigDecimal(it.lat), BigDecimal(it.lon))
            }
            ?: run {
                log.warn("No geocoding result for address='{}'", address)
                null
            }
    }
}

/**
 * Data class for deserializing Nominatim JSON response.
 */
private data class NominatimResult(
    val lat: String,
    val lon: String
)
