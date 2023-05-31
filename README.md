# Kim - Kotlin Image Metadata
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.20-blue.svg?logo=kotlin)](httpw://kotlinlang.org)
![JVM](https://img.shields.io/badge/-JVM-gray.svg?style=flat)
![Android](https://img.shields.io/badge/-Android-gray.svg?style=flat)
![macOS](https://img.shields.io/badge/-macOS-gray.svg?style=flat)
![iOS](https://img.shields.io/badge/-iOS-gray.svg?style=flat)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kim&metric=coverage)](https://sonarcloud.io/summary/new_code?id=kim)
[![Latest release](https://img.shields.io/github/v/release/realAshampoo/kim?color=brightgreen&label=latest%20release)](https://github.com/realAshampoo/kim/releases/latest)

Kim is a Kotlin Multiplatform library for reading and writing image metadata.

It's part of [Ashampoo Photos](https://ashampoo.com/photos).

## Features

Current features:

* JPG: Read & Write EXIF, IPTC & XMP
* PNG: Read & Write EXIF Chunk & XMP (iTXT)
* TIFF: Read EXIF & XMP

The future development of features on our part is driven entirely by the
needs of Ashampoo Photos, which, in turn, is driven by user community feedback.

## Installation

Add to your `build.gradle.kts` for Multiplatform:
```
repositories {
    mavenCentral()
}

kotlin {
    val commonMain by sourceSets.getting {
        dependencies {
            implementation("com.ashampoo:kim:0.1.1")
        }
    }
}
```

Add to your `build.gradle.kts` for JVM:
```
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.ashampoo:kim:0.1.1")
}
```

## Sample usage (for JVM)
```
import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.convertToPhotoMetadata
import com.ashampoo.kim.format.jpeg.JpegRewriter
import com.ashampoo.kim.format.tiff.constants.ExifTag
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.input.JvmInputStreamByteReader
import com.ashampoo.kim.output.OutputStreamByteWriter
import com.ashampoo.kim.readMetadata
import java.io.File

fun main() {

    val inputFile = File("myphoto.jpg")

    /*
     * readMetadata() takes kotlin.ByteArray & io.ktor.utils.io.core.Input
     * on all platforms and depending on the platform also java.io.File,
     * java.io.InputStream, NSData and string paths.
     */
    val metadata = Kim.readMetadata(inputFile)

    if (metadata == null) {
        println("Image not found: $inputFile")
        return
    }

    /* ImageMetadata has a proper toString() similar to the output of ExifTool */
    println(metadata)

    val orientation = metadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)

    println("Orientation: $orientation")

    val takenDate = metadata.findTiffField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)

    println("Taken date: $takenDate")

    /*
     * Convert the raw imageMetadata to a summary object used by Ashampoo Photos.
     */
    val photoMetadata = metadata.convertToPhotoMetadata()

    /* PhotoMetadata is a data class with default toString() */
    println(photoMetadata)

    /*
     * Change orientation
     *
     * Note: This API will be improved in future versions.
     */

    val outputSet: TiffOutputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

    val rootDirectory = outputSet.getOrCreateRootDirectory()

    rootDirectory.removeField(TiffTag.TIFF_TAG_ORIENTATION)
    rootDirectory.add(TiffTag.TIFF_TAG_ORIENTATION, 8)

    val outputFile = File("myphoto_changed.jpg")

    OutputStreamByteWriter(
        outputFile.outputStream()
    ).use { outputStreamByteWriter ->

        JpegRewriter.updateExifMetadataLossless(
            byteReader = JvmInputStreamByteReader(inputFile.inputStream()),
            byteWriter = outputStreamByteWriter,
            outputSet = outputSet
        )
    }
}
```

## Limitations

We are actively working to address the following limitations in future updates:

* No interpretation of XMP data; Adobe XMP Toolkit SDK is currently required.
* No support for EXIF & IPTC in PNG zTXT chunks.
* Inability to update EXIF, IPTC and XMP in JPG files simultaneously.
* Insufficient error handling for broken or non-standard conforming files.

## Contributions

Contributions to Ashampoo Kim are welcome! If you encounter any issues,
have suggestions for improvements, or would like to contribute new features,
please feel free to submit a pull request.

## Acknowledgements

We thank the following organizations and people.

* JetBrains for making [Kotlin](https://kotlinlang.org).
* Apache Software Foundation for making [Apache Commons Imaging](https://commons.apache.org/proper/commons-imaging/).
* Drew Noakes for making [metadata-extractor](https://github.com/drewnoakes/metadata-extractor).
* Phil Harvey for making [ExifTool](https://exiftool.org/).
* [Unsplash](https://unsplash.com) for providing test images.

## License

This code is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).

See the `NOTICE.txt` file for required notices and attributions.
