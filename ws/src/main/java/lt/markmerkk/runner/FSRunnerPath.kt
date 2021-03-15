package lt.markmerkk.runner

import com.google.common.base.Verify
import lt.markmerkk.Paths
import java.io.File

class FSRunnerPath(
    val toolDir: File,
    val outputDir: File,
) {
    init {
        Verify.verify(
            toolDir.exists(),
            "Format tool directory does not exist at '${toolDir.absolutePath}'"
        )
    }

    val toolFile: File = File(toolDir, Paths.TTS_TOOL)
    val input: File = File(toolDir, Paths.TTS_INPUT)
}