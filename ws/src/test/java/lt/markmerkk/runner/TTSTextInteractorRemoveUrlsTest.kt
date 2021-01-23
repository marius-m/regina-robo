package lt.markmerkk.runner

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TTSTextInteractorRemoveUrlsTest {

    lateinit var textInteractor: TTSTextInteractor

    private val maxSymbolsPerSection = 10

    @Before
    fun setUp() {
        textInteractor = TTSTextInteractor(maxSymbolsPerSection)
    }

    @Test
    fun validLong() {
        val result = textInteractor.removeUrls("Šaltiniai: https://www.delfi.lt/sveikata/zinoti-sveika/kas-parasyta-i-lietuva-atvezamos-vakcinos-informaciniame-lapelyje-susipazinkite.d?id=86083643https://www.delfi.lt/sveikata/sveikatos-naujienos/norvegija-bando-issklaidyti-abejones-del-vakcinos-nuo-koronaviruso-po-senyvu-zmoniu-mirciu.d?id=86267395https://www.delfi.lt/news/daily/world/norvegija-tiria-mirciu-po-covid-19-skiepo-atvejus-iprasti-simptomai-galejo-tapti-kai-kuriu-silpnos-sveikatos-senyvo-amziaus-zmoniu-mirties-priezastimi.d?id=86233931")

        assertThat(result).isEqualTo("Šaltiniai: ")
    }

}