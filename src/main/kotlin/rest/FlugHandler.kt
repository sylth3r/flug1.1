package de.hska.flug.rest

import de.hska.flug.Application.Companion.ID_PATH_VAR
import com.fasterxml.jackson.core.JsonParseException
import de.hska.kunde.rest.util.itemLinks
import de.hska.kunde.rest.util.singleLinks
import de.hska.flug.entity.Flug
import org.slf4j.LoggerFactory
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import reactor.core.publisher.onErrorResume
import reactor.core.publisher.toMono
import de.hska.flug.service.FlugService
import java.net.URI
import javax.validation.ConstraintViolationException

@Component
@Suppress("TooManyFunctions")
class FlugHandler(private val service: FlugService) {

    fun findById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(ID_PATH_VAR)
        val flug = service.findById(id).map {
            it._links = request.uri().singleLinks()
            it
        }

        return flug.flatMap { ServerResponse.ok().body(it.toMono()) }
                .switchIfEmpty(ServerResponse.notFound().build())
    }

    fun find(request: ServerRequest): Mono<ServerResponse> {
        val queryParams = request.queryParams()

        // https://stackoverflow.com/questions/45903813/...
        //     ...webflux-functional-how-to-detect-an-empty-flux-and-return-404
        val flug = service.find(queryParams).map {
            if (it.id != null) {
                it.links = request.uri().itemLinks(it.id)
            }
            it
        }
                .collectList()

        return flug.flatMap {
            if (it.isEmpty()) ServerResponse.notFound().build() else ServerResponse.ok().body(it.toMono())
        }
    }

    fun create(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono<Flug>()
                .flatMap { service.create(it) }
                .flatMap {
                    LOGGER.trace("Flug abgespeichert: {}", it)
                    val location = URI("${request.uri()}${it.id}")
                    ServerResponse.created(location).build()
                }
                .onErrorResume(ConstraintViolationException::class) {
                    var msg = it.message
                    if (msg == null) {
                        ServerResponse.badRequest().build()
                    } else {
                        // Funktion "create" mit Parameter "kunde"
                        msg = msg.replace("create.flug.", "")
                        ServerResponse.badRequest().body(msg.toMono())
                    }
                }
                .onErrorResume(DecodingException::class) {
                    handleDecodingException(it)
                }

    fun deleteById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(ID_PATH_VAR)
        return service.deleteById(id).flatMap { ServerResponse.noContent().build() }
    }
    private
    fun handleDecodingException(e: DecodingException): Mono<ServerResponse> {
        val exception = e.cause
        return if (exception is JsonParseException) {
            val msg = exception.message
            LOGGER.debug(msg)
            if (msg == null) {
                ServerResponse.badRequest().build()
            } else {
                ServerResponse.badRequest().body(msg.toMono())
            }
        } else {
            ServerResponse.status(INTERNAL_SERVER_ERROR).build()
        }
    }

    private
    companion object {
        val LOGGER = LoggerFactory.getLogger(FlugHandler::class.java)
    }
}
