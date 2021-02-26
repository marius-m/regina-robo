package lt.markmerkk.runner

import io.reactivex.rxjava3.core.FlowableEmitter
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class ProcessISEmitter(
    private val inputStream: InputStream
): FlowableOnSubscribe<String> {
    override fun subscribe(emitter: FlowableEmitter<String>) {
        val isr = InputStreamReader(inputStream)
        val br = BufferedReader(isr)
        try {
            var output: String? = br.readLine()
            while (output != null && !emitter.isCancelled) {
                emitter.onNext(output)
                output = br.readLine()
            }
        } catch (e: Exception) {
            emitter.onError(e)
        } finally {
            emitter.onComplete()
        }
    }

}