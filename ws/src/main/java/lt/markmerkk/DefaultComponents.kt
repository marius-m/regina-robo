package lt.markmerkk

import lt.markmerkk.runner.*
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
            fsRunnerPath: FSRunnerPath,
            timeProvider: TimeProvider
    ): FSSourcePath {
        return FSSourcePath(fsRunnerPath, timeProvider)
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
    open fun provideAudioCombiner(
            fsSourcePath: FSSourcePath,
            fsInteractor: TTSFSInteractor
    ): TTSAudioFileCombiner {
        return TTSAudioFileCombiner(fsSourcePath)
    }

    @Bean
    @Scope("singleton")
    open fun provideTextInteractor(): TTSTextInteractor {
        return TTSTextInteractor(maxSymbolsPerSection = TTSTextInteractor.DEFAULT_MAX_SYMBOLS)
    }

    @Bean
    @Scope("singleton")
    open fun provideConvertProcessRunner(
            resourceLoader: ResourceLoader,
            fsRunnerPath: FSRunnerPath,
            fsSourcePath: FSSourcePath
    ): ConvertProcessRunner {
        return ConvertProcessRunnerImpl(fsRunnerPath, fsSourcePath)
    }

    @Bean
    @Scope("singleton")
    open fun provideConvertInteractor(
            fsRunnerPath: FSRunnerPath,
            fsSourcePath: FSSourcePath,
            fsInteractor: TTSFSInteractor,
            audioFileCombiner: TTSAudioFileCombiner,
            textInteractor: TTSTextInteractor,
            convertProcessRunner: ConvertProcessRunner
    ): TTSConvertInteractor {
        return TTSConvertInteractor(
                fsInteractor,
                textInteractor,
                audioFileCombiner,
                convertProcessRunner,
                fsSourcePath
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

    @Bean
    @Scope("singleton")
    open fun provideConverter(
            timeProvider: TimeProvider,
            uuidGenerator: UUIDGenerator,
            convertInteractor: TTSConvertInteractor,
            fsSourcePath: FSSourcePath
    ): Converter {
        return Converter(
                timeProvider,
                uuidGenerator,
                convertInteractor,
                fsSourcePath
        )
    }

    private val logger = LoggerFactory.getLogger(DefaultComponents::class.java)!!

}