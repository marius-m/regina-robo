package lt.markmerkk.runner

import io.reactivex.rxjava3.core.Single
import java.io.File

/**
 * Will be running conversion through as an external process
 */
interface ConvertProcessRunner {

    /**
     * Runs a conversion process and returns output
     */
    fun run(id: String): Single<List<File>>
}