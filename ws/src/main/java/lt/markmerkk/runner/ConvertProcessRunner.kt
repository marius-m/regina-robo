package lt.markmerkk.runner

import io.reactivex.rxjava3.core.Completable

/**
 * Will be running conversion through as an external process
 */
interface ConvertProcessRunner {

    /**
     * Runs a conversion process and returns output
     */
    fun run(): Completable
}