package lt.markmerkk.runner

import com.google.common.base.Stopwatch
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import lt.markmerkk.Consts
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

class ConvertProcessRunnerImpl(
        private val fsRunnerPath: FSRunnerPath,
        private val fsSourcePath: FSSourcePath
): ConvertProcessRunner {

    /**
     * Runs a conversion process and returns output
     *
     * Will try to kill process if it hangs more than 1minute.
     * Big text should not be affected, as only limited amount of text can
     * be translated anyway
     *
     * Process regularly takes around 5-10 seconds to finish
     */
    override fun run(id: String): Single<List<File>> {
        return Single.defer {
            val sw = Stopwatch.createStarted()
            logger.debug("--- Converter ---")
            logger.debug("Directory: in ${fsRunnerPath.toolDir.absolutePath}")
            logger.debug("Tool: in ${fsRunnerPath.toolFile.absolutePath}")
//            logger.debug("Recording text: ${FileUtils.readFileToString(fsRunnerPath.input, Consts.ENCODING)}")
            logger.debug("Recording text: <...>")
            logger.debug("Starting process (${sw.asMillis()})")
            val process = ProcessBuilder("wine", fsRunnerPath.toolFile.absolutePath)
                    .directory(fsRunnerPath.toolDir)
                    .start()
            logger.debug("Scheduling EXIT timeout (${sw.asMillis()})")
            val isExitNoError: Boolean = process.waitFor(60, TimeUnit.SECONDS)
            val inputResponse = extractInputStream(process.inputStream)
            val errorResponse = extractInputStream(process.errorStream)
            logger.debug("--- IS ---")
            logger.debug(inputResponse.joinToString("\n"))
            if (!isExitNoError || errorResponse.isNotEmpty()) {
                logger.warn("Process either did not finish or hard errors!")
                logger.debug("--- ES ---")
                logger.debug(errorResponse.joinToString("\n"))
                Single.error<List<File>>(IllegalStateException("Did not exit with regular process finish"))
            } else {
                logger.debug("Process end (${sw.asMillis()})")
                Single.just(fsSourcePath.formatterFiles())
            }
        }
    }

    private fun extractInputStream(inputStream: InputStream?): List<String> {
        if (inputStream == null) {
            return emptyList()
        }
        val isr = InputStreamReader(inputStream)
        val br = BufferedReader(isr)
        val isOutput = mutableListOf<String?>()
        var output = br.readLine()
        while (output != null) {
            output = br.readLine()
            isOutput.add(output)
        }
        return isOutput
                .filterNotNull()
                .filter { it.isNotBlank() }
                .toList()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConvertProcessRunnerImpl::class.java)!!
    }

}

private fun Stopwatch.asMillis(): String {
    return "${this.elapsed(TimeUnit.MILLISECONDS)}ms"
}
