package lt.markmerkk.runner

import org.assertj.core.api.Assertions
import org.junit.Test

class TTSAudioFileCombinerExtractSequenceNumberTest {

    @Test
    fun zero() {
        // Assemble
        val fileName = "0-pastr_0_0.wav"

        // Act
        val result = TTSAudioFileCombiner.extractSequenceNumberOrZero(fileName)

        // Assert
        Assertions.assertThat(result).isEqualTo(0)
    }

    @Test
    fun five() {
        // Assemble
        val fileName = "5-pastr_0_0.wav"

        // Act
        val result = TTSAudioFileCombiner.extractSequenceNumberOrZero(fileName)

        // Assert
        Assertions.assertThat(result).isEqualTo(5)
    }

    @Test
    fun doubleDigits1() {
        // Assemble
        val fileName = "10-pastr_0_0.wav"

        // Act
        val result = TTSAudioFileCombiner.extractSequenceNumberOrZero(fileName)

        // Assert
        Assertions.assertThat(result).isEqualTo(10)
    }

    @Test
    fun doubleDigits2() {
        // Assemble
        val fileName = "21-pastr_0_0.wav"

        // Act
        val result = TTSAudioFileCombiner.extractSequenceNumberOrZero(fileName)

        // Assert
        Assertions.assertThat(result).isEqualTo(21)
    }

    @Test
    fun empty() {
        // Assemble
        val fileName = ""

        // Act
        val result = TTSAudioFileCombiner.extractSequenceNumberOrZero(fileName)

        // Assert
        Assertions.assertThat(result).isEqualTo(0)
    }

}