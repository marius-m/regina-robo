package lt.markmerkk.runner

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import lt.markmerkk.Consts
import lt.markmerkk.TimeProvider
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FilenameFilter
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.*

class TTSFSInteractor(
        private val sourcePath: FSSourcePath,
        private val timeProvider: TimeProvider
) {

    fun cleanUpFormatter(): Completable {
        return Completable.fromAction {
            formatterFiles()
                    .forEach {
                        logger.debug("Removing: ${it.absolutePath}")
                        it.delete()
                    }
            Completable.complete()
        }
    }

    fun cleanUpOldOutput(): Completable {
        return Completable.fromAction {
            val now = timeProvider.now()
            logger.debug("Removing old files from output dir (${now})")
            val outputDirs: List<File> = sourcePath.outputDir().listFiles()?.toList() ?: emptyList()
            outputDirs.forEach {
                val fileCreateTime = fileCreateTime(it)
                val isFileOld = isFileOld(fileCreateTime, now)
                if (isFileOld) {
                    logger.debug("File ${it.name} is old enough, removing... (${fileCreateTime})")
                    it.deleteRecursively()
                } else {
                    logger.debug("Skipping ${it.name} from removal (${fileCreateTime})")
                }
            }
            Completable.complete()
        }
    }

    /**
     * Creates data files for tts process to pick up
     */
    fun createTextAsInput(inputFile: File, text: String, encoding: String): Single<String> {
        return Single.defer {
            if (inputFile.exists()) {
                inputFile.delete()
            }
            val createResult = inputFile.createNewFile()
            if (!createResult) {
                throw IllegalStateException("Cannot create new data file")
            }
            FileUtils.writeStringToFile(inputFile, text, encoding)
            logger.debug("Recording text to ${inputFile.absolutePath}")
            Single.just(text)
        }
    }

    /**
     * Extracts formatter output to merge directory
     */
    fun extractToOutputDir(id: String): Single<List<File>> {
        return Single.defer {
            val outputDir = sourcePath.outputDirById(id)
            val formatterFiles = formatterFiles()
            logger.debug("Extracting to ${outputDir.absolutePath}: [${formatterFiles}]")
            formatterFiles
                    .forEach { FileUtils.copyFileToDirectory(it, outputDir) }
            val outputFilesById = outputFilesById(id)
            Single.just(outputFilesById)
        }
    }

    fun outputFilesById(id: String): List<File> {
        val outputFiles = sourcePath.outputDirById(id)
                .listFiles(FormatterOutputFilter()) ?: emptyArray()
        return outputFiles
                .toList()
    }

    /**
     * Lists all available output files in formatter
     */
    private fun formatterFiles(): List<File> {
        val formatterFiles = sourcePath.formatterDir()
                .listFiles(FormatterOutputFilter()) ?: emptyArray()
        return formatterFiles.toList()
                .plus(sourcePath.unencodedInputSource())
                .plus(sourcePath.inputSource())
                .onlyExisting()
    }

    private fun fileCreateTime(file: File): LocalDateTime {
        val createFileTime = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                .creationTime()
        return LocalDateTime.ofInstant(createFileTime.toInstant(), timeProvider.zoneId)
    }


    companion object {
        private val logger = LoggerFactory.getLogger(TTSFSInteractor::class.java)!!
        private val TIME_LIMIT_MINUTES = 3 * 60 // 3 hours

        // 3 hours = 60 * 3
        fun isFileOld(fileCreateTime: LocalDateTime, now: LocalDateTime): Boolean {
            return Duration.between(fileCreateTime, now).toMinutes() >= TIME_LIMIT_MINUTES
        }
    }

}

class FormatterOutputFilter: FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return name.startsWith(Consts.OUTPUT_PREFIX)
                && (name.endsWith(Consts.OUTPUT_EXTENSION_TXT) || name.endsWith(Consts.OUTPUT_EXTENSION_AUDIO))
                || name == Consts.INPUT_FILENAME
                || name == Consts.INPUT_FILENAME_ENCODED
    }
}

fun List<File>.onlyExisting(): List<File> = filter { it.exists() }
fun List<File>.onlyAudio(): List<File> = filter { it.name.endsWith(Consts.OUTPUT_EXTENSION_AUDIO) }
fun List<File>.asNamedString(): List<String> = map { it.name }