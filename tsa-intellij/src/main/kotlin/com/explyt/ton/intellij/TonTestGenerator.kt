package com.explyt.ton.intellij

import com.explyt.ton.intellij.model.TonFileType
import com.explyt.ton.intellij.model.TonGenerateTestModel
import com.explyt.ton.intellij.persist.TonSettingsPersistence
import com.explyt.ton.intellij.utils.ExplytFileManager
import com.explyt.ton.intellij.utils.coroutines.withBackgroundProgressOnPooledThread
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ton.intellij.func.psi.FuncFunction
import org.ton.mapping.makeMethodsMapping
import org.ton.sarif.toSarifReport
import org.usvm.machine.FiftAnalyzer
import org.usvm.machine.FuncAnalyzer
import org.usvm.machine.MethodId
import org.usvm.machine.TactAnalyzer
import java.io.File
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

@Service(Service.Level.PROJECT)
class TonTestGenerator(private val project: Project) {
    private val persistence = TonSettingsPersistence.getInstance()

    suspend fun generateTests(fileUnderTest: VirtualFile) = withContext(Dispatchers.Main) {
        val tonFileType = fileUnderTest.extension?.let { TonFileType.fromExtension(it) } ?: return@withContext
        val generateTestModel = TonGenerateTestModel(
            project = project,
            fileUnderTest = fileUnderTest,
            fileType = tonFileType,
            tonStdLibs = persistence.tonStdLibs.toMutableMap()
        )

        if (!TonGenerateTestsDialogWindow(generateTestModel).showAndGet()) {
            // user closed dialog
            return@withContext
        }

        persistence.tonStdLibs = generateTestModel.tonStdLibs

        val sourcesPath = Path.of(generateTestModel.fileUnderTest.path)
        val testFile = withBackgroundProgressOnPooledThread(project, "Generating TON tests") {
            val methodsMapping = hashMapOf<MethodId, FuncFunction>()
            makeMethodsMapping(
                funcSourcesPath = sourcesPath,
                project = project,
                mapping = methodsMapping,
                counter = AtomicInteger(1),
                visitedSources = hashSetOf()
            )

            val tvmAnalyzer = createTvmAnalyzer(generateTestModel)
            val testResult = tvmAnalyzer.analyzeAllMethods(sourcesPath)

            val sarifReport = testResult.toSarifReport(methodsMapping.mapValues { it.value.name!! })

            getTestFile(sourcesPath.toFile()).also { it.writeText(sarifReport) }
        }

        ExplytFileManager.getInstance(project).openFileInEditor(testFile)
    }

    private fun getTestFile(fileUnderTest: File): File {
        var i = 0
        while (true) {
            i++
            val suffix = "_test" + if (i == 1) "" else "$i"
            val testFileCandidate = fileUnderTest
                .parentFile
                .resolve(fileUnderTest.nameWithoutExtension + suffix + ".json")
            if (!testFileCandidate.exists()) {
                return testFileCandidate
            }
        }
    }

    private fun createTvmAnalyzer(generateTestModel: TonGenerateTestModel) = when (generateTestModel.fileType) {
        TonFileType.FIF -> FiftAnalyzer(
            fiftStdlibPath = generateTestModel.fiftStdLib!!.toPath()
        )
        TonFileType.FC -> FuncAnalyzer(
            fiftStdlibPath = generateTestModel.fiftStdLib!!.toPath(),
            funcStdlibPath = generateTestModel.funcStdLib!!.toPath(),
        )
        TonFileType.TACT -> TactAnalyzer
    }

    companion object {
        fun getInstance(project: Project): TonTestGenerator = project.service<TonTestGenerator>()
    }
}
