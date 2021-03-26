package lt.markmerkk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import lt.markmerkk.runner.ConvertProcessRunner
import lt.markmerkk.runner.ConvertProcessRunnerDocker
import lt.markmerkk.runner.FSRunnerPath
import lt.markmerkk.runner.FSRunnerPathDocker
import lt.markmerkk.runner.FSRunnerPathWine
import lt.markmerkk.runner.FSSourcePath
import lt.markmerkk.runner.TTSAudioConverterMp3
import lt.markmerkk.runner.TTSAudioFileCombiner
import lt.markmerkk.runner.TTSFSInteractor
import lt.markmerkk.runner.TTSTextInteractor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.core.env.Environment
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
        @Value("\${toolPath}") dirPathTool: String,
        @Value("\${outPath}") outPathTool: String
    ): FSRunnerPath {
        // return FSRunnerPathWine(
        //     toolDir = File(dirPathTool),
        //     outputDir = File(outPathTool),
        // )
        return FSRunnerPathDocker(
            toolDir = File(dirPathTool),
            outputDir = File(outPathTool),
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
    open fun provideAudioConverterMp3(
        fsSourcePath: FSSourcePath
    ): TTSAudioConverterMp3 {
        return TTSAudioConverterMp3(fsSourcePath)
    }

    @Bean
    @Scope("singleton")
    open fun provideTextInteractor(): TTSTextInteractor {
        return TTSTextInteractor(maxSymbolsPerSection = TTSTextInteractor.DEFAULT_MAX_SYMBOLS_PER_SECTION)
    }

    @Bean
    @Scope("singleton")
    open fun provideConvertProcessRunner(
        resourceLoader: ResourceLoader,
        fsRunnerPath: FSRunnerPath,
        fsSourcePath: FSSourcePath
    ): ConvertProcessRunner {
        //return ConvertProcessRunnerImpl(fsRunnerPath, fsSourcePath)
        return ConvertProcessRunnerDocker(fsRunnerPath, fsSourcePath)
    }

    @Bean
    @Scope("singleton")
    open fun provideConvertInteractor(
        fsRunnerPath: FSRunnerPath,
        fsSourcePath: FSSourcePath,
        fsInteractor: TTSFSInteractor,
        audioFileCombiner: TTSAudioFileCombiner,
        audioConverterMp3: TTSAudioConverterMp3,
        textInteractor: TTSTextInteractor,
        convertProcessRunner: ConvertProcessRunner
    ): TTSConvertInteractor {
        return TTSConvertInteractor(
            fsInteractor,
            textInteractor,
            audioFileCombiner,
            audioConverterMp3,
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
        fsSourcePath: FSSourcePath,
        buildConfig: BuildConfig
    ): Converter {
        return Converter(
            timeProvider,
            uuidGenerator,
            convertInteractor,
            fsSourcePath,
            buildConfig
        )
    }

    @Bean
    @Scope("singleton")
    open fun provideObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(KotlinModule())
        }
    }

    @Bean
    @Scope("singleton")
    open fun provideBuildConfig(
        @Value("\${version}") version: String,
        @Value("\${sentry.dsn}") sentryDsn: String,
        @Value("\${dockerPort}") dockerPort: String,
        @Value("\${dockerHost}") dockerHost: String,
        environment: Environment,
    ): BuildConfig {
        return BuildConfig(
            version = version,
            serverPort = dockerPort,
            serverProfiles = environment.activeProfiles.toList(),
            sentryDsn = sentryDsn,
            dockerHost = dockerHost,
        )
    }

    @Bean
    @Scope("singleton")
    open fun provideRabbitCreds(
        @Value("\${rabbit.user}") user: String,
        @Value("\${rabbit.pass}") pass: String
    ): RabbitCreds {
        return RabbitCreds(
            user = user,
            pass = pass
        )
    }

    private val logger = LoggerFactory.getLogger(DefaultComponents::class.java)!!
}