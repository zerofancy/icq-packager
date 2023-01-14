import java.io.File
import java.nio.file.Files

data class ControlModel(
    var packageName: String = "",
    var version: String = "",
    var mantainer: String = "",
    var mantainerEmail: String = "",
    var priority: String = "optional",
    var architecture: String = "",
    var description: String = "",
    var depends: List<String> = listOf(),
    var installedSize: Long = 0,
    var homePage: String = "",
) {
    override fun toString(): String {
        return """
            Package: $packageName
            Version: $version
            Maintainer: $mantainer <$mantainerEmail>
            Priority: $priority
            Architecture: $architecture
            Description: $description
            Depends: ${depends.joinToString(", ")}
            Installed-Size: $installedSize
            Homepage: $homePage
            
        """.trimIndent()
    }
}

class ControlBuilder {
    class DependBuilder {
        private val deps = mutableListOf<String>()

        fun dep(dep: String) {
            deps.add(dep)
        }

        fun build(): List<String> = deps
    }

    private val controlModel = ControlModel()

    fun packageName(name: String) {
        controlModel.packageName = name
    }

    fun version(version: String) {
        controlModel.version = version
    }

    fun mantainer(name: String, email: String) {
        controlModel.mantainer = name
        controlModel.mantainerEmail = email
    }

    fun priority(value: String) {
        controlModel.priority = value
    }

    fun architecture(value: String) {
        controlModel.architecture = value
    }

    fun description(desc: String) {
        controlModel.description = desc
    }

    fun deps(builder: DependBuilder.() -> Unit) {
        val depsBuilder = DependBuilder()
        builder(depsBuilder)
        controlModel.depends = depsBuilder.build()
    }

    fun installSize(size: Long) {
        controlModel.installedSize = size
    }

    fun collectDirSize(path: String) = Files.walk(File(Environment.buildTempDebDir, path).toPath()).mapToLong { p -> p.toFile().length() }.sum().let {
        (it / 1024f).toLong()
    }

    fun homePage(page: String) {
        controlModel.homePage = page
    }

    fun build() = controlModel
}

fun generateControlFile(builder: ControlBuilder.() -> Unit) {
    logger.info("生成control文件")
    val controlBuilder = ControlBuilder()
    builder(controlBuilder)
    val controlModel = controlBuilder.build()

    logger.info(controlModel.toString())

    val targetFile = File(Environment.buildTempDebDir, "DEBIAN/control").also {
        checkParent(it)
        deleteSelf(it)
    }
    targetFile.writeText(controlModel.toString())
}

fun chmodForDEBIANDir() {
    logger.info("chmod")
    val dir = File(Environment.buildTempDebDir, "DEBIAN")
    execProcessWait("chmod", "755", "-R", dir.absolutePath)
}

fun packageWithDpkg() {
    Environment.buildOutputDebDir.run {
        deleteRecursively()
        mkdirs()
    }

    logger.info("开始打包作业")
    // 重新打包 (ubuntu新版本下默认使用zstd压缩，但这不能被debian/deepin现有版本支持，所以需要-Zxz)
    execProcessWait("dpkg-deb", "-b","-Zxz", Environment.buildTempDebDir.absolutePath, Environment.buildOutputDebDir.absolutePath + "/")
    logger.info("打包完成，输出目录 ${Environment.buildOutputDebDir}")
}