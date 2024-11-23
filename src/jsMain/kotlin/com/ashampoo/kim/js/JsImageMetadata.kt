package com.ashampoo.kim.js

import kotlin.js.Date

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("ImageMetadata")
public data class JsImageMetadata(

    val mimeType: String?,

    /* Image resolution */
    val widthPx: Int?,
    val heightPx: Int?,

    /** tiff:Orientation */
    val orientation: Int?,

    /* Capture parameters */
    val takenDate: Date?,
    val gpsLatitude: Double?,
    val gpsLongitude: Double?,
    val cameraMake: String?,
    val cameraModel: String?,
    val lensMake: String?,
    val lensModel: String?,
    val iso: Int?,
    val exposureTime: Double?,
    val fNumber: Double?,
    val focalLength: Double?,

    /* Ratings & Tags */
    val flagged: Boolean,
    val rating: Int?,
    val keywords: Array<String>,

    /* Persons */
    val faces: Array<Face>,
    val personsInImage: Array<String>,

    /* Albums */
    val albums: Array<String>
)

@OptIn(ExperimentalJsExport::class)
@JsExport
public data class Face(
    val name: String,
    val xPos: Double,
    val yPos: Double,
    val width: Double,
    val height: Double
)
