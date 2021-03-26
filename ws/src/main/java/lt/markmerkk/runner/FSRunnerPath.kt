package lt.markmerkk.runner

import java.io.File

interface FSRunnerPath {
    val toolDir: File
    val toolFile: File
    val input: File
    val outputDir: File
}