import org.gradle.api.logging.LogLevel
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * 出错时重复执行
 */
fun retrying(times: Int, block: ()-> Unit) {
    repeat(times) {time ->
        runCatching(block)
            .onSuccess { return }
            .onFailure {
                logger.warn("第$time 次下载出错", it)
            }
    }
    throw RuntimeException("错误次数过多")
}

/**
 * @param url 远程文件路径
 * @param localFile 本地文件路径（相对于build/tmp/deb）
 * @param hash sha256校验和，不传则不进行校验
 */
fun download(url: String, localFile: String, hash: CheckSum? = null) = retrying(5) {
    logger.log(LogLevel.INFO,"下载$url -> $localFile")
    val outFile = File(Environment.buildTempDebDir, localFile).also {
        checkParent(it)
        deleteSelf(it)
    }
    val connection = URL(url).openConnection()
    val completeSize = connection.contentLengthLong
    var downloaded = 0L

    var data = ByteArray(1024)
    connection
        .getInputStream()
        .let{ BufferedInputStream(it) }
        .use {
            val fos = BufferedOutputStream(FileOutputStream(outFile))
            var x: Int
            var lastProgressTime = -1L
            while (true)  {
                x = it.read(data, 0, 1024)
                if (x == -1) {
                    break
                }
                downloaded += x
                fos.write(data, 0, x)
                if (System.currentTimeMillis() - lastProgressTime >= 1000) {
                    lastProgressTime = System.currentTimeMillis()
                    logger.info("下载进度 $downloaded/$completeSize")
                }
            }
            fos.close()
        }

    hash ?: return@retrying
    outFile.inputStream().use {
        require(hash.check(it))
    }
}

fun srcFile(file: String, target: String) {
    logger.info("复制 $file -> $target")
    val originFile = File(Environment.srcDir, file)
    val targetFile = File(Environment.buildTempDebDir, target).also {
        checkParent(it)
        deleteSelf(it)
    }

    originFile.copyRecursively(targetFile)
}

fun unzip(file: String, target: String) {
    logger.info("解压缩 $file -> $target")
    val originFile = File(Environment.buildTempDebDir, file)
    val targetFile = File(Environment.buildTempDebDir, target).also {
        it.mkdirs()
    }

    execProcessWait("tar", "-xvf", originFile.canonicalPath, "-C", targetFile.canonicalPath)
}

fun deleteFileOrDir(file: String) {
    logger.info("删除 $file")
    File(Environment.buildTempDebDir, file).takeIf {
        it.exists()
    }?.deleteRecursively()
}

internal fun checkParent(file: File) = file.parentFile.mkdirs()

internal fun deleteSelf(file: File) = if (file.exists()) {
    file.deleteRecursively()
} else true

internal fun execProcessWait(command: String, vararg args: String = emptyArray()) {
    val commandAndArgs = listOf(command) + args
    logger.info("exec:" + commandAndArgs.joinToString(" "))
    val process = Runtime.getRuntime().exec(commandAndArgs.toTypedArray())
    val code = process.waitFor()
    process.inputStream.use { inputStream ->
        inputStream.readAllBytes().decodeToString().let {
            logger.debug(it)
        }
    }
    process.errorStream.use {
        val errorOutput = it.readAllBytes().decodeToString()
        logger.error(errorOutput)
    }
    require(code == 0)
}
