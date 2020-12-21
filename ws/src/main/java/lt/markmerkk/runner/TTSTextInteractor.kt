package lt.markmerkk.runner

class TTSTextInteractor(
        private val maxSymbolsPerSection: Int
) {

    fun rmAllBreaks(inputText: String): String {
        return inputText
                .replace("\n", " ")
    }

    /**
     * Splits text into sections
     * Always return at least one section
     * @return text split into sections by [maxSymbolsPerSection]
     */
    fun split(
            inputText: String
    ): List<String> {
        val textInSections = mutableListOf<String>()
        val fullTextInSections = inputText
                .split("[ ]".toRegex())
        var currentSection = ""
        for (textSection in fullTextInSections) {
            val sectionSize = currentSection.length
            val nextSectionSize = sectionSize + textSection.length
            if (nextSectionSize < maxSymbolsPerSection) {
                currentSection += " $textSection"
            } else {
                textInSections.add(currentSection.trim())
                currentSection = textSection
            }
        }
        if (currentSection.isNotEmpty()) {
            textInSections.add(currentSection.trim())
        }
        return textInSections.toList()
    }


    companion object {
        const val DEFAULT_MAX_SYMBOLS = 400
    }

}

fun String.replaceInvalidCharacters(): String {
    return this
            .replace("„", "\"")
            .replace("“", "\"")
}
