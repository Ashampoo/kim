# Kim - Kotlin Image Metadata

[![Kotlin](https://img.shields.io/badge/kotlin-1.8.20-blue.svg?logo=kotlin)](httpw://kotlinlang.org)
![JVM](https://img.shields.io/badge/-JVM-gray.svg?style=flat)
![Android](https://img.shields.io/badge/-Android-gray.svg?style=flat)
![macOS](https://img.shields.io/badge/-macOS-gray.svg?style=flat)
![iOS](https://img.shields.io/badge/-iOS-gray.svg?style=flat)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kim&metric=coverage)](https://sonarcloud.io/summary/new_code?id=kim)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ashampoo/kim/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ashampoo/kim)

Kim is a Kotlin Multiplatform library for reading and writing image metadata.

It's part of [Ashampoo Photos](https://ashampoo.com/photos).

## Features

Current features:

* JPG: Read & Write EXIF, IPTC & XMP
* PNG: Read & Write EXIF Chunk & XMP. Also read non-standard EXIF & IPTC from zTXT chunks.
* TIFF: Read EXIF & XMP
* Handling of XMP content through [XMP Core for Kotlin Multiplatform](https://github.com/Ashampoo/xmpcore).

The future development of features on our part is driven entirely by the
needs of Ashampoo Photos, which, in turn, is driven by user community feedback.

## Installation

```
implementation("com.ashampoo:kim:0.3.0")
```

## Sample usage in Kotlin (for JVM)

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

    val takenDate = metadata.findStringValue(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)

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

## Sample usage in Java

This is the equivalent Java code as shown above.

While it may not be the most aesthetically pleasing, it functions correctly and will
serve as a helpful starting point if you're still in the process of transitioning to Kotlin.

```
import com.ashampoo.kim.Kim;
import com.ashampoo.kim.common.PhotoMetadataConverterKt;
import com.ashampoo.kim.format.ImageMetadata;
import com.ashampoo.kim.format.jpeg.JpegRewriter;
import com.ashampoo.kim.format.tiff.constants.ExifTag;
import com.ashampoo.kim.format.tiff.constants.TiffTag;
import com.ashampoo.kim.format.tiff.write.TiffOutputDirectory;
import com.ashampoo.kim.format.tiff.write.TiffOutputSet;
import com.ashampoo.kim.input.JvmInputStreamByteReader;
import com.ashampoo.kim.model.PhotoMetadata;
import com.ashampoo.kim.output.ByteWriter;
import com.ashampoo.kim.output.OutputStreamByteWriter;

import java.io.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {

        File inputFile = new File("myphoto.jpg");
        File outputFile = new File("myphoto_changed.jpg");

        JvmInputStreamByteReader byteReader = new JvmInputStreamByteReader(
                new FileInputStream((inputFile))
        );

        ImageMetadata metadata = Kim.readMetadata(byteReader);

        /* ImageMetadata has a proper toString() similar to the output of ExifTool */
        System.out.println(metadata);

        /*
         * Convert the raw imageMetadata to a summary object used by Ashampoo Photos.
         */
        PhotoMetadata photoMetadata =
                PhotoMetadataConverterKt.convertToPhotoMetadata(metadata, false);

        System.out.println(photoMetadata);

        Short orientation = metadata.findShortValue(TiffTag.INSTANCE.getTIFF_TAG_ORIENTATION());

        System.out.println("Orientation: " + orientation);

        String takenDate = metadata.findStringValue(ExifTag.INSTANCE.getEXIF_TAG_DATE_TIME_ORIGINAL());

        System.out.println("Taken date: " + takenDate);

        /*
         * Change orientation
         */

        TiffOutputSet outputSet = metadata.getExif() != null ?
                metadata.getExif().createOutputSet() : new TiffOutputSet();

        TiffOutputDirectory rootDirectory = outputSet.getOrCreateRootDirectory();

        rootDirectory.removeField(TiffTag.INSTANCE.getTIFF_TAG_ORIENTATION());
        rootDirectory.add(TiffTag.INSTANCE.getTIFF_TAG_ORIENTATION(), (short) 8);

        JvmInputStreamByteReader secondByteReader = new JvmInputStreamByteReader(
                new FileInputStream((inputFile))
        );

        ByteWriter byteWriter = new OutputStreamByteWriter(new FileOutputStream(outputFile));

        JpegRewriter.INSTANCE.updateExifMetadataLossless(secondByteReader, byteWriter, outputSet);
    }
}
```

## Limitations

* Kim can read non-standard EXIF & IPTC from PNG zTXT chunks, but it won't write them.
* Inability to update EXIF, IPTC and XMP in JPG files simultaneously.
* Insufficient error handling for broken or non-standard conforming files.

## Contributions

Contributions to Ashampoo Kim are welcome! If you encounter any issues,
have suggestions for improvements, or would like to contribute new features,
please feel free to submit a pull request.

## Acknowledgements

* JetBrains for making [Kotlin](https://kotlinlang.org).
* Apache Software Foundation for making [Apache Commons Imaging](https://commons.apache.org/proper/commons-imaging/).
* Drew Noakes for making [metadata-extractor](https://github.com/drewnoakes/metadata-extractor).
* Phil Harvey for making [ExifTool](https://exiftool.org/).
* [Unsplash](https://unsplash.com) for providing test images.

## License

This code is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).

See the `NOTICE.txt` file for required notices and attributions.
