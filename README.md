# Kim - Kotlin Image Metadata

[![Kotlin](https://img.shields.io/badge/kotlin-2.1.10-blue.svg?logo=kotlin)](httpw://kotlinlang.org)
![JVM](https://img.shields.io/badge/-JVM-gray.svg?style=flat)
![Android](https://img.shields.io/badge/-Android-gray.svg?style=flat)
![iOS](https://img.shields.io/badge/-iOS-gray.svg?style=flat)
![Windows](https://img.shields.io/badge/-Windows-gray.svg?style=flat)
![Linux](https://img.shields.io/badge/-Linux-gray.svg?style=flat)
![macOS](https://img.shields.io/badge/-macOS-gray.svg?style=flat)
![JS](https://img.shields.io/badge/-JS-gray.svg?style=flat)
![WASM](https://img.shields.io/badge/-WASM-gray.svg?style=flat)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kim&metric=coverage)](https://sonarcloud.io/summary/new_code?id=kim)

Kim is a Kotlin Multiplatform library for reading and writing image metadata.

It's part of [Ashampoo Photo Organizer](https://ashampoo.com/photo-organizer).

## Features

* JPG: Read & Write EXIF, IPTC & XMP
* PNG: Read & Write `eXIf` chunk & XMP
    + Also read non-standard EXIF & IPTC from `tEXt`/`zTXt` chunk
* WebP: Read & Write EXIF & XMP
* HEIC / AVIF: Read EXIF & XMP
* JXL: Read & Write EXIF & XMP of uncompressed files
* TIFF / RAW: Read EXIF & XMP
    + Full support for Adobe DNG, Canon CR2, Canon CR3 & Fujifilm RAF
    + Support for Nikon NEF, Sony ARW & Olympus ORF without lens info
    + Support for Panasonic RW2 without lens info and image size
    + API for preview image extraction of DNG, CR2, CR3, RAF, NEF, ARW & RW2
* Handling of XMP content through
  [XMP Core for Kotlin Multiplatform](https://github.com/Ashampoo/xmpcore)
* Convenient `Kim.update()` API to perform updates to the relevant places
    + JPG: Lossless rotation by modifying only one byte (where present)

The future development of features on our part is driven entirely by the needs
of Ashampoo Photo Organizer, which, in turn, is driven by user community feedback.

## Installation

```
implementation("com.ashampoo:kim:0.23")
```

For the targets `wasmJs` & `js` you also need to specify this:

```
implementation(npm("pako", "2.1.0"))
```

## Sample usages

### Read metadata

`Kim.readMetadata()` takes `kotlin.ByteArray` on all platforms and depending on
the platform also `kotlinx.io.files.Path`, Ktor `Source` & `ByteReadChannel`,
`java.io.File`, `java.io.InputStream`, `NSData` and `String` paths.

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
- Date taken
- GPS coordinates
- Camera make & model
- Lens make & model
- ISO, Exposure time, F-Number, Focal length
- Image title & description
- Rating
- `XMP:pick` flag
- Keywords
- Faces (XMP-mwg-rs regions, used by Picasa and others)
- Persons in image

```kotlin
val bytes: ByteArray = loadBytes()

val photoMetadata = Kim.readMetadata(bytes).convertToPhotoMetadata()
```

### Change orientation using low level API

```kotlin
val inputFile = File("myphoto.jpg")
val outputFile = File("myphoto_changed.jpg")

val metadata = Kim.readMetadata(inputFile)

val outputSet: TiffOutputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

val rootDirectory = outputSet.getOrCreateRootDirectory()

rootDirectory.removeField(TiffTag.TIFF_TAG_ORIENTATION)
rootDirectory.add(TiffTag.TIFF_TAG_ORIENTATION, 8)

OutputStreamByteWriter(outputFile.outputStream()).use { outputStreamByteWriter ->

    JpegRewriter.updateExifMetadataLossless(
        byteReader = JvmInputStreamByteReader(inputFile.inputStream(), inputFile.length()),
        byteWriter = outputStreamByteWriter,
        outputSet = outputSet
    )
}
```

See the [example project](examples/kim-kotlin-jvm-sample/src/main/kotlin/Main.kt) for more details.

### Change orientation using Kim.update() API

```kotlin
val bytes: ByteArray = loadBytes()

val newBytes = Kim.update(
    bytes = bytes,
    update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
)
```

See [AbstractUpdaterTest](src/commonTest/kotlin/com/ashampoo/kim/format/AbstractUpdaterTest.kt) for more samples.

### Update thumbnail using Kim.update() API

```kotlin
val bytes: ByteArray = loadBytes()
val thumbnailBytes: ByteArray = loadThumbnailBytes()

val newBytes = Kim.updateThumbnail(
    bytes = bytes,
    thumbnailBytes = thumbnailBytes
)
```

### Using Java

See the [Java example project](examples/kim-java-sample/src/main/java/Main.java) how to use Kim in Java projects.

## Limitations

* Inability to update EXIF, IPTC and XMP in JPG files simultaneously.
* Does not read the image size and orientation for HEIC, AVIF & JPEG XL.
* Does not read brotli compressed metadata of JPEG XL due to missing brotli KMP libs.
* MakerNote support is experimental and limited.
    + Can't extract preview image of ORF as offsets are burried into MakerNote.
    + Can't identify lens info of NEF, ARW, RW2 & ORF because this is constructed from MakerNote fields.
    + Missing image size for RW2 as this is also burried in MakerNotes.
* There is right now no convienient tooling for GeoTiff like there is for GPS.

### Regarding HEIC & AVIF metadata

In the processing of HEIC and AVIF files, we handle them as standard
ISOBMFF-based files, adhering rigorously to the EIC/ISO 14496-12 specification.
To preempt potential legal issues, we intentionally omit certain boxes outlined
in the HEIC specification, notably the image size ("ispe") and image rotation ("irot") boxes.
This approach extends to AVIF images, as they repurpose the same boxes.

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
