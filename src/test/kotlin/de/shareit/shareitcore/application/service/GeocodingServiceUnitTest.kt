package de.shareit.shareitcore.application.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.util.stream.Stream


class GeocodingServiceUnitTest {

    private val webClientBuilder = WebClient.builder()
    private val geocodingService = NominatimGeocodingService(webClientBuilder)

    @ParameterizedTest
    @MethodSource("geocodingServiceUnitTestProvider")
    fun test_1(adress: String, lat: BigDecimal, lon: BigDecimal) {
        val (actLat, actLon) = geocodingService.geocodeAddress(adress)!!

        assertThat(actLat).isEqualTo(lat)
        assertThat(actLon).isEqualTo(lon)
    }

    companion object {

        @JvmStatic
        fun geocodingServiceUnitTestProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "Zeughaus 2 Unter den Linden Friedrichswerder Mitte Berlin 10117 Deutschland",
                    BigDecimal("52.5177477"),
                    BigDecimal("13.3970225")
                )
            )
        }
    }
}