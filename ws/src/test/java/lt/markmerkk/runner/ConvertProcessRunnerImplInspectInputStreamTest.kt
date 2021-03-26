package lt.markmerkk.runner

import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

class ConvertProcessRunnerImplInspectInputStreamTest {

    @Mock lateinit var fsRunnerPath: FSRunnerPathWine
    @Mock lateinit var fsSourcePath: FSSourcePath
    lateinit var runner : ConvertProcessRunnerImpl

    private val testScheduler = TestScheduler()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        runner = ConvertProcessRunnerImpl(
            fsRunnerPath,
            fsSourcePath
        )
    }

    @Test
    fun valid() {
        // Assemble
        val input = "test1\ntest2\ntest3\n"

        // Act
        val result = runner.inspectInputStream("I", input.byteInputStream(), testScheduler)
            .test()
        testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

        // Assert
        result.assertComplete()
        result.assertNoErrors()
        result.assertValues("test1", "test2", "test3")
    }

    @Test
    fun noInput() {
        // Assemble
        val input = ""

        // Act
        val result = runner.inspectInputStream("I", input.byteInputStream(), testScheduler)
            .test()
        testScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

        // Assert
        result.assertComplete()
        result.assertNoValues()
    }

}