package lt.markmerkk.rabbit

import org.assertj.core.api.Assertions
import org.junit.Test

class InputExtrasTest {

    @Test
    fun valid() {
        // Assemble
        val inputMap = mapOf<String, Any?>(
                "text" to "123"
        )

        // Act
        val result = InputExtras.fromMap(inputMap)

        // Assert
        Assertions.assertThat(result).isEqualTo(
                InputExtras(text = "123")
        )
    }

    @Test
    fun noText() {
        // Assemble
        val inputMap = mapOf<String, Any?>()

        // Act
        val result = InputExtras.fromMap(inputMap)

        // Assert
        Assertions.assertThat(result).isEqualTo(InputExtras.asEmpty())
    }

}