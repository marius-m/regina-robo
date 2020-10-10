package lt.markmerkk.runner

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import lt.markmerkk.TimeProvider
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime

class TTSFSInteractor(
        private val sourcePath: FSSourcePath,
        private val timeProvider: TimeProvider
) {

    fun cleanUpFormatter(): Completable {
        return Completable.fromAction {
            sourcePath.formatterFiles()
                    .forEach {
                        logger.debug("Removing: ${it.absolutePath}")
                        it.delete()
                    }
            Completable.complete()
        }
    }

    fun cleanUpOutput(outputDirs: List<File>): Completable {
        return Completable.fromAction {
            logger.info("--- Cleaning output dirs ---")
            outputDirs.forEach {
                logger.debug("File ${it.name} is old enough, removing...")
                it.deleteRecursively()
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
     * @return formatted audio and config files
     */
    fun extractToOutputDir(id: String, fileIndex: Int, files: List<File>): Single<List<File>> {
        return Single.defer {
            val outputDir = sourcePath.outputDirById(id)
            logger.debug("Extracting to ${outputDir.absolutePath}: $files")
            files.forEach { outputFile ->
                val targetExt = outputFile.extension
                val targetFile = File(outputDir, "${fileIndex}-${outputFile.nameWithoutExtension}.${targetExt}")
                FileUtils.copyFile(outputFile, targetFile)
            }
            val outputFilesById = sourcePath.outputFilesById(id)
            Single.just(outputFilesById)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TTSFSInteractor::class.java)!!
    }

}
