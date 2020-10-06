package lt.markmerkk.runner

import lt.markmerkk.TimeProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.time.*


class TTSFSInteractorIsFileOldTest {

    @Test
    fun oneHourPass() {
        // Assemble
        val now = LocalDateTime.of(1970, 1, 1, 10, 0)
        val fileCreateTime = LocalDateTime.of(1970, 1, 1, 9, 0)

        // Act
        val result = TTSFSInteractor.isFileOld(
                fileCreateTime = fileCreateTime,
                now = now
        )

        // Assert
        assertThat(result).isFalse()
    }

    @Test
    fun manyHoursAhead() {
        // Assemble
        val now = LocalDateTime.of(1970, 1, 1, 19, 0)
        val fileCreateTime = LocalDateTime.of(1970, 1, 1, 9, 0)

        // Act
        val result = TTSFSInteractor.isFileOld(
                fileCreateTime = fileCreateTime,
                now = now
        )

        // Assert
        assertThat(result).isTrue()
    }

    @Test
    fun exactly3Hours() {
        // Assemble
        val now = LocalDateTime.of(1970, 1, 1, 12, 0)
        val fileCreateTime = LocalDateTime.of(1970, 1, 1, 9, 0)

        // Act
        val result = TTSFSInteractor.isFileOld(
                fileCreateTime = fileCreateTime,
                now = now
        )

        // Assert
        assertThat(result).isTrue()
    }

}