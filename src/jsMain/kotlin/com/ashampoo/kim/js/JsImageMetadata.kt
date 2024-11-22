package com.ashampoo.kim.js

@OptIn(ExperimentalJsExport::class)
@JsExport
public data class JsImageMetadata(
    val mimeType: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val xmp: String
) {

    public companion object {

        public val UNKNOWN: JsImageMetadata = JsImageMetadata(
            mimeType = UNKNOWN_IMAGE_MIME_TYPE,
            imageWidth = 0,
            imageHeight = 0,
            xmp = ""
        )
    }
}
