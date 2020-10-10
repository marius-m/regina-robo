package lt.markmerkk.runner

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.SequenceInputStream
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem


/**
 * Joins audio files together
 */
class TTSAudioFileCombiner(
        private val fsSourcePath: FSSourcePath
) {

    /**
     * Combine audio files into 1 long file
     * @return output files
     */
    fun combineAudioFiles(
            id: String
    ): Single<List<File>> {
        return Single.defer {
            combineAudioFilesToOne(id, fsSourcePath.outputFilesById(id))
            Single.just(fsSourcePath.outputFilesById(id))
        }
    }

    /**
     * Combines input audio files to one file
     */
    private fun combineAudioFilesToOne(id: String, files: List<File>): File {
        logger.debug("--- Combining audio files ---")
        val audioWavs = extractOnlyAudioWavs(files)
                .sortedBy { it.name }
        val audioWavsNames = audioWavs.map { it.name }
        logger.debug("Files for merging: $audioWavsNames")
        val workingDir = fsSourcePath.outputDirById(id)
        val root = fsSourcePath.mergeAudioRootById(id)
        for (audioToMerge in audioWavs) {
            logger.debug("Merging ${audioToMerge.name} to ${root.name}")
            combineWavs(workingDir, root, audioToMerge)
        }
        logger.debug("--- --------------------- ---")
        return root
    }

    private fun combineWavs(
            workingDir: File,
            root: File,
            fileToAppend: File
    ): File {
        try {
            if (!root.exists()) {
                logger.debug("Root file does not exist, marking file as the first one to be merged")
                FileUtils.copyFile(fileToAppend, root)
                return root
            }
            val tmpFile = File.createTempFile("root-tmp", ".wav", workingDir)
            val clip1 = AudioSystem.getAudioInputStream(root)
            val clip2 = AudioSystem.getAudioInputStream(fileToAppend)

            val combineLength = clip1.frameLength + clip2.frameLength
            val appendedFiles = AudioInputStream(
                    SequenceInputStream(clip1, clip2),
                    clip1.format,
                    combineLength
            )
            AudioSystem.write(
                    appendedFiles,
                    AudioFileFormat.Type.WAVE,
                    tmpFile
            )
            FileUtils.copyFile(tmpFile, root)
            FileUtils.deleteQuietly(tmpFile)
            return root
        } catch (e: Exception) {
            logger.error("Error combining files together", e)
        }
        throw IllegalArgumentException("Error combining audio files")
    }

    private fun extractOnlyAudioWavs(files: List<File>): List<File> {
        return files.filter { it.absolutePath.endsWith(".wav") }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TTSAudioFileCombiner::class.java)!!
    }

}