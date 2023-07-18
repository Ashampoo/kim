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

* JPG: Read & Write EXIF, IPTC & XMP
* PNG: Read & Write `eXIf` chunk & XMP.  
  Also read non-standard EXIF & IPTC from `tEXt`/`zTXt` chunk.
* TIFF: Read EXIF & XMP
* Handling of XMP content through
  [XMP Core for Kotlin Multiplatform](https://github.com/Ashampoo/xmpcore).
* Convenicent `Kim.update()` API to perform updates to the relevant places.

The future development of features on our part is driven entirely by the needs
of Ashampoo Photos, which, in turn, is driven by user community feedback.

## Installation

```
implementation("com.ashampoo:kim:0.3.0")
```

## Sample usages

### Read metadata

`Kim.readMetadata()` takes `kotlin.ByteArray` & `io.ktor.utils.io.core.Input`
on all platforms and depending on the platform also `java.io.File`,
`java.io.InputStream`, `NSData` and string paths.

```kotlin
val bytes: ByteArray = loadBytes()

val metadata = Kim.readMetadata(bytes)

/* ImageMetadata has a proper toString() similar to the output of ExifTool */
println(metadata)

val orientation = metadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)

println("Orientation: $orientation")

val takenDate = metadata.findStringValue(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)

println("Taken date: $takenDate")
```

### Create high level summary object

This creates an instance of [PhotoMetadata](src/commonMain/kotlin/com/ashampoo/kim/model/PhotoMetadata.kt).
It contains the following:

- Image size
- Orientation
- Taken date
- GPS coordinates
- Camera make & model
- Lens make & model
- ISO, Exposure time, F-Number, Focal length
- Rating
- Keywords
- Faces (XMP-mwg-rs regions, used by Picasa and others)
- Persons in image

```kotlin
val bytes: ByteArray = loadBytes()

val photoMetadata = Kim.readMetadata(bytes).convertToPhotoMetadata()
```

### Change orientation using low level API

```kotlin
val metadata = Kim.readMetadata(File("myphoto.jpg"))

val outputSet: TiffOutputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

val rootDirectory = outputSet.getOrCreateRootDirectory()

rootDirectory.removeField(TiffTag.TIFF_TAG_ORIENTATION)
rootDirectory.add(TiffTag.TIFF_TAG_ORIENTATION, 8)

val inputStream = File("myphoto.jpg").inputStream()
val outputStream = File("myphoto_changed.jpg").outputStream()

OutputStreamByteWriter(
    File("myphoto_changed.jpg").outputStream()
).use { outputStreamByteWriter ->

    JpegRewriter.updateExifMetadataLossless(
        byteReader = JvmInputStreamByteReader(inputStream),
        byteWriter = outputStreamByteWriter,
        outputSet = outputSet
    )
}
```

### Change orientation using Kim.update() API

```kotlin
val bytes: ByteArray = loadBytes()

val newBytes = Kim.update(
    bytes = bytes,
    updates = setOf(
        MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
    )
)
```

See [JpegUpdaterTest](src/commonTest/kotlin/com/ashampoo/kim/format/jpeg/JpegUpdaterTest.kt)
for more samples.

## Limitations

* No XMP extraction out of TIFF
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

This code is under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

See the `NOTICE.txt` file for required notices and attributions.
