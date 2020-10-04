package lt.markmerkk.runner

import io.reactivex.rxjava3.core.Completable
import lt.markmerkk.Consts
import lt.markmerkk.Paths
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class ConvertProcessRunnerImpl(
        resourceLoader: ResourceLoader,
        private val fsRunnerPath: FSRunnerPath
): ConvertProcessRunner {

    /**
     * Runs a conversion process and returns output
     */
    override fun run(): Completable {
        return Completable.fromAction {
            logger.debug("--- Converter ---")
            logger.debug("Directory: in ${fsRunnerPath.toolDir.absolutePath}")
            logger.debug("Tool: in ${fsRunnerPath.toolFile.absolutePath}")
            logger.debug("Recording text: ${FileUtils.readFileToString(fsRunnerPath.input, Consts.ENCODING)}")
            val process = ProcessBuilder(fsRunnerPath.toolFile.absolutePath)
                    .directory(fsRunnerPath.toolDir)
                    .start()
            printStream("IS", process.inputStream)
            printStream("Error", process.errorStream)
            Completable.complete()
        }
    }

    private fun printStream(prefix: String, inputStream: InputStream) {
        val isr = InputStreamReader(inputStream)
        val br = BufferedReader(isr)

        var output = br.readLine()
        while (output != null) {
            logger.debug("$prefix: $output")
            output = br.readLine()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConvertProcessRunnerImpl::class.java)!!
    }

}