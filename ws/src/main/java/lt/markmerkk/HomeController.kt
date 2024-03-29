package lt.markmerkk

import lt.markmerkk.entities.RequestInput
import lt.markmerkk.entities.ResponseOutput
import lt.markmerkk.runner.DockerProcessStopper
import lt.markmerkk.runner.FSSourcePath
import lt.markmerkk.runner.TTSFSInteractor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.FileInputStream

@RestController
@RequestMapping("/api/")
class HomeController(
    @Autowired private val fsInteractor: TTSFSInteractor,
    @Autowired private val fsSourcePath: FSSourcePath,
    @Autowired private val converter: Converter,
    @Autowired private val resourceLoader: ResourceLoader,
    @Autowired private val dockerProcessStopper: DockerProcessStopper,
    @Autowired private val uuidGenerator: UUIDGenerator,
) {

    @RequestMapping(
            value = ["/test-sentry"],
            method = [RequestMethod.GET],
            produces = ["application/json"]
    )
    @ResponseBody
    fun test(): HttpStatus {
        throw IllegalArgumentException("Testing sentry reporting")
    }

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
        try {
            return converter.processRun(targetId = targetId, inputRequest = inputRequest)
        } catch (e: Exception) {
            dockerProcessStopper.stop(targetId)
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
    ): ResponseEntity<Resource> {
        if (!fsSourcePath.hasOutputById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")
        }
        val outputFile = fsSourcePath.outputFilesById(id)
                .firstOrNull { it.name == fileName }
        if (outputFile != null) {
            val resource = InputStreamResource(FileInputStream(outputFile))
            return ResponseEntity.ok()
                    .contentLength(outputFile.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${outputFile.name}\"")
                    .body(resource)
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