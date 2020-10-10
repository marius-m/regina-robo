package lt.markmerkk

import lt.markmerkk.runner.FSSourcePath
import lt.markmerkk.runner.TTSFSInteractor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
class SchedulerConfig(
        @Autowired private val fsInteractor: TTSFSInteractor,
        @Autowired private val fsSourcePath: FSSourcePath
) {

    @Scheduled(fixedDelay = MINUTE * 30)
    fun scheduleCleanOutput() {
        fsInteractor.cleanUpOutput(fsSourcePath.outputFilesOld())
                .subscribe()
    }

    companion object {
        const val MILLI = 1L
        const val SECOND = MILLI * 1000L
        const val MINUTE = SECOND * 60L
        const val HOUR = MINUTE * 60L

        private val logger = LoggerFactory.getLogger(SchedulerConfig::class.java)!!
    }
}