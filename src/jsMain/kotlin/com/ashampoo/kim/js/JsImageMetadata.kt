package com.ashampoo.kim.js

@OptIn(ExperimentalJsExport::class)
@JsExport
public data class JsImageMetadata(
    val mimeType: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val xmp: String
)
