import com.ashampoo.kim.Kim
import com.ashampoo.kim.input.JvmInputStreamByteReader
import com.ashampoo.kim.input.use
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.OutputStreamByteWriter
import com.ashampoo.kim.readMetadata
import java.io.File

fun main() {

    val testFile = File("testphoto.jpg")

    val imageMetadata = Kim.readMetadata(testFile)

    println("---")
    println(imageMetadata)
    println("---")

    val update = MetadataUpdate.TakenDate(System.currentTimeMillis())

    val outputFile = File("testphoto_mod.jpg")

    JvmInputStreamByteReader(testFile.inputStream(), testFile.length()).use { byteReader ->

        OutputStreamByteWriter(outputFile.outputStream()).use { byteWriter ->

            Kim.update(byteReader, byteWriter, update)
        }
    }
}
