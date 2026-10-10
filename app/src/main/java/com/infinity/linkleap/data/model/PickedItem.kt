package com.infinity.linkleap.data.model

import android.net.Uri

data class PickedItem(
    val name: String,
    val uri: Uri?,
    val path: String?,
    val type: String // app, image, video, file
)