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
            fsSourcePath: FSSourcePath
    ): TTSFSInteractor {
        return TTSFSInteractor(fsSourcePath)
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

//    @Bean
//    @Scope("singleton")
//    open fun provideTssAudioFileCombiner(
//            fsSourcePath: FSSourcePath,
//            ttsfsInteractor: TTSFSInteractor
//    ): TTSAudioFileCombiner {
//        return TTSAudioFileCombiner(fsSourcePath, ttsfsInteractor)
//    }

//    @Bean
//    @Scope("singleton")
//    open fun provideTtsTextInteractor(): TTSTextInteractor {
//        return TTSTextInteractor(maxSymbolsPerSection = 400)
//    }

//    @Bean
//    @Scope("singleton")
//    open fun provideConverterInteractor2(
//            fsInteractor: TTSFSInteractor,
//            convertProcessRunner: ConvertProcessRunner,
//            fsSourcePath: FSSourcePath,
//            ttsAudioFileCombiner: TTSAudioFileCombiner,
//            ttsTextInteractor: TTSTextInteractor
//    ): TTSConverterInteractor {
//        return TTSConverterInteractor(
//                fsInteractor,
//                convertProcessRunner,
//                fsSourcePath,
//                ttsAudioFileCombiner,
//                ttsTextInteractor
//        )
//    }
//
//    @Bean
//    @Scope("singleton")
//    open fun provideTTSRunner(
//            ttsConverterInteractor: TTSConverterInteractor,
//            ttsRunnerListenerRssMapper: TTSRunnerListenerRssMapper,
//            timeProvider: TimeProvider
//    ): TTSRunner {
//        val executor = Executors.newSingleThreadExecutor()
//        return TTSRunner(
//                ttsConverterInteractor,
//                Schedulers.from(executor),
//                timeProvider
//        ).apply {
//            onAttach()
//            register(ttsRunnerListenerRssMapper)
//        }
//    }
//
//    @Bean
//    @Scope("singleton")
//    open fun provideTTSScheduler(
//            ttsRunner: TTSRunner
//    ): TTSScheduler {
//        return TTSScheduler(ttsRunner)
//                .apply { onAttach() }
//    }
//
//    @Bean
//    @Scope("singleton")
//    open fun provideTTSMappingListener(
//            ttsRunner: TTSRunner,
//            ttsfsInteractor: TTSFSInteractor
//    ): TTSMappingListener {
//        return TTSMappingListener(
//                runnerListener = TTSResultListenerRunner(
//                        ttsRunner,
//                        Schedulers.from(Executors.newSingleThreadExecutor())
//                ),
//                fsListener = TTSResultListenerFs(ttsfsInteractor)
//        )
//    }
//
//    @Bean
//    @Scope("singleton")
//    open fun provideRssSourceProvider(
//            rssFeedDao: RssFeedDao,
//            uuidGenerator: UUIDGenerator
//    ): RssSourceProvider {
//        return RssSourceProvider(
//                rssLink = "https://www.delfi.lt/rss/feeds/daily.xml",
//                uuidGenerator = uuidGenerator
//        )
//    }
//
//    @Bean
//    @Scope("singleton")
//    open fun provideTimeProvider(): TimeProvider {
//        return TimeProvider(
//                zoneId = ZoneId.systemDefault(),
//                clock = Clock.systemUTC()
//        )
//    }

    private val logger = LoggerFactory.getLogger(DefaultComponents::class.java)!!

}