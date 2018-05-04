package de.hska.flug.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import javax.validation.constraints.Pattern

@JsonPropertyOrder("start", "ziel", "preis")
data class Flug (
    @get:Pattern(regexp = Flug.ID_PATTERN, message = "{flug.id.pattern}")
    @JsonIgnore
    val id: String,
    val start: String?,
    val ziel: String,
    val preis: Preis?
) {
    @Suppress("PropertyName", "VariableNaming")
    var _links: Map<String, Map<String, String>>? = null

    var links: List<Map<String, String>>? = null

    @Suppress("ReturnCount")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Flug
        return id == other.id
    }

    override fun hashCode() = id.hashCode()

    companion object {
        private
        const val HEX_PATTERN = "[\\dA-Fa-f]"

        /**
         * Muster für eine UUID.
         */
        const val ID_PATTERN =
            "$HEX_PATTERN{8}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-" +
                    "$HEX_PATTERN{4}-$HEX_PATTERN{12}"

    }
}
