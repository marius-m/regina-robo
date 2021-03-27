package lt.markmerkk

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import lt.markmerkk.config.TTSRecordConfig
import lt.markmerkk.runner.ConvertProcessRunner
import lt.markmerkk.runner.FSSourcePath
import lt.markmerkk.runner.TTSAudioConverterMp3
import lt.markmerkk.runner.TTSAudioFileCombiner
import lt.markmerkk.runner.TTSFSInteractor
import lt.markmerkk.runner.TTSTextInteractor
import java.io.File
import java.time.LocalDateTime

class TTSConvertInteractor(
        private val fsInteractor: TTSFSInteractor,
        private val textInteractor: TTSTextInteractor,
        private val audioFileCombiner: TTSAudioFileCombiner,
        private val audioConverterMp3: TTSAudioConverterMp3,
        private val runner: ConvertProcessRunner,
        private val fsSourcePath: FSSourcePath
) {

    fun streamCleanUp(): Completable {
        return fsInteractor.cleanUpFormatter()
    }

    fun streamConvert(
            id: String,
            text: String,
            extras: Map<String, String>
    ): Flowable<List<File>> {
        val inputAsTextSections = textInteractor
            .split(text)
            .map { textInteractor.rmBreaks(it) }
            .map { textInteractor.removeUrls(it) }
            .map { textInteractor.replaceInvalidCharacters(it) }
            .map { textInteractor.splitLongSentences(it, TTSTextInteractor.DEFAULT_MAX_SYMBOLS_PER_WORD) }
        val indexTextSections: List<Pair<Int, String>> = inputAsTextSections
            .mapIndexed { index, section -> index to section }
        return Flowable.fromIterable(indexTextSections)
                .filter { it.second.isNotBlank() }
                .flatMapSingle { (index, textSection) ->
                    fsInteractor.createTextAsInput(
                            inputFile = fsSourcePath.inputSource(),
                            text = textSection,
                            encoding = Consts.ENCODING
                    ).map { index to it }
                }.flatMapSingle { (index, _) ->
                    runner.run(id = id, extras = extras)
                            .map { index to it }
                }.flatMapSingle { (index, formatFiles) ->
                    fsInteractor.extractToOutputDir(
                            id = id,
                            fileIndex = index,
                            files = formatFiles
                    )
                }
    }

    fun streamCombineAudio(id: String): Single<List<File>> {
        return audioFileCombiner.combineAudioFiles(id = id)
                .flatMap { fsInteractor.cleanToRootAudioOnly(id = id) }
    }

    fun streamConvertMp3(id: String): Single<List<File>> {
        return audioConverterMp3.convertToMp3(id = id)
    }

    fun streamRecordConfig(
            id: String,
            fetchTime: LocalDateTime,
            text: String,
            isStatusOk: Boolean,
            statusMessage: String
    ): Single<File> {
        val record = TTSRecordConfig(
                id = id,
                fetchDateTime = fetchTime,
                text = text,
                isStatusOk = isStatusOk,
                statusMessage = statusMessage
        )
        return fsInteractor.recordConfig(id, record)
    }

}