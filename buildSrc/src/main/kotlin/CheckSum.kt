import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.logging.LogLevel
import java.io.InputStream

interface CheckSum {
    class Md5(val value: String): CheckSum {
        override fun check(data: InputStream): Boolean {
            val real = DigestUtils.md5Hex(data)
            if (real.toLowerCase() == value.toLowerCase()) {
                return true
            }
            logger.log(LogLevel.ERROR, "校验和不匹配，预期 $value, 实际 $real")
            return false
        }
    }

    class Sha1(val value: String): CheckSum {
        override fun check(data: InputStream): Boolean {
            val real = DigestUtils.sha1Hex(data)
            if (real.toLowerCase() == value.toLowerCase()) {
                return true
            }
            logger.log(LogLevel.ERROR, "校验和不匹配，预期 $value, 实际 $real")
            return false
        }
    }

    class Sha256(val value: String): CheckSum {
        override fun check(data: InputStream): Boolean {
            val real = DigestUtils.sha256Hex(data)
            if (real.toLowerCase() == value.toLowerCase()) {
                return true
            }
            logger.log(LogLevel.ERROR, "校验和不匹配，预期 $value, 实际 $real")
            return false
        }
    }

    class Sha512(val value: String): CheckSum {
        override fun check(data: InputStream): Boolean {
            val real = DigestUtils.sha512Hex(data)
            if (real.toLowerCase() == value.toLowerCase()) {
                return true
            }
            logger.log(LogLevel.ERROR, "校验和不匹配，预期 $value, 实际 $real")
            return false
        }
    }

    fun check(data: InputStream): Boolean
}
