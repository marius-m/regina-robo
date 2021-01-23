package lt.markmerkk.entities

data class RequestInput(
        val inputText: String,
        val extraEntityId: String,
        val extraTextId: String
) {
    fun asMap(): Map<String, String> {
        return mapOf(
                "entityId" to extraEntityId,
                "textId" to extraTextId
        )
    }
}