package lt.markmerkk.rabbit

import org.assertj.core.api.Assertions
import org.junit.Test

class MapExtractOrEmptyTest {

    @Test
    fun valid() {
        // Assemble
        val inputMap = mapOf<String, Any?>(
                "entityId" to "123"
        )

        // Act
        val result = inputMap.extractOrEmpty("entityId")

        // Assert
        Assertions.assertThat(result).isEqualTo("123")
    }

    @Test
    fun nullValue() {
        // Assemble
        val inputMap = mapOf<String, Any?>(
                "entityId" to null
        )

        // Act
        val result = inputMap.extractOrEmpty("entityId")

        // Assert
        Assertions.assertThat(result).isEqualTo("")
    }

    @Test
    fun empty() {
        // Assemble
        val inputMap = mapOf<String, Any?>(
                "entityId" to ""
        )

        // Act
        val result = inputMap.extractOrEmpty("entityId")

        // Assert
        Assertions.assertThat(result).isEqualTo("")
    }

    @Test
    fun noKey() {
        // Assemble
        val inputMap = mapOf<String, Any?>()

        // Act
        val result = inputMap.extractOrEmpty("entityId")

        // Assert
        Assertions.assertThat(result).isEqualTo("")
    }

    @Test
    fun nullAsString() {
        // Assemble
        val inputMap = mapOf<String, Any?>(
                "entityId" to "null"
        )

        // Act
        val result = inputMap.extractOrEmpty("entityId")

        // Assert
        Assertions.assertThat(result).isEqualTo("")
    }
}