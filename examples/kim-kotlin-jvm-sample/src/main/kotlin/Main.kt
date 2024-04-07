import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.format.jpeg.JpegRewriter
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.constant.GeoTiffTag
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.format.tiff.write.TiffWriterLossy
import com.ashampoo.kim.input.DefaultRandomAccessByteReader
import com.ashampoo.kim.input.JvmInputStreamByteReader
import com.ashampoo.kim.input.KotlinIoSourceByteReader
import com.ashampoo.kim.input.use
import com.ashampoo.kim.jvm.readMetadata
import com.ashampoo.kim.kotlinx.KimKotlinx
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.OutputStreamByteWriter
import java.io.File

fun main() {

    printMetadata()

    updateTakenDate()

    updateTakenDateLowLevelApi()

    /* Various GeoTiff samples */
    setGeoTiffToJpeg()
    setGeoTiffToTiff()
    setGeoTiffToTiffUsingKotlinx()
    setGeoTiffToTiffUsingKotlinxAndTiffReader()
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

fun setGeoTiffToJpeg() {

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

/**
 * Shows how to update set GeoTiff to a TIF file using JVM API.
 */
fun setGeoTiffToTiff() {

    val inputFile = File("empty.tif")
    val outputFile = File("geotiff.tif")

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

        val tiffWriter = TiffWriterLossy(outputSet.byteOrder)

        tiffWriter.write(
            byteWriter = outputStreamByteWriter,
            outputSet = outputSet
        )
    }
}

/**
 * Shows how to update set GeoTiff to a TIF file using kotlinx-io.
 */
fun setGeoTiffToTiffUsingKotlinx() {

    val inputPath = kotlinx.io.files.Path("empty.tif")
    val outputPath = kotlinx.io.files.Path("geotiff_kotlinxio.tif")

    /*
     * Kim.readMetadata(inputPath) (extension function) is also possible,
     * but if IDEA yields errors this approach works better.
     */
    val metadata = KimKotlinx.readMetadata(inputPath) ?: return

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

    val byteArrayByteWriter = ByteArrayByteWriter()

    val tiffWriter = TiffWriterLossy(outputSet.byteOrder)

    tiffWriter.write(
        byteWriter = byteArrayByteWriter,
        outputSet = outputSet
    )

    val updatedBytes = byteArrayByteWriter.toByteArray()

    outputPath.writeBytes(updatedBytes)
}

/**
 * Shows how to update set GeoTiff to a TIF file using kotlinx-io
 * using low level API. Only use this if you now for sure it's a TIFF file,
 * because this skips the file format detection in Kim.readMetadata()
 */
fun setGeoTiffToTiffUsingKotlinxAndTiffReader() {

    val inputPath = kotlinx.io.files.Path("empty.tif")
    val outputPath = kotlinx.io.files.Path("geotiff_lowlevel.tif")

    val tiffContents: TiffContents? =
        KotlinIoSourceByteReader.read(inputPath) { byteReader ->
            byteReader?.let {

                /*
                 * TIFF files can be extremely large.
                 * It is not advisable to load them entirely into a ByteArray.
                 */
                val randomAccessByteReader = DefaultRandomAccessByteReader(byteReader)

                TiffReader.read(randomAccessByteReader)
            }
        }

    val outputSet: TiffOutputSet = tiffContents?.createOutputSet() ?: TiffOutputSet()

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

    val byteArrayByteWriter = ByteArrayByteWriter()

    val tiffWriter = TiffWriterLossy(outputSet.byteOrder)

    tiffWriter.write(
        byteWriter = byteArrayByteWriter,
        outputSet = outputSet
    )

    val updatedBytes = byteArrayByteWriter.toByteArray()

    outputPath.writeBytes(updatedBytes)
}
