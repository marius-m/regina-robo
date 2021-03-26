package lt.markmerkk.runner

import com.google.common.base.Verify
import lt.markmerkk.Paths
import java.io.File

class FSRunnerPathDocker(
    override val toolDir: File,
    override val outputDir: File,
): FSRunnerPath {
    init {
        Verify.verify(
            toolDir.exists(),
            "Format tool directory does not exist at '${toolDir.absolutePath}'"
        )
    }

    override val toolFile: File = File(toolDir, Paths.TTS_TOOL)
    override val input: File = File(toolDir, Paths.TTS_INPUT)
}