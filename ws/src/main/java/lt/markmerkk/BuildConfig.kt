package lt.markmerkk

data class BuildConfig(
    val version: String,
    val serverPort: String,
    val serverProfiles: List<String>,
    val sentryDsn: String,
    val dockerHost: String,
) {
    val resourceBasePath: String = "http://${dockerHost}:$serverPort"
}
