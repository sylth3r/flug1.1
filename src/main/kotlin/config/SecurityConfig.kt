/*
 * Copyright (C) 2016 - 2018 Juergen Zimmermann, Hochschule Karlsruhe
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
package de.hska.kunde.config

import java.util.Locale
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails
        .MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User

// https://github.com/spring-projects/spring-security/blob/5.0.2.RELEASE/samples/...
//       ...javaconfig/hellowebflux/src/main/java/sample/SecurityConfig.java


/**
 * Security-Konfiguration.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface SecurityConfig {
    /**
     * Bean-Definition, um den Zugriffsschutz an der REST-Schnittstelle zu
     * konfigurieren.
     *
     * @param http Injiziertes Objekt von `ServerHttpSecurity` als
     *      Ausgangspunkt für die Konfiguration.
     * @return Objekt von `SecurityWebFilterChain`
     */
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity) = http.authorizeExchange()
                    .pathMatchers(POST, KUNDE_PATH).permitAll()
                    .pathMatchers(GET, KUNDE_PATH, KUNDE_ID_PATH).hasRole(ADMIN)
                    .pathMatchers(PUT, KUNDE_PATH).hasRole(ADMIN)
                    .pathMatchers(PATCH, KUNDE_ID_PATH).hasRole(ADMIN)
                    .pathMatchers(DELETE, KUNDE_ID_PATH).hasRole(ADMIN)

                    .pathMatchers(GET, ACTUATOR_PATH, "$ACTUATOR_PATH/*")
                    .hasRole(ENDPOINT_ADMIN)
                    .pathMatchers(POST, "$ACTUATOR_PATH/*")
                    .hasRole(ENDPOINT_ADMIN)

                    .anyExchange().authenticated()

                    .and()
                    .httpBasic()
                    .and()
                    .formLogin().disable()
                    .csrf().disable()
                    // FIXME Disable FrameOptions: Clickjacking
                    .build()

    /**
     * Bean, um Test-User anzulegen. Dazu gehören jeweils ein Benutzername, ein
     * Passwort und diverse Rollen.
     *
     * @return Ein Objekt, mit dem diese Test-User verwaltet werden, z.B. für
     * die künftige Suche.
     */
    @Bean
    @Suppress("DEPRECATION")
    fun userDetailsRepository(): MapReactiveUserDetailsService {
        val admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("p")
                .roles(ADMIN, KUNDE, ENDPOINT_ADMIN)
                .build()
        val alpha = User.withDefaultPasswordEncoder()
                .username("alpha")
                .password("p")
                .roles(KUNDE)
                .build()
        return MapReactiveUserDetailsService(admin, alpha)
    }

    private
    companion object {
        val ADMIN = "ADMIN"
        val KUNDE = "KUNDE"
        val ENDPOINT_ADMIN = "ENDPOINT_ADMIN"

        val KUNDE_PATH = "/"
        val KUNDE_ID_PATH = "/*"
        val ACTUATOR_PATH = "/actuator"

        @Suppress("unused")
        val REALM by lazy {
            // Name der REALM = Name des Parent-Package in Grossbuchstaben,
            // z.B. KUNDEN
            val pkg = SecurityConfig::class.java.`package`.name
            val parentPkg = pkg.substring(0, pkg.lastIndexOf('.'))
            parentPkg.substring(parentPkg.lastIndexOf('.') + 1)
                    .toUpperCase(Locale.getDefault())
        }
    }
}
