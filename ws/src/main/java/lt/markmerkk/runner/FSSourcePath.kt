package lt.markmerkk.runner

import lt.markmerkk.Consts
import lt.markmerkk.Paths
import lt.markmerkk.TimeProvider
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FilenameFilter
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.Duration
import java.time.LocalDateTime

/**
 * Provides file source paths
 */
class FSSourcePath(
        private val fsRunnerPath: FSRunnerPath,
        private val timeProvider: TimeProvider
) {

    fun unencodedInputSource(): File {
        return File(fsRunnerPath.toolDir, Paths.TTS_INPUT_NO_ENCODING)
    }

    fun inputSource(): File {
        return File(fsRunnerPath.toolDir, Paths.TTS_INPUT)
    }

    fun formatterDir(): File {
        return fsRunnerPath.toolDir
    }

    fun outputDir(): File {
        val outputDir = fsRunnerPath.outputDir
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        return outputDir
    }

    fun outputDirById(id: String): File {
        val outputDirById = File(outputDir(), id)
        if (!outputDirById.exists()) {
            outputDirById.mkdirs()
        }
        return outputDirById
    }

    fun outputFiles(): List<File> {
        return outputDir().listFiles()?.toList() ?: emptyList()
    }

    fun outputFilesOld(): List<File> {
        val now = timeProvider.now()
        val outputFiles = outputDir().listFiles()?.toList() ?: emptyList()
        return outputFiles
                .filter { outputFile -> isFileOld(fileCreateTime(outputFile), now) }
    }

    fun rootAudioByIdTmp(id: String): File {
        val outputDirById = outputDirById(id)
        return File(outputDirById, Paths.TTS_OUTPUT_RECORD_RAW)
    }

    fun configById(id: String): File {
        val outputDirById = outputDirById(id)
        return File(outputDirById, Paths.TTS_OUTPUT_CONFIG)
    }

    fun hasOutputById(id: String): Boolean {
        return File(outputDir(), id).exists()
    }

    fun outputFilesById(id: String): List<File> {
        val outputFiles = outputDirById(id)
                .listFiles() ?: emptyArray()
        return outputFiles
                .toList()
    }

    /**
     * Lists all available output files in formatter
     */
    fun formatterFiles(formatterDir: File): List<File> {
        val formatterFiles = formatterDir
                .listFiles(FormatterOutputFilter()) ?: emptyArray()
        return formatterFiles.toList()
                .plus(unencodedInputSource())
                .plus(inputSource())
                .onlyExisting()
    }

    private fun fileCreateTime(file: File): LocalDateTime {
        val createFileTime = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                .creationTime()
        return LocalDateTime.ofInstant(createFileTime.toInstant(), timeProvider.zoneId)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FSSourcePath::class.java)!!
        private val TIME_LIMIT_MINUTES = 5 // 5 minutes

        // 3 hours = 60 * 3
        fun isFileOld(fileCreateTime: LocalDateTime, now: LocalDateTime): Boolean {
            return Duration.between(fileCreateTime, now).toMinutes() >= TIME_LIMIT_MINUTES
        }
    }

}

class FormatterOutputFilter: FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return name.startsWith(Consts.OUTPUT_PREFIX)
                && (name.endsWith(Consts.OUTPUT_EXTENSION_TXT) || name.endsWith(Consts.OUTPUT_EXTENSION_AUDIO_WAV))
                || name == Consts.INPUT_FILENAME
                || name == Consts.INPUT_FILENAME_ENCODED
    }
}

fun List<File>.onlyExisting(): List<File> = filter { it.exists() }
fun List<File>.onlyAudioWav(): List<File> = filter {
    it.name.endsWith(Consts.OUTPUT_EXTENSION_AUDIO_WAV)
}
fun List<File>.onlyAudioMp3(): List<File> = filter {
    it.name.endsWith(Consts.OUTPUT_EXTENSION_AUDIO_MP3)
}
fun List<File>.onlyAudio(): List<File> = filter {
    it.name.endsWith(Consts.OUTPUT_EXTENSION_AUDIO_WAV)
            || it.name.endsWith(Consts.OUTPUT_EXTENSION_AUDIO_MP3)
}
fun List<File>.asNamedString(): List<String> = map { it.name }
