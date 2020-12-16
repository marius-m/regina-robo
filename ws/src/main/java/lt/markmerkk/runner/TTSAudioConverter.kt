package lt.markmerkk.runner

import io.reactivex.rxjava3.core.Single
import org.slf4j.LoggerFactory
import ws.schild.jave.AudioAttributes
import ws.schild.jave.Encoder
import ws.schild.jave.EncodingAttributes
import ws.schild.jave.MultimediaObject
import java.io.File

class TTSAudioConverterMp3(
        private val fsSourcePath: FSSourcePath
) {

    private val encoder = Encoder()

    /**
     * Combine audio files into 1 long file
     * @return output files
     */
    fun convertToMp3(
            id: String
    ): Single<List<File>> {
        return Single.defer {
            val outputFiles = fsSourcePath.outputFilesById(id)
            val targetDir = fsSourcePath.outputDirById(id)
            val outWavs: List<File> = outputFiles.onlyAudioWav()
            val outMp3s: List<File> = outputFiles
                    .onlyAudio()
                    .mapNotNull { source ->
                        val target = File(targetDir, "${source.nameWithoutExtension}.mp3")
                        convertAudioFile(source, target)
                    }
            // Either leave mp3s or waves based if everything was converted
            if (outWavs.size == outMp3s.size) {
                outWavs.forEach { it.delete() }
            } else {
                outMp3s.forEach { it.delete() }
            }
            Single.just(fsSourcePath.outputFilesById(id))
        }
    }

    private fun convertAudioFile(
            source: File,
            target: File
    ): File? {
        return try {
            val audio = AudioAttributes().apply {
                setCodec("libmp3lame")
                setBitRate(128000)
                setChannels(2)
                setSamplingRate(44100)
            }
            val attrs = EncodingAttributes().apply {
                format = "mp3"
                audioAttributes = audio
            }
            encoder.encode(MultimediaObject(source), target, attrs)
            target
        } catch (ex: Exception) {
            logger.warn("Was not able to convert to mp3", ex)
            null
        }
    }

    // 3.0.1
//    private fun convertAudioFile(
//            source: File,
//            target: File
//    ): File? {
//        return try {
//            val audio = AudioAttributes().apply {
//                setCodec("libmp3lame")
//                setBitRate(128000)
//                setChannels(2)
//                setSamplingRate(44100)
//            }
//            val attrs = EncodingAttributes().apply {
//                setInputFormat("wav")
//                setOutputFormat("mp3")
//                setAudioAttributes(audio)
//            }
//            encoder.encode(MultimediaObject(source), target, attrs)
//            target
//        } catch (ex: Exception) {
//            logger.warn("Was not able to convert to mp3", ex)
//            null
//        }
//    }

    companion object {
        private val logger = LoggerFactory.getLogger(TTSAudioConverterMp3::class.java)!!
    }

}

