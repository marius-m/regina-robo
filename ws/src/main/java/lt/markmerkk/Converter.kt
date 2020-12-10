package lt.markmerkk

import com.google.common.base.Stopwatch
import io.reactivex.rxjava3.core.Single
import lt.markmerkk.entities.RequestInput
import lt.markmerkk.entities.ResponseOutput
import lt.markmerkk.runner.FSSourcePath
import lt.markmerkk.runner.asNamedString
import java.io.File
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class Converter(
        private val timeProvider: TimeProvider,
        private val uuidGenerator: UUIDGenerator,
        private val convertInteractor: TTSConvertInteractor,
        private val fsSourcePath: FSSourcePath
) {

    private val isRunning = AtomicBoolean(false)

    @Throws(IllegalStateException::class)
    fun processRun(
            inputRequest: RequestInput,
            extra: Map<String, Any> = emptyMap()
    ): ResponseOutput {
        if (isRunning.get()) {
            throw IllegalStateException("Process already running")
        }
        isRunning.set(true)
        val targetId = uuidGenerator.generate()
        val now = timeProvider.now()
        val sw = Stopwatch.createStarted()
        val outputFiles: List<File> = convertInteractor.streamCleanUp()
                .andThen(convertInteractor.streamConvert(targetId, inputRequest.inputText))
                .toList()
                .flatMap { convertInteractor.streamCombineAudio(targetId) }
                .flatMap {
                    convertInteractor.streamRecordConfig(
                            id = targetId,
                            fetchTime = now,
                            text = inputRequest.inputText,
                            isStatusOk = true,
                            statusMessage = "Success"
                    )
                }
                .flatMap { Single.just(fsSourcePath.outputFilesById(targetId)) }
                .blockingGet()
        sw.stop()
        isRunning.set(false)
        val durationMillis = sw.elapsed(TimeUnit.MILLISECONDS)
        if (outputFiles.isNotEmpty()) {
            return ResponseOutput(
                    id = targetId,
                    text = inputRequest.inputText,
                    recordDurationMillis = durationMillis,
                    resources = outputFiles.asNamedString(),
                    extra = extra
            )
        } else {
            throw IllegalStateException("Could not convert")
        }
    }
}