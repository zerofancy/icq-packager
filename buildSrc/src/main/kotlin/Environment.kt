import org.gradle.api.Project
import java.io.File

object Environment {
    lateinit var project: Project
    val buildDir get() = project.buildDir
    val buildTempDir get() = File(buildDir, "tmp")
    val buildTempDebDir get() = File(buildTempDir, "deb")
    val buildOutputDebDir get() = File(buildDir, "output")
    val srcDir get() = File(project.projectDir, "src")

    fun init(project: Project) {
        this.project = project
    }
}

val logger get() = Environment.project.logger