package lt.markmerkk

import lt.markmerkk.entities.RequestInput
import lt.markmerkk.entities.ResponseOutput
import lt.markmerkk.runner.ConvertProcessRunnerImpl
import lt.markmerkk.runner.FSSourcePath
import lt.markmerkk.runner.TTSFSInteractor
import lt.markmerkk.runner.asNamedString
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.io.File

@RestController
@RequestMapping("/api/")
class HomeController(
        @Autowired private val runner: ConvertProcessRunnerImpl,
        @Autowired private val fsInteractor: TTSFSInteractor,
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
        fsInteractor.cleanUpOldOutput()
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
            @RequestBody inputText: RequestInput
    ): ResponseOutput {
        val targetId = uuidGenerator.generate()
        val outputFiles: List<File> = fsInteractor.cleanUpFormatter()
                .andThen(
                        fsInteractor.createTextAsInput(
                                inputFile = fsSourcePath.unencodedInputSource(),
                                text = inputText.inputText,
                                encoding = Consts.ENCODING_NO_ENCODE
                        )
                ).flatMap {
                    fsInteractor.createTextAsInput(
                            inputFile = fsSourcePath.inputSource(),
                            text = inputText.inputText,
                            encoding = Consts.ENCODING
                    )
                }.flatMapCompletable {
                    runner.run()
                }.andThen(fsInteractor.extractToOutputDir(targetId))
                .blockingGet()
        if (outputFiles.isNotEmpty()) {
            return ResponseOutput(
                    id = targetId,
                    text = inputText.inputText,
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
        val outputFile = fsInteractor.outputFilesById(id)
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
        return fsInteractor.outputFilesById(id)
                .map { it.name }
    }
}