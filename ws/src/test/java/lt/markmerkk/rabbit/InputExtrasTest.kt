package lt.markmerkk.rabbit

import org.assertj.core.api.Assertions
import org.junit.Test

class InputExtrasTest {

    @Test
    fun valid() {
        // Assemble
        val inputMap = mapOf<String, Any?>(
                "text" to "123",
                "entityId" to "asdf1",
                "textId" to "asdf2"
        )

        // Act
        val result = InputExtras.fromMap(inputMap)

        // Assert
        Assertions.assertThat(result).isEqualTo(
                InputExtras(
                        text = "123",
                        extraEntityId = "asdf1",
                        extraTextId = "asdf2"
                )
        )
    }

    @Test
    fun noExtras() {
        // Assemble
        val inputMap = mapOf<String, Any?>(
                "text" to "123"
//                "entityId" to "asdf1",
//                "textId" to "asdf2"
        )

        // Act
        val result = InputExtras.fromMap(inputMap)

        // Assert
        Assertions.assertThat(result).isEqualTo(
                InputExtras(
                        text = "123",
                        extraEntityId = "",
                        extraTextId = ""
                )
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