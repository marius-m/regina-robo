package lt.markmerkk

import io.sentry.Sentry
import lt.markmerkk.runner.FSSourcePath
import lt.markmerkk.runner.TTSFSInteractor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import javax.annotation.PostConstruct

@Configuration
@EnableScheduling
open class SchedulerConfig(
        @Autowired private val fsInteractor: TTSFSInteractor,
        @Autowired private val fsSourcePath: FSSourcePath
) {


    @Autowired lateinit var env: Environment
    @Autowired lateinit var buildConfig: BuildConfig

    @PostConstruct
    fun onAttach() {
        if (buildConfig.sentryDsn.isNotEmpty()) {
            Sentry.init { options ->
                options.isEnableExternalConfiguration = true
                options.dsn = buildConfig.sentryDsn
                options.environment = env.activeProfiles.joinToString(",")
                options.release = buildConfig.version
            }
        }
    }

    @Scheduled(fixedDelay = MINUTE * 5)
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