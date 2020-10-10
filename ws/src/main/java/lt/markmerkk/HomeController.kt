package lt.markmerkk

import com.google.common.base.Stopwatch
import io.reactivex.rxjava3.core.Single
import lt.markmerkk.entities.RequestInput
import lt.markmerkk.entities.ResponseOutput
import lt.markmerkk.runner.*
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/")
class HomeController(
        @Autowired private val fsInteractor: TTSFSInteractor,
        @Autowired private val fsSourcePath: FSSourcePath,
        @Autowired private val uuidGenerator: UUIDGenerator,
        @Autowired private val convertInteractor: TTSConvertInteractor,
        @Autowired private val timeProvider: TimeProvider
) {

    @RequestMapping(
            value = ["/process-clean"],
            method = [RequestMethod.GET],
            produces = ["application/json"]
    )
    @ResponseBody
    fun processClean(): HttpStatus {
        fsInteractor.cleanUpFormatter()
                .blockingSubscribe()
        return HttpStatus.OK
    }

    @RequestMapping(
            value = ["/output-clean"],
            method = [RequestMethod.GET],
            produces = ["application/json"]
    )
    @ResponseBody
    fun outputClean(): HttpStatus {
        fsInteractor.cleanUpOutput(fsSourcePath.outputFiles())
                .blockingSubscribe()
        return HttpStatus.OK
    }

    @RequestMapping(
            value = ["/process-run"],
            method = [RequestMethod.POST],
            consumes = ["application/json"],
            produces = ["application/json"]
    )
    @ResponseBody
    fun processRun(
            @RequestBody inputRequest: RequestInput
    ): ResponseOutput {
        val targetId = uuidGenerator.generate()
        val now = timeProvider.now()
        val sw = Stopwatch.createStarted()
        val outputFiles: List<File> = convertInteractor.streamCleanUp()
                .andThen(convertInteractor.streamConvert(targetId, inputRequest.inputText))
                .toList()
                .flatMap { convertInteractor.streamCombineAudio(targetId) }
                .flatMap { convertInteractor.streamRecordConfig(targetId, now, inputRequest.inputText) }
                .flatMap { Single.just(fsSourcePath.outputFilesById(targetId)) }
                .blockingGet()
        sw.stop()
        val durationMillis = sw.elapsed(TimeUnit.MILLISECONDS)
        if (outputFiles.isNotEmpty()) {
            return ResponseOutput(
                    id = targetId,
                    text = inputRequest.inputText,
                    recordDurationMillis = durationMillis,
                    resources = outputFiles.asNamedString()
            )
        } else {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Was not able to convert!")
        }
    }

    @RequestMapping(
            value = ["/output-fetch/{id}/{fileName}"],
            method = [RequestMethod.GET],
            produces = ["application/json"]
    )
    @ResponseBody
    fun fetchOutput(
            @PathVariable("id") id: String,
            @PathVariable("fileName") fileName: String
    ): ByteArray {
        if (!fsSourcePath.hasOutputById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")
        }
        val outputFile = fsSourcePath.outputFilesById(id)
                .firstOrNull { it.name == fileName }
        if (outputFile != null) {
            return FileUtils.readFileToByteArray(outputFile)
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")
        }
    }

    @RequestMapping(
            value = ["/output-files/{id}"],
            method = [RequestMethod.GET],
            produces = ["application/json"]
    )
    @ResponseBody
    fun outputFiles(
            @PathVariable("id") id: String
    ): List<String> {
        if (!fsSourcePath.hasOutputById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")
        }
        return fsSourcePath.outputFilesById(id)
                .map { it.name }
    }
}