package lt.markmerkk.runner

import com.google.common.base.Stopwatch
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import lt.markmerkk.Consts
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class ConvertProcessRunnerImpl(
        private val fsRunnerPath: FSRunnerPath,
        private val fsSourcePath: FSSourcePath
) : ConvertProcessRunner {

    private var dispTimeout: Disposable? = null
    private var dispInput: Disposable? = null

    /**
     * Runs a conversion process and returns output
     *
     * Will try to kill process if it hangs more than 1minute.
     * Big text should not be affected, as only limited amount of text can
     * be translated anyway
     *
     * Process regularly takes around 5-10 seconds to finish
     */
    override fun run(
            id: String,
            extras: Map<String, String>
    ): Single<List<File>> {
        return Single.defer {
            disposeAll()
            val sw = Stopwatch.createStarted()
            logger.debug("---------------------")
            logger.debug("--- Convert START ---")
            logger.debug("---------------------")
            logger.debug("Directory: in ${fsRunnerPath.toolDir.absolutePath}")
            logger.debug("Tool: in ${fsRunnerPath.toolFile.absolutePath}")
            logger.debug("Record raw text: ${FileUtils.readFileToString(fsRunnerPath.input, Consts.ENCODING)}")
//            logger.debug("Recording text: <...>")
            logger.debug("Record extras: $extras")
            logger.debug("Starting process (${sw.asMillis()})")
            val process = ProcessBuilder("wine", fsRunnerPath.toolFile.absolutePath)
                    .directory(fsRunnerPath.toolDir)
                    .redirectErrorStream(true)
                    .start()
            dispTimeout = inspectProcessForTimeout(process)
            dispInput = inspectInputStream("I", process.inputStream, Schedulers.io())
                .ignoreElements()
                .subscribe({
                    logger.info("End reading stream: Complete")
                }, {
                    logger.info("End reading stream: Error", it)
                })
            logger.debug("Scheduling EXIT timeout '${PROCESS_TIMEOUT_SECONDS}' (${sw.asMillis()})")
            val isExitNoError = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!isExitNoError) {
                logger.warn("Process either did not finish or hard errors!")
                Single.error<List<File>>(IllegalStateException("Did not exit with regular process finish"))
            } else {
                logger.debug("Process end (${sw.asMillis()})")
                Single.just(fsSourcePath.formatterFiles())
            }
        }.doOnEvent { _, _ ->
            logger.debug("---------------------")
            logger.debug("--- Convert END ---")
            logger.debug("---------------------")
        }.doFinally {
            disposeAll()
        }
    }

    private fun inspectProcessForTimeout(process: Process): Disposable {
        return Flowable.interval(1L, TimeUnit.SECONDS, Schedulers.io())
                .subscribe({ ping ->
                    logger.info("Ping on process ${ping}s / ${PROCESS_TIMEOUT_SECONDS}s")
                    if (ping >= PROCESS_TIMEOUT_SECONDS) {
                        process.destroy()
                    }
                }, { error ->
                    logger.warn("Process close inpector error", error)
                })
    }

    // private fun inspectInputStream(prefix: String, inputStream: InputStream): Disposable {
    //     return Completable.fromAction {
    //         val isr = InputStreamReader(inputStream)
    //         val br = BufferedReader(isr)
    //         var output: String?
    //         do {
    //             output = br.readLine()
    //             logger.info("$prefix:$output")
    //         } while(output != null)
    //         Completable.complete()
    //     }.subscribeOn(Schedulers.io())
    //             .subscribe({
    //                 logger.info("End reading stream: Complete")
    //             }, {
    //                 logger.info("End reading stream: Error", it)
    //             })
    // }

    internal fun inspectInputStream(
        prefix: String,
        inputStream: InputStream,
        scheduler: Scheduler
    ): Flowable<String> {
        return Flowable.create<String>(ProcessISEmitter(inputStream), BackpressureStrategy.BUFFER)
            .subscribeOn(scheduler)
            .doOnNext { logger.info("${prefix}:${it}") }
            .timeout(3L, TimeUnit.MINUTES)
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

    private fun disposeAll() {
        dispTimeout?.dispose()
        dispInput?.dispose()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConvertProcessRunnerImpl::class.java)!!
        const val PROCESS_TIMEOUT_SECONDS = 30L
    }

}

private fun Stopwatch.asMillis(): String {
    return "${this.elapsed(TimeUnit.MILLISECONDS)}ms"
}
