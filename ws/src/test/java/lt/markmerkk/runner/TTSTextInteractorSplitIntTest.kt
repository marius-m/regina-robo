package lt.markmerkk.runner

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TTSTextInteractorSplitIntTest {

    lateinit var textInteractor: TTSTextInteractor

    private val maxSymbolsPerSection = 100

    @Before
    fun setUp() {
        textInteractor = TTSTextInteractor(maxSymbolsPerSection)
    }

    @Test
    fun realText() {
        // Assemble
        val text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

        // Act
        val resultText = textInteractor.split(text)

        //Assert
        assertThat(resultText.size).isEqualTo(6)
        assertThat(resultText[0]).isEqualTo("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the")
        assertThat(resultText[1]).isEqualTo("industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type")
        assertThat(resultText[2]).isEqualTo("and scrambled it to make a type specimen book. It has survived not only five centuries, but also the")
        assertThat(resultText[3]).isEqualTo("leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s")
        assertThat(resultText[4]).isEqualTo("with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop")
        assertThat(resultText[5]).isEqualTo("publishing software like Aldus PageMaker including versions of Lorem Ipsum.")
    }

}