package lt.markmerkk.runner

import com.google.common.base.Stopwatch
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import lt.markmerkk.Consts
import lt.markmerkk.UUIDGenerator
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

class ConvertProcessRunnerDocker(
    private val fsRunnerPath: FSRunnerPath,
    private val fsSourcePath: FSSourcePath,
    private val uuidGenerator: UUIDGenerator,
) : ConvertProcessRunner {

    private var dispTimeout: Disposable? = null
    private var dispInput: Disposable? = null

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
            val formatterInput = File(fsRunnerPath.toolDir, "/formatter-input")
            val formatterOutput = File(fsRunnerPath.toolDir, "/formatter-output")
            FileUtils.copyFileToDirectory(fsRunnerPath.input, formatterInput)
            val dockerContainerName = "wine1-${id}"
            val process = ProcessBuilder("/bin/bash", "./run.sh", dockerContainerName)
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
            val exitValue = process.exitValue()
            if (!isExitNoError || exitValue > 0) {
                logger.warn("Process either did not finish or hard errors!")
                Single.error<List<File>>(IllegalStateException("Did not exit with regular process finish"))
            } else {
                logger.debug("Process end (${sw.asMillis()})")
                Single.just(fsSourcePath.formatterFiles(formatterOutput))
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
