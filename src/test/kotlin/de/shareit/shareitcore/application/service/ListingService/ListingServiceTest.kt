package de.shareit.shareitcore.application.service

import de.shareit.shareitcore.application.GeocodingService
import de.shareit.shareitcore.application.ListingService
import de.shareit.shareitcore.domain.model.AppUser
import de.shareit.shareitcore.domain.model.Item
import de.shareit.shareitcore.domain.model.PriceUnit
import de.shareit.shareitcore.domain.service.ItemRepository
import de.shareit.shareitcore.domain.service.UserRepository
import de.shareit.shareitcore.ui.dto.ItemResponseDto
import de.shareit.shareitcore.web.dto.ItemDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class ListingServiceTest {

    @Mock
    private lateinit var itemRepository: ItemRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var geocodingService: GeocodingService

    @InjectMocks
    private lateinit var listingService: ListingService

    @Captor
    private lateinit var itemCaptor: ArgumentCaptor<Item>

    /**
     * TestcreateItem: Wenn der Owner existiert und Geocoding Koordinaten liefert, soll ein Item
     * korrekt angelegt und das DTO zurückgegeben werden.
     */
    @Test
    fun `createItem legt neues Item an mit Koordinaten`() {
        val ownerId = 42L
        val owner = AppUser(
            id = ownerId,
            displayName = "Besitzer",
            email = "owner@example.com",
            oauthProvider = "local",
            providerId = "owner42"
        )
        `when`(userRepository.findById(ownerId)).thenReturn(Optional.of(owner))

        val address = "Musterstraße 1, 12345 Stadt"
        val lat = BigDecimal("52.520008")
        val lon = BigDecimal("13.404954")
        `when`(geocodingService.geocode(address)).thenReturn(Pair(lat, lon))

        val dto = ItemDto(
            title = "Bohrmaschine",
            description = "Leistungsstark und neu",
            priceAmount = BigDecimal("15.00"),
            priceUnit = PriceUnit.HOURLY,
            address = address
        )

        // Simuliere, dass itemRepository.save(...) das Item mit einer ID versieht
        doAnswer { invocation ->
            val toSave = invocation.arguments[0] as Item
            toSave.id = 7L
            toSave.createdAt = Instant.now()
            toSave.updatedAt = Instant.now()
            toSave
        }.`when`(itemRepository).save(any(Item::class.java))

        val response: ItemResponseDto = listingService.createItem(ownerId, dto)

        // Verifiziere, dass itemRepository.save aufgerufen wurde und Felder korrekt gesetzt wurden
        verify(itemRepository).save(itemCaptor.capture())
        val savedItem = itemCaptor.value

        assertEquals("Bohrmaschine", savedItem.title)
        assertEquals("Leistungsstark und neu", savedItem.description)
        assertEquals(BigDecimal("15.00"), savedItem.priceAmount)
        assertEquals(PriceUnit.HOURLY, savedItem.priceUnit)
        assertEquals(lat, savedItem.latitude)
        assertEquals(lon, savedItem.longitude)
        assertEquals(address, savedItem.address)
        assertEquals(owner, savedItem.owner)

        // Assertions für das zurückgegebene DTO
        assertEquals(7L, response.id)
        assertEquals("Bohrmaschine", response.title)
        assertEquals("Leistungsstark und neu", response.description)
        assertEquals(BigDecimal("15.00"), response.priceAmount)
        assertEquals(PriceUnit.HOURLY, response.priceUnit)
        assertEquals(ownerId, response.ownerId)
        assertEquals("Besitzer", response.ownerDisplayName)
        assertEquals(lat, response.latitude)
        assertEquals(lon, response.longitude)
        assertEquals(address, response.address)
        assertNotNull(response.createdAt)
        assertNotNull(response.updatedAt)
    }

    /**
     * TestcreateItem: Wenn der Owner nicht existiert, soll eine RuntimeException geworfen werden.
     */
    @Test
    fun `createItem wirft bei nicht vorhandenem Owner`() {
        val ownerId = 99L
        `when`(userRepository.findById(ownerId)).thenReturn(Optional.empty())

        val dto = ItemDto(
            title = "Bohrmaschine",
            description = "Leistungsstark",
            priceAmount = BigDecimal("10.00"),
            priceUnit = PriceUnit.DAILY,
            address = "Irgendwo 5, 54321 Stadt"
        )

        assertThrows(RuntimeException::class.java) {
            listingService.createItem(ownerId, dto)
        }
        verify(itemRepository, never()).save(any(Item::class.java))
    }

    /**
     * TestupdateItem: Wenn Item existiert und Owner übereinstimmt,
     * werden Felder aus dto übernommen und Koordinaten aktualisiert.
     * Die aktualisierte updatedAt‐Zeit darf einfach nur ungleich der alten sein.
     */
    @Test
    fun `updateItem aktualisiert bestehendes Item korrekt`() {
        val ownerId = 5L
        val itemId = 11L

        // 1) Fester „alter” Timestamp, damit es nie zeitgleich mit Instant.now() wird:
        val baseTimestamp = Instant.parse("2025-01-01T00:00:00Z")

        // 2) Vorhandenes Item mocken:
        val existingOwner = AppUser(
            id = ownerId,
            displayName = "UserX",
            email = "ux@example.com",
            oauthProvider = "local",
            providerId = "ux5"
        )
        val existingItem = Item(
            id = itemId,
            title = "Altgerät",
            description = "Alt",
            priceAmount = BigDecimal("8.00"),
            priceUnit = PriceUnit.DAILY,
            owner = existingOwner,
            createdAt = baseTimestamp,
            updatedAt = baseTimestamp,
            latitude = BigDecimal("50.110924"),
            longitude = BigDecimal("8.682127"),
            address = "Altstraße 2, 11111 Stadt"
        )
        `when`(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem))

        // 3) Neues DTO mit leerem Address-Feld: => automatische Geocoding
        val dto = ItemDto(
            title = "Neu Gerät",
            description = "Neu",
            priceAmount = BigDecimal("12.00"),
            priceUnit = PriceUnit.HOURLY,
            address = ""
        )
        // Stub: geocodingService.geocode("") liefert neue Koordinaten
        val newLat = BigDecimal("48.135125")
        val newLon = BigDecimal("11.581981")
        `when`(geocodingService.geocode("")).thenReturn(Pair(newLat, newLon))

        // 4) itemRepository.save(...) soll das übergebene Item einfach zurückliefern
        doAnswer { invocation ->
            invocation.arguments[0] as Item
        }.`when`(itemRepository).save(any(Item::class.java))

        val response: ItemResponseDto = listingService.updateItem(ownerId, itemId, dto)

        // Vor und nach Aufruf: findById + save
        verify(itemRepository).findById(itemId)
        verify(itemRepository).save(itemCaptor.capture())

        val savedItem = itemCaptor.value

        // 5) Feld-Übernahmen prüfen:
        assertEquals("Neu Gerät", savedItem.title)
        assertEquals("Neu", savedItem.description)
        assertEquals(BigDecimal("12.00"), savedItem.priceAmount)
        assertEquals(PriceUnit.HOURLY, savedItem.priceUnit)

        // 6) Geocoding-Koordinaten gesetzt:
        assertEquals(newLat, savedItem.latitude)
        assertEquals(newLon, savedItem.longitude)
        assertEquals("", savedItem.address)

        // 7) Owner bleibt gleich:
        assertEquals(existingOwner, savedItem.owner)

        // 8) **Wichtig:** updatedAt wurde geändert – jetzt vergleichen wir nur auf „nicht gleich”:
        assertNotEquals(baseTimestamp, savedItem.updatedAt,
            "updatedAt sollte nach Ausführung der Methode nicht mehr dem alten Timestamp entsprechen."
        )
        assertNotNull(savedItem.updatedAt)

        // 9) Prüfe das zurückgegebene ResponseDto
        assertEquals(itemId, response.id)
        assertEquals("Neu Gerät", response.title)
        assertEquals("Neu", response.description)
        assertEquals(BigDecimal("12.00"), response.priceAmount)
        assertEquals(PriceUnit.HOURLY, response.priceUnit)
        assertEquals(newLat, response.latitude)
        assertEquals(newLon, response.longitude)
        assertEquals("", response.address)
    }

    /**
     * TestupdateItem: Wenn Item nicht existiert, soll IllegalArgumentException geworfen werden.
     */
    @Test
    fun `updateItem wirft bei nicht vorhandenem Item`() {
        val ownerId = 2L
        val itemId = 99L
        `when`(itemRepository.findById(itemId)).thenReturn(Optional.empty())

        val dto = ItemDto(
            title = "Irrelevant",
            description = "",
            priceAmount = BigDecimal("5.00"),
            priceUnit = PriceUnit.HOURLY,
            address = "Testweg 3"
        )

        assertThrows(IllegalArgumentException::class.java) {
            listingService.updateItem(ownerId, itemId, dto)
        }
        verify(itemRepository, never()).save(any(Item::class.java))
    }

    /**
     * TestupdateItem: Wenn Owner nicht übereinstimmt, soll IllegalArgumentException geworfen werden.
     */
    @Test
    fun `updateItem wirft bei falschem Owner`() {
        val ownerId = 7L
        val wrongOwnerId = 8L
        val itemId = 15L

        val realOwner = AppUser(
            id = ownerId,
            displayName = "Real",
            email = "real@example.com",
            oauthProvider = "local",
            providerId = "real7"
        )
        val otherOwner = AppUser(
            id = wrongOwnerId,
            displayName = "Other",
            email = "other@example.com",
            oauthProvider = "local",
            providerId = "other8"
        )
        val item = Item(
            id = itemId,
            title = "Test",
            description = "Desc",
            priceAmount = BigDecimal("3.00"),
            priceUnit = PriceUnit.DAILY,
            owner = realOwner,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            latitude = null,
            longitude = null,
            address = ""
        )
        `when`(itemRepository.findById(itemId)).thenReturn(Optional.of(item))

        val dto = ItemDto(
            title = "UpdateVersuch",
            description = "Fail",
            priceAmount = BigDecimal("10.00"),
            priceUnit = PriceUnit.HOURLY,
            address = ""
        )

        assertThrows(IllegalArgumentException::class.java) {
            listingService.updateItem(wrongOwnerId, itemId, dto)
        }
        verify(itemRepository, never()).save(any(Item::class.java))
    }

    /**
     * TestfindAll: Wenn Items vorhanden sind, sollen alle als DTO-Liste zurückgegeben werden.
     */
    @Test
    fun `findAll gibt alle Items zurück`() {
        val owner = AppUser(
            id = 1L,
            displayName = "A",
            email = "a@x.com",
            oauthProvider = "local",
            providerId = "a1"
        )
        val item1 = Item(
            id = 101L,
            title = "Item1",
            description = "D1",
            priceAmount = BigDecimal("1.00"),
            priceUnit = PriceUnit.HOURLY,
            owner = owner,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            latitude = null,
            longitude = null,
            address = ""
        )
        val item2 = Item(
            id = 102L,
            title = "Item2",
            description = "D2",
            priceAmount = BigDecimal("2.00"),
            priceUnit = PriceUnit.DAILY,
            owner = owner,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            latitude = BigDecimal("48.775846"),
            longitude = BigDecimal("9.182932"),
            address = "Stadtmitte"
        )
        `when`(itemRepository.findAll()).thenReturn(listOf(item1, item2))

        val result: List<ItemResponseDto> = listingService.findAll()

        assertEquals(2, result.size)
        // Erste DTO
        with(result[0]) {
            assertEquals(101L, id)
            assertEquals("Item1", title)
            assertEquals("D1", description)
            assertEquals(BigDecimal("1.00"), priceAmount)
            assertEquals(PriceUnit.HOURLY, priceUnit)
            assertEquals(1L, ownerId)
            assertEquals("A", ownerDisplayName)
            assertNull(latitude)
            assertNull(longitude)
            assertNull(address)
        }
        // Zweite DTO
        with(result[1]) {
            assertEquals(102L, id)
            assertEquals("Item2", title)
            assertEquals("D2", description)
            assertEquals(BigDecimal("2.00"), priceAmount)
            assertEquals(PriceUnit.DAILY, priceUnit)
            assertEquals(BigDecimal("48.775846"), latitude)
            assertEquals(BigDecimal("9.182932"), longitude)
            assertEquals("Stadtmitte", address)
        }
    }

    /**
     * TestfindById: Wenn Item existiert, wird entsprechendes DTO zurückgegeben.
     */
    @Test
    fun `findById gibt korrektes Item zurück`() {
        val owner = AppUser(
            id = 3L,
            displayName = "B",
            email = "b@x.com",
            oauthProvider = "local",
            providerId = "b3"
        )
        val item = Item(
            id = 200L,
            title = "Gefunden",
            description = "Beschreibung",
            priceAmount = BigDecimal("5.50"),
            priceUnit = PriceUnit.HOURLY,
            owner = owner,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            latitude = BigDecimal("51.165691"),
            longitude = BigDecimal("10.451526"),
            address = "Mitte"
        )
        `when`(itemRepository.findById(200L)).thenReturn(Optional.of(item))

        val response: ItemResponseDto = listingService.findById(200L)

        assertEquals(200L, response.id)
        assertEquals("Gefunden", response.title)
        assertEquals("Beschreibung", response.description)
        assertEquals(BigDecimal("5.50"), response.priceAmount)
        assertEquals(PriceUnit.HOURLY, response.priceUnit)
        assertEquals(BigDecimal("51.165691"), response.latitude)
        assertEquals(BigDecimal("10.451526"), response.longitude)
        assertEquals("Mitte", response.address)
        assertEquals(3L, response.ownerId)
        assertEquals("B", response.ownerDisplayName)
    }

    /**
     * TestfindById: Wenn Item nicht existiert, wird IllegalArgumentException geworfen.
     */
    @Test
    fun `findById wirft bei nicht vorhandenem Item`() {
        `when`(itemRepository.findById(500L)).thenReturn(Optional.empty())

        assertThrows(IllegalArgumentException::class.java) {
            listingService.findById(500L)
        }
    }

    /**
     * TestfindByOwner: Wenn Owner existiert, werden dessen Items gemappt.
     */
    @Test
    fun `findByOwner gibt Items des Owners zurück`() {
        val ownerId = 4L
        val owner = AppUser(
            id = ownerId,
            displayName = "C",
            email = "c@x.com",
            oauthProvider = "local",
            providerId = "c4"
        )
        `when`(userRepository.findById(ownerId)).thenReturn(Optional.of(owner))

        val itemA = Item(
            id = 301L,
            title = "A",
            description = "A",
            priceAmount = BigDecimal("3.00"),
            priceUnit = PriceUnit.DAILY,
            owner = owner,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            latitude = null,
            longitude = null,
            address = ""
        )
        val itemB = Item(
            id = 302L,
            title = "B",
            description = "B",
            priceAmount = BigDecimal("4.00"),
            priceUnit = PriceUnit.HOURLY,
            owner = owner,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            latitude = null,
            longitude = null,
            address = ""
        )
        `when`(itemRepository.findByOwnerId(ownerId)).thenReturn(listOf(itemA, itemB))

        val result = listingService.findByOwner(ownerId)
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == 301L && it.ownerId == ownerId })
        assertTrue(result.any { it.id == 302L && it.ownerId == ownerId })
    }

    /**
     * TestfindByOwner: Wenn Owner nicht existiert, wird IllegalArgumentException geworfen.
     */
    @Test
    fun `findByOwner wirft bei nicht vorhandenem Owner`() {
        val ownerId = 999L
        `when`(userRepository.findById(ownerId)).thenReturn(Optional.empty())

        assertThrows(IllegalArgumentException::class.java) {
            listingService.findByOwner(ownerId)
        }
        verify(itemRepository, never()).findByOwnerId(anyLong())
    }

    /**
     * TestdeleteItem: Wenn Item existiert und Owner übereinstimmt, soll delete(...) aufgerufen werden.
     */
    @Test
    fun `deleteItem löscht Item bei korrektem Owner`() {
        val ownerId = 6L
        val itemId = 601L
        val owner = AppUser(
            id = ownerId,
            displayName = "D",
            email = "d@x.com",
            oauthProvider = "local",
            providerId = "d6"
        )
        val item = Item(
            id = itemId,
            title = "ZumLöschen",
            description = "",
            priceAmount = BigDecimal("0.00"),
            priceUnit = PriceUnit.HOURLY,
            owner = owner,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            latitude = null,
            longitude = null,
            address = ""
        )
        `when`(itemRepository.findById(itemId)).thenReturn(Optional.of(item))

        listingService.deleteItem(ownerId, itemId)

        verify(itemRepository).delete(item)
    }

    /**
     * TestdeleteItem: Wenn Item nicht existiert, wird IllegalArgumentException geworfen.
     */
    @Test
    fun `deleteItem wirft bei nicht vorhandenem Item`() {
        `when`(itemRepository.findById(888L)).thenReturn(Optional.empty())

        assertThrows(IllegalArgumentException::class.java) {
            listingService.deleteItem(1L, 888L)
        }
        verify(itemRepository, never()).delete(any(Item::class.java))
    }

    /**
     * TestdeleteItem: Wenn Owner nicht übereinstimmt, wird IllegalArgumentException geworfen.
     */
    @Test
    fun `deleteItem wirft bei falschem Owner`() {
        val owner = AppUser(
            id = 10L,
            displayName = "E",
            email = "e@x.com",
            oauthProvider = "local",
            providerId = "e10"
        )
        val anotherOwnerId = 11L
        val item = Item(
            id = 701L,
            title = "NichtMein",
            description = "",
            priceAmount = BigDecimal("2.00"),
            priceUnit = PriceUnit.DAILY,
            owner = owner,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            latitude = null,
            longitude = null,
            address = ""
        )
        `when`(itemRepository.findById(701L)).thenReturn(Optional.of(item))

        assertThrows(IllegalArgumentException::class.java) {
            listingService.deleteItem(anotherOwnerId, 701L)
        }
        verify(itemRepository, never()).delete(any(Item::class.java))
    }
}
