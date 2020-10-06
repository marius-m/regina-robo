package lt.markmerkk

import lt.markmerkk.runner.ConvertProcessRunnerImpl
import lt.markmerkk.runner.FSRunnerPath
import lt.markmerkk.runner.FSSourcePath
import lt.markmerkk.runner.TTSFSInteractor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import java.io.File
import java.time.Clock
import java.time.ZoneId

@Component
class DefaultComponents {

    @Bean
    @Scope("singleton")
    open fun provideUUIDGenerator(): UUIDGenerator {
        return UUIDGenerator()
    }

    @Bean
    @Scope("singleton")
    open fun provideFsRunnerPath(
            @Value("\${toolPath}") dirPathTool: String
    ): FSRunnerPath {
        return FSRunnerPath(
                toolDir = File(dirPathTool)
        )
    }

    @Bean
    @Scope("singleton")
    open fun provideFSSourcePath(
            fsRunnerPath: FSRunnerPath
    ): FSSourcePath {
        return FSSourcePath(fsRunnerPath)
    }

    @Bean
    @Scope("singleton")
    open fun provideFsInteractor(
            fsSourcePath: FSSourcePath,
            timeProvider: TimeProvider
    ): TTSFSInteractor {
        return TTSFSInteractor(fsSourcePath, timeProvider)
    }

    @Bean
    @Scope("singleton")
    @Profile("dev")
    open fun provideConvertProcessRunnerIntel(
            resourceLoader: ResourceLoader,
            fsRunnerPath: FSRunnerPath
    ): ConvertProcessRunnerImpl {
        return ConvertProcessRunnerImpl(
                resourceLoader,
                fsRunnerPath
        )
    }

    @Bean
    @Scope("singleton")
    open fun provideTimeProvider(): TimeProvider {
        return TimeProvider(
                zoneId = ZoneId.systemDefault(),
                clock = Clock.systemUTC()
        )
    }

    private val logger = LoggerFactory.getLogger(DefaultComponents::class.java)!!

}