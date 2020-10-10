package lt.markmerkk.runner

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TTSTextInteractorSplitTest {

    lateinit var textInteractor: TTSTextInteractor

    private val maxSymbolsPerSection = 10

    @Before
    fun setUp() {
        textInteractor = TTSTextInteractor(maxSymbolsPerSection)
    }

    @Test
    fun noText() {
        val resultText = textInteractor.split("")

        assertThat(resultText.size).isEqualTo(1)
        assertThat(resultText[0]).isEqualTo("")
    }

    @Test
    fun allTextFitsInSection() {
        val resultText = textInteractor.split("text text")

        assertThat(resultText.size).isEqualTo(1)
        assertThat(resultText[0]).isEqualTo("text text")
    }

    @Test
    fun notAllTextFitsPerSection() {
        val resultText = textInteractor.split("text text text")

        assertThat(resultText.size).isEqualTo(2)
        assertThat(resultText[0]).isEqualTo("text text")
        assertThat(resultText[1]).isEqualTo("text")
    }

    @Test
    fun notAllTextFitsPerSection_moreMultipleSections() {
        val resultText = textInteractor.split("text text text text text")

        assertThat(resultText.size).isEqualTo(3)
        assertThat(resultText[0]).isEqualTo("text text")
        assertThat(resultText[1]).isEqualTo("text text")
        assertThat(resultText[2]).isEqualTo("text")
    }
}