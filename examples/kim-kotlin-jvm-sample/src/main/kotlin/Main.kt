import com.ashampoo.kim.Kim
import com.ashampoo.kim.format.jpeg.JpegRewriter
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.input.JvmInputStreamByteReader
import com.ashampoo.kim.input.use
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.OutputStreamByteWriter
import com.ashampoo.kim.readMetadata
import java.io.File

fun main() {

    printMetadata()

    updateTakenDate()

    updateTakenDateLowLevel()
}

fun printMetadata() {

    val inputFile = File("testphoto.jpg")

    val imageMetadata = Kim.readMetadata(inputFile)

    println(imageMetadata)
}

/**
 * Shows how to update the taken date using Kim.update() API
 */
fun updateTakenDate() {

    val update = MetadataUpdate.TakenDate(System.currentTimeMillis())

    val inputFile = File("testphoto.jpg")
    val outputFile = File("testphoto_mod1.jpg")

    JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length()).use { byteReader ->

        OutputStreamByteWriter(outputFile.outputStream()).use { byteWriter ->

            Kim.update(byteReader, byteWriter, update)
        }
    }
}

/**
 * Shows how to update the taken date using the low level API.
 */
fun updateTakenDateLowLevel() {

    val inputFile = File("testphoto.jpg")
    val outputFile = File("testphoto_mod2.jpg")

    val metadata = Kim.readMetadata(inputFile) ?: return

    val outputSet: TiffOutputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

    val exifDirectory = outputSet.getOrCreateExifDirectory()

    exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
    exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, "2222:02:02 13:37:42")

    OutputStreamByteWriter(outputFile.outputStream()).use { outputStreamByteWriter ->

        JpegRewriter.updateExifMetadataLossless(
            byteReader = JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length()),
            byteWriter = outputStreamByteWriter,
            outputSet = outputSet
        )
    }
}
