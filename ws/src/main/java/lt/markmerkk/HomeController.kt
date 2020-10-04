package lt.markmerkk

import lt.markmerkk.entities.RequestInput
import lt.markmerkk.entities.ResponseOutput
import lt.markmerkk.runner.ConvertProcessRunnerImpl
import lt.markmerkk.runner.FSSourcePath
import lt.markmerkk.runner.TTSFSInteractor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/")
class HomeController(
        @Autowired private val runner: ConvertProcessRunnerImpl,
        @Autowired private val fsInteractor: TTSFSInteractor,
        @Autowired private val fsSourcePath: FSSourcePath
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
            value = ["/process-run"],
            method = [RequestMethod.POST],
            consumes = ["application/json"],
            produces = ["application/json"]
    )
    @ResponseBody
    fun processRun(
            @RequestBody inputText: RequestInput
    ): ResponseOutput {
        val outputFiles: List<String> = fsInteractor.cleanUpFormatter()
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
                }.andThen(fsInteractor.extractToOutputDir())
                .andThen(fsInteractor.printOutputFiles())
                .blockingGet()
        return ResponseOutput(
                outputResources = outputFiles
        )
    }
}