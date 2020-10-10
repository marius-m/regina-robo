package lt.markmerkk

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
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

@RestController
@RequestMapping("/api/")
class HomeController(
        @Autowired private val runner: ConvertProcessRunnerImpl,
        @Autowired private val fsInteractor: TTSFSInteractor,
        @Autowired private val audioFileCombiner: TTSAudioFileCombiner,
        @Autowired private val textInteractor: TTSTextInteractor,
        @Autowired private val fsSourcePath: FSSourcePath,
        @Autowired private val uuidGenerator: UUIDGenerator
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
        val inputAsTextSections = textInteractor.split(inputRequest.inputText)
        val indexTextSections: List<Pair<Int, String>> = inputAsTextSections
                .mapIndexed { index, s -> index to s }
        val streamCleanUp: Completable = fsInteractor.cleanUpFormatter()
        val streamConvertText: Flowable<List<File>> = Flowable.fromIterable(indexTextSections)
                .flatMapSingle { (index, textSection) ->
                    fsInteractor.createTextAsInput(
                            inputFile = fsSourcePath.inputSource(),
                            text = textSection,
                            encoding = Consts.ENCODING
                    ).map { index to it }
                }.flatMapSingle { (index, _) ->
                    runner.run(id = targetId)
                            .map { index to it }
                }.flatMapSingle { (index, formatFiles) ->
                    fsInteractor.extractToOutputDir(
                            id = targetId,
                            fileIndex = index,
                            files = formatFiles
                    )
                }
        val streamCombineAudio = audioFileCombiner.combineAudioFiles(id = targetId)
        val outputFiles: List<File> = streamCleanUp
                .andThen(streamConvertText)
                .toList()
                .flatMap { streamCombineAudio }
                .blockingGet()
        if (outputFiles.isNotEmpty()) {
            return ResponseOutput(
                    id = targetId,
                    text = inputRequest.inputText,
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