/*
 * Copyright (C) 2017 - 2018 Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hska.kunde.config.dev

import de.hska.kunde.config.Settings.DEV
import java.security.Principal
import org.slf4j.LoggerFactory.getLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.web.server.WebFilter


/**
 * Interface, um im Profil _dev_ die Daten eines Requests und des zugehörigen
 * Response protokollieren zu können.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface LogRequest {
    /**
     * Bean-Definition, um die Daten eines Requests und des zugehörigen Response
     * zu protokollieren.
     *
     * @return Ein WebFilter, der protokolliert und den nächsten WEbFilter
     * aufruft.
     */
    @Bean
    @Profile(DEV)
    fun loggingFilter() =
        WebFilter { exchange, chain ->
            with(exchange.request) {
                exchange.getPrincipal<Principal>().subscribe {
                    LOGGER.debug("Principal:         ${it.name}")
                }
                LOGGER.debug("""
                    |REQUEST >>>
                    |URI:               $uri
                    |HTTP-Methode:      $methodValue
                    |Context-Pfad:      ${path.contextPath().value()}
                    |Pfad:              ${path.pathWithinApplication().value()}
                    |Query-Parameter:   $queryParams
                    |Headers:           $headers
                    |<<<
                    |""".trimMargin("|"))
            }

            chain.filter(exchange)
        }

    private
    companion object {
        val LOGGER = getLogger(LogRequest::class.java)
    }
}
