package lt.markmerkk.runner

import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.File

class TTSAudioFileCombinerSortByPriorityTest {

    @Test
    fun valid() {
        // Assemble
        val files: List<File> = listOf(
                createFile("0-pastr_0_0.wav"),
                createFile("1-pastr_0_0.wav"),
                createFile("2-pastr_0_0.wav")
        )
        // Act
        val result = files.sortByAudioFileIndex()

        // Assert
        Assertions.assertThat(result).containsExactly(
                createFile("0-pastr_0_0.wav"),
                createFile("1-pastr_0_0.wav"),
                createFile("2-pastr_0_0.wav")
        )
    }

    @Test
    fun mixOrder() {
        // Assemble
        val files = listOf(
                createFile("1-pastr_0_0.wav"),
                createFile("0-pastr_0_0.wav"),
                createFile("2-pastr_0_0.wav")
        )
        // Act
        val result = files.sortByAudioFileIndex()

        // Assert
        Assertions.assertThat(result).containsExactly(
                createFile("0-pastr_0_0.wav"),
                createFile("1-pastr_0_0.wav"),
                createFile("2-pastr_0_0.wav")
        )
    }

    @Test
    fun mixMoreThanTen() {
        // Assemble
        val files = (0..24)
                .map { createFile("${it}-pastr_0_0.wav") }
        // Act
        val result = files.sortByAudioFileIndex()

        // Assert
        Assertions.assertThat(result).containsExactly(
                createFile("0-pastr_0_0.wav"),
                createFile("1-pastr_0_0.wav"),
                createFile("2-pastr_0_0.wav"),
                createFile("3-pastr_0_0.wav"),
                createFile("4-pastr_0_0.wav"),
                createFile("5-pastr_0_0.wav"),
                createFile("6-pastr_0_0.wav"),
                createFile("7-pastr_0_0.wav"),
                createFile("8-pastr_0_0.wav"),
                createFile("9-pastr_0_0.wav"),
                createFile("10-pastr_0_0.wav"),
                createFile("11-pastr_0_0.wav"),
                createFile("12-pastr_0_0.wav"),
                createFile("13-pastr_0_0.wav"),
                createFile("14-pastr_0_0.wav"),
                createFile("15-pastr_0_0.wav"),
                createFile("16-pastr_0_0.wav"),
                createFile("17-pastr_0_0.wav"),
                createFile("18-pastr_0_0.wav"),
                createFile("19-pastr_0_0.wav"),
                createFile("20-pastr_0_0.wav"),
                createFile("21-pastr_0_0.wav"),
                createFile("22-pastr_0_0.wav"),
                createFile("23-pastr_0_0.wav"),
                createFile("24-pastr_0_0.wav")
        )
    }


    private fun createFile(name: String): File {
        return File("${name}.wav")
    }

}
