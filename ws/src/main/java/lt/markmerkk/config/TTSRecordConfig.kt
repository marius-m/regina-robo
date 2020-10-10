package lt.markmerkk.config

import org.slf4j.LoggerFactory
import org.springframework.util.Base64Utils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Represents record config
 */
data class TTSRecordConfig(
        val fetchDateTime: LocalDateTime,
        val id: String,
        val text: String
) {

    fun toMap(): Map<String, String> {
        return mapOf(
                KEY_FETCH_DATESTAMP to dateTimeFormatter.format(fetchDateTime),
                KEY_ID to id,
                KEY_TEXT to Base64Utils.encodeToString(text.toByteArray())
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TTSRecordConfig::class.java)!!
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        const val KEY_FETCH_DATESTAMP = "fetchDateStamp"
        const val KEY_ID = "id"
        const val KEY_TEXT = "text"
        private val mandatoryKeys = listOf(
                KEY_FETCH_DATESTAMP,
                KEY_ID,
                KEY_TEXT
        )

        fun fromProperties(properties: Properties?): TTSRecordConfig? {
            if (properties == null) return null
            val containsMandatoryProperties = properties.keys.containsAll(mandatoryKeys)
            if (!containsMandatoryProperties) {
                val missingKeys = mandatoryKeys
                        .filter { !properties.keys.contains(it) }
                logger.warn("Properties file does not contain all mandatory keys." +
                        " Mandataroy $mandatoryKeys / Missing: $missingKeys")
                return null
            }
            val fetchDateTime = LocalDateTime.parse(properties.getProperty(KEY_FETCH_DATESTAMP), dateTimeFormatter)
            val text = String(Base64Utils.decodeFromString(properties.getProperty(KEY_TEXT)))
            return TTSRecordConfig(
                    fetchDateTime = fetchDateTime,
                    id = properties.getProperty(KEY_ID),
                    text = text
            )
        }

    }

}