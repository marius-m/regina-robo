package lt.markmerkk.runner

import com.google.common.base.Stopwatch
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.concurrent.TimeUnit

class DockerProcessStopper(
    private val fsRunnerPath: FSRunnerPath,
) {

    private var dispStop: Disposable? = null
    private var dispInput: Disposable? = null

    fun stop(
        id: String,
    ) {
        dispStop = Completable.fromAction {
            val dockerContainerName = "wine1-${id}"
            val sw = Stopwatch.createStarted()
            logger.debug("-- Docker force stop START --")
            logger.debug("Starting process (${sw.asMillis()})")
            val process = ProcessBuilder("/bin/bash", "./stop.sh", dockerContainerName)
                .directory(fsRunnerPath.toolDir)
                .redirectErrorStream(true)
                .start()
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
                Completable.error(IllegalStateException("Did not exit with regular process finish"))
            } else {
                logger.debug("Process end (${sw.asMillis()})")
                Completable.complete()
            }
        }.doOnEvent {
            logger.debug("--------------------------")
            logger.debug("--------------------------")
        }.doFinally {
            dispInput?.dispose()
        }.subscribeOn(Schedulers.io())
            .timeout(3L, TimeUnit.MINUTES)
            .subscribe({
                logger.debug("-- Force stop END (success) --")
            }, {
                logger.warn("-- Force stop END (failure) --", it)
            })
    }

    private fun inspectInputStream(
        prefix: String,
        inputStream: InputStream,
        scheduler: Scheduler
    ): Flowable<String> {
        return Flowable.create<String>(ProcessISEmitter(inputStream), BackpressureStrategy.BUFFER)
            .subscribeOn(scheduler)
            .doOnNext { logger.info("${prefix}:${it}") }
            .timeout(3L, TimeUnit.MINUTES)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConvertProcessRunnerImpl::class.java)!!
        const val PROCESS_TIMEOUT_SECONDS = 180L
    }
}

private fun Stopwatch.asMillis(): String {
    return "${this.elapsed(TimeUnit.MILLISECONDS)}ms"
}
