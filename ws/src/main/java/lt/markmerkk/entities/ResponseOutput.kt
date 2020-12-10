package lt.markmerkk.entities

data class ResponseOutput(
        val requestId: String,
        val id: String,
        val text: String,
        val recordDurationMillis: Long,
        val resources: List<String>
)