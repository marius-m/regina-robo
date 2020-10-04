package lt.markmerkk

import lt.markmerkk.runner.ConvertProcessRunnerImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/")
class HomeController(
        @Autowired private val runner: ConvertProcessRunnerImpl
) {

    @RequestMapping(
            value = ["/"],
            method = [RequestMethod.GET],
            produces = ["application/json"]
    )
    @ResponseBody
    fun index(): String {
        return "{\"hello\": \"world\"}"
    }

    @RequestMapping(
            value = ["/process-run"],
            method = [RequestMethod.GET],
            produces = ["application/json"]
    )
    @ResponseBody
    fun runProcessor(): String {
        runner.run()
                .subscribe()
        return "{\"hello\": \"world\"}"
    }
}