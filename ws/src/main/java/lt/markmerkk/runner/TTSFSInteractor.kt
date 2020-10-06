package lt.markmerkk.runner

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import lt.markmerkk.Consts
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FilenameFilter

class TTSFSInteractor(
        private val sourcePath: FSSourcePath
) {

    /**
     * Cleans up formatter from output files
     */
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
    fun extractToOutputDir(): Completable {
        return Completable.defer {
            val outputDir = sourcePath.outputDir()
            val formatterFiles = formatterFiles()
            logger.debug("Extracting to ${outputDir.absolutePath}: [${formatterFiles}]")
            formatterFiles
                    .forEach { FileUtils.copyFileToDirectory(it, outputDir) }
            Completable.complete()
        }
    }

    fun printOutputFiles(): Single<List<String>> {
        return Single.defer {
            val filesAsString = outputFiles()
                    .map { it.name }
            Single.just(filesAsString)
        }
    }

    /**
     * Lists all available output files
     */
    fun outputFiles(): List<File> {
        val outputFiles = sourcePath.outputDir()
                .listFiles(FormatterOutputFilter()) ?: emptyArray()
        return outputFiles
                .toList()
    }

    /**
     * Lists all available output files in formatter
     */
    fun formatterFiles(): List<File> {
        val formatterFiles = sourcePath.formatterDir()
                .listFiles(FormatterOutputFilter()) ?: emptyArray()
        return formatterFiles.toList()
                .plus(sourcePath.unencodedInputSource())
                .plus(sourcePath.inputSource())
                .onlyExisting()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TTSFSInteractor::class.java)!!
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

class FilesWithExtension(private val extensions: List<String>): FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return extensions
                .filter { name.endsWith(it) }
                .count() > 0
    }
}

fun List<File>.onlyExisting(): List<File> = filter { it.exists() }
fun List<File>.onlyAudio(): List<File> = filter { it.name.endsWith(Consts.OUTPUT_EXTENSION_AUDIO) }
