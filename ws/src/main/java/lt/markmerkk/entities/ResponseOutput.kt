package lt.markmerkk.entities

data class ResponseOutput(
        val id: String,
        val text: String,
        val resources: List<String>
)