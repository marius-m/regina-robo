package lt.markmerkk.runner

import lt.markmerkk.Paths
import java.io.File

/**
 * Provides file source paths
 */
class FSSourcePath(
        private val fsRunnerPath: FSRunnerPath
) {

    fun unencodedInputSource(): File {
        return File(fsRunnerPath.toolDir, Paths.TTS_INPUT_NO_ENCODING)
    }

    fun inputSource(): File {
        return File(fsRunnerPath.toolDir, Paths.TTS_INPUT)
    }

    fun formatterDir(): File {
        return fsRunnerPath.toolDir
    }

    fun outputDir(): File {
        val outputDir = File(fsRunnerPath.toolDir, Paths.TTS_OUTPUT)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        return outputDir
    }

}