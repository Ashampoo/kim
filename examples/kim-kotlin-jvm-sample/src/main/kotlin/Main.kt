import com.ashampoo.kim.Kim
import com.ashampoo.kim.format.jpeg.JpegRewriter
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.constant.GeoTiffTag
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

    updateTakenDateLowLevelApi()

    setGeoTiff()
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
fun updateTakenDateLowLevelApi() {

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

fun setGeoTiff() {

    val inputFile = File("testphoto.jpg")
    val outputFile = File("testphoto_mod3.jpg")

    val metadata = Kim.readMetadata(inputFile) ?: return

    val outputSet: TiffOutputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

    val rootDirectory = outputSet.getOrCreateRootDirectory()

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_MODEL_PIXEL_SCALE_TAG,
        doubleArrayOf(0.0002303616678184751, -0.0001521606816798535, 0.0)
    )

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_MODEL_TIEPOINT_TAG,
        doubleArrayOf(0.0, 0.0, 0.0, 8.915687629578438, 48.92432542097789, 0.0)
    )

    rootDirectory.add(
        GeoTiffTag.EXIF_TAG_GEO_KEY_DIRECTORY_TAG,
        shortArrayOf(1, 0, 2, 3, 1024, 0, 1, 2, 2048, 0, 1, 4326, 1025, 0, 1, 2)
    )

    OutputStreamByteWriter(outputFile.outputStream()).use { outputStreamByteWriter ->

        JpegRewriter.updateExifMetadataLossless(
            byteReader = JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length()),
            byteWriter = outputStreamByteWriter,
            outputSet = outputSet
        )
    }
}
