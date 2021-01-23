package lt.markmerkk.runner

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TTSTextInteractorSplitLongWordsTest {

    lateinit var textInteractor: TTSTextInteractor

    private val maxSymbolsPerSection = 10

    @Before
    fun setUp() {
        textInteractor = TTSTextInteractor(maxSymbolsPerSection)
    }

    @Test
    fun noText() {
        val result = textInteractor.splitLongSentences("", 10)

        assertThat(result).isEqualTo("")
    }

    @Test
    fun normalWord() {
        val result = textInteractor.splitLongSentences("marius", 10)

        assertThat(result).isEqualTo("marius")
    }

    @Test
    fun multipleWords() {
        val result = textInteractor.splitLongSentences("marius marius marius marius", 10)

        assertThat(result).isEqualTo("marius marius marius marius")
    }

    @Test
    fun longWord() {
        val result = textInteractor.splitLongSentences("besikiskiakopusteliaudamasbesikiskiakopusteliaudamas", 10)

        assertThat(result).isEqualTo("besikiskia kopustelia udamasbesi kiskiakopu steliaudam as")
    }

}