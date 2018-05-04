package de.hska.flug.service

import de.hska.flug.entity.Flug
import de.hska.flug.entity.Preis
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.validation.annotation.Validated
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.validation.Valid
import kotlin.math.abs

@Component
@Validated
class FlugService {

    fun findById(id: String) = if (id[0] == 'f' || id[0] == 'F') {
        Mono.empty()
    } else {
        createFlug(id).toMono()
    }

    @Suppress("ReturnCount")
    fun find(queryParams: MultiValueMap<String, String>): Flux<Flug> {
        if (queryParams.isEmpty()) {
            return findAll()
        }

        for ((key, value) in queryParams) {
            if (value.size != 1) {
                return Flux.empty()
            }

            val paramValue = value[0]
            when (key) {
                "id" -> return findById(paramValue).flux()
                "ziel" -> return findByZiel(paramValue)
            }
        }

        return Flux.empty()
    }

    fun findAll(): Flux<Flug> {
        val fluege = ArrayList<Flug>(MAX_FLUEGE)
        repeat(MAX_FLUEGE) {
            var id = UUID.randomUUID().toString()
            if (id[0] == 'f') {
                id = id.replaceFirst("f", "1")
            }
            val kunde = createFlug(id)
            fluege.add(kunde)
        }
        return fluege.toFlux()
    }

    @Suppress("ReturnCount")
    private
    fun findByZiel(ziel: String): Flux<Flug> {
        if (ziel.isBlank()) {
            return findAll()
        }

        if (ziel[0] == 'Z') {
            return Flux.empty()
        }

        val anzahl = ziel.length
        val fluege = ArrayList<Flug>(anzahl)
        repeat(anzahl) {
            var id = UUID.randomUUID().toString()
            if (id[0] == 'f') {
                id = id.replaceFirst("f", "1")
            }
            val flug = createFlug(id, ziel)
            fluege.add(flug)
        }
        return fluege.toFlux()
    }

    fun create(@Valid flug: Flug): Mono<Flug> {
        val neuerFlug = flug.copy(id = UUID.randomUUID().toString())
        LOGGER.trace("Neuer Flug: {}", neuerFlug)
        return neuerFlug.toMono()
    }

    fun deleteById(kundeId: String) = findById(kundeId)

    private
    fun createFlug(id: String): Flug {
        val anzahlZiele = ZIEL.size
        var zieleIdx = RANDOM.nextInt() % anzahlZiele
        if (zieleIdx < 0) {
            zieleIdx += anzahlZiele
        }
        val ziel = ZIEL[zieleIdx]
        return createFlug(id, ziel)
    }

    private
    fun createFlug(id: String, ziel: String): Flug {
        val preis = Preis(betrag = BigDecimal.ONE, waehrung = Currency.getInstance(Locale.GERMANY))

        return Flug(
                id = id,
                ziel = ziel,
                start= " ",
                preis = preis
        )
    }

    @Suppress("MagicNumber")
    private
    fun getYear() = (abs(RANDOM.nextLong()) % 60) + 1

    private
    companion object {
        const val MAX_FLUEGE = 8
        val START = listOf("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
        val ZIEL = listOf("London", "New York", "Frankfurt", "Paris", "Rom")
        val RANDOM by lazy {
            val seed = LocalDateTime.now()
                    .toEpochSecond(ZoneOffset.ofHours(0))
            Random(seed)
        }
        val LOGGER = LoggerFactory.getLogger(FlugService::class.java)
    }
}
