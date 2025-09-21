package com.example.picchallenge.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun EnhancedImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }
    
    Image(
        painter = rememberAsyncImagePainter(model = imageRequest),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
fun NetworkImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onLoading: (() -> Unit)? = null,
    onSuccess: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .listener(
                onStart = { onLoading?.invoke() },
                onSuccess = { _, _ -> onSuccess?.invoke() },
                onError = { _, _ -> onError?.invoke() }
            )
            .build()
    }
    
    val painter = rememberAsyncImagePainter(model = imageRequest)
    
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
