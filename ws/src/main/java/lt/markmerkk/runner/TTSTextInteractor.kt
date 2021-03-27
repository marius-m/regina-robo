package lt.markmerkk.runner

class TTSTextInteractor(
        private val maxSymbolsPerSection: Int
) {

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

    fun rmBreaks(inputSentence: String): String {
        return inputSentence
            .replace("\n", "; ")
            .replace("\r", "")
    }

    fun removeUrls(inputSentence: String): String {
        return inputSentence
                .replace(regexUrl, "")
    }

    fun splitLongSentences(inputSentence: String, maxSymbolCountInWord: Int): String {
        val words = inputSentence
                .split(" ")
                .map { splitLongWord(it, maxSymbolCountInWord) }
        return words.joinToString(" ")
    }

    private fun splitLongWord(inputWord: String, maxSymbolCount: Int): String {
        val sb = StringBuilder(inputWord)
        var currentSymbolIndex = 0
        var nextSplit = maxSymbolCount
        while (currentSymbolIndex <= sb.length) {
            if (nextSplit == currentSymbolIndex) {
                sb.insert(currentSymbolIndex, " ")
                nextSplit += maxSymbolCount + 1
            }
            currentSymbolIndex++
        }
        return sb.toString()
    }

    fun replaceInvalidCharacters(inputText: String): String {
        return inputText
                .replace("„", "\"")
                .replace("“", "\"")
                .replace("–", "-")
    }

    companion object {
        const val DEFAULT_MAX_SYMBOLS_PER_SECTION = 400
        const val DEFAULT_MAX_SYMBOLS_PER_WORD = 25
        val regexUrl = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".toRegex()
    }

}

