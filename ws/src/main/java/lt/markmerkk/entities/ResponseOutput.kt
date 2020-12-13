package lt.markmerkk.entities

data class ResponseOutput(
        val resourceBasePath: String,
        val id: String,
        val text: String,
        val recordDurationMillis: Long,
        val resources: List<String>,
        val extra: Map<String, Any>
)