/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.sketch.sample.ui.photo.pexels

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.panpf.sketch.drawable.MaskProgressDrawable
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.PauseLoadWhenScrollingDrawableDecodeInterceptor
import com.github.panpf.sketch.sample.R
import com.github.panpf.sketch.sample.R.color
import com.github.panpf.sketch.sample.R.drawable
import com.github.panpf.sketch.sample.image.ImageType.LIST
import com.github.panpf.sketch.sample.image.setApplySettings
import com.github.panpf.sketch.sample.model.Photo
import com.github.panpf.sketch.sample.prefsService
import com.github.panpf.sketch.sample.ui.common.compose.AppendState
import com.github.panpf.sketch.sample.util.letIf
import com.github.panpf.sketch.sample.widget.TextDrawable
import com.github.panpf.sketch.stateimage.IconStateImage
import com.github.panpf.sketch.stateimage.ResColor
import com.github.panpf.sketch.stateimage.saveCellularTrafficError
import com.github.panpf.tools4a.dimen.ktx.dp2px
import com.github.panpf.tools4a.toast.ktx.showLongToast
import com.google.accompanist.drawablepainter.DrawablePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun PhotoListContent(
    photoPagingFlow: Flow<PagingData<Photo>>,
    restartImageFlow: Flow<Any>,
    reloadFlow: Flow<Any>,
    animatedPlaceholder: Boolean = false,
    onClick: (items: List<Photo>, photo: Photo, index: Int) -> Unit
) {
    val lazyPagingItems = photoPagingFlow.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = reloadFlow) {
        scope.launch {
            reloadFlow.collect {
                lazyPagingItems.refresh()
            }
        }
    }
    val context = LocalContext.current
    val localView = LocalView.current
    LaunchedEffect(key1 = reloadFlow) {
        scope.launch {
            restartImageFlow.collect {
                // todo Look for ways to actively discard the old state redraw, and then listen for restartImageFlow to perform the redraw
                context.showLongToast("You need to scroll through the list manually to see the changes")
                localView.postInvalidate()
            }
        }
    }
    SwipeRefresh(
        state = SwipeRefreshState(lazyPagingItems.loadState.refresh is LoadState.Loading),
        onRefresh = { lazyPagingItems.refresh() }
    ) {
        val lazyGridState = rememberLazyGridState()
        LaunchedEffect(lazyGridState.isScrollInProgress) {
            PauseLoadWhenScrollingDrawableDecodeInterceptor.scrolling =
                lazyGridState.isScrollInProgress
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            state = lazyGridState,
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.grid_divider)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.grid_divider)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.grid_divider)),
        ) {
            items(
                count = lazyPagingItems.itemCount,
                key = { lazyPagingItems.peek(it)?.diffKey ?: "" },
                contentType = { 1 }
            ) { index ->
                val item = lazyPagingItems[index]
                item?.let {
                    PhotoContent(index, it, animatedPlaceholder) { photo, index ->
                        onClick(lazyPagingItems.itemSnapshotList.items, photo, index)
                    }
                }
            }

            if (lazyPagingItems.itemCount > 0) {
                item(
                    key = "AppendState",
                    span = { GridItemSpan(this.maxLineSpan) },
                    contentType = 2
                ) {
                    AppendState(lazyPagingItems.loadState.append) {
                        lazyPagingItems.retry()
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoContent(
    index: Int,
    photo: Photo,
    animatedPlaceholder: Boolean = false,
    onClick: (photo: Photo, index: Int) -> Unit
) {
    val context = LocalContext.current
    val dataFromLogoState = rememberDataFromLogoState()
    val mimeTypeLogoState = rememberMimeTypeLogoState {
        val newLogoDrawable: (String) -> Drawable = {
            TextDrawable.builder()
                .beginConfig()
                .width(((it.length + 1) * 6).dp2px)
                .height(16.dp2px)
                .fontSize(9.dp2px)
                .bold()
                .textColor(Color.WHITE)
                .endConfig()
                .buildRoundRect(it, Color.parseColor("#88000000"), 10.dp2px)
        }
        mapOf(
            "image/gif" to DrawablePainter(newLogoDrawable("GIF")),
            "image/png" to DrawablePainter(newLogoDrawable("PNG")),
            "image/jpeg" to DrawablePainter(newLogoDrawable("JPEG")),
            "image/webp" to DrawablePainter(newLogoDrawable("WEBP")),
            "image/bmp" to DrawablePainter(newLogoDrawable("BMP")),
            "image/svg+xml" to DrawablePainter(newLogoDrawable("SVG")),
            "image/heic" to DrawablePainter(newLogoDrawable("HEIC")),
            "image/heif" to DrawablePainter(newLogoDrawable("HEIF")),
        )
    }
    val progressPainter = rememberDrawableProgressPainter(MaskProgressDrawable())
    val progressIndicatorState = rememberProgressIndicatorState(progressPainter)
    val showDataFromLogo by context.prefsService.showDataFromLogo.stateFlow.collectAsState()
    val showMimeTypeLogo by context.prefsService.showMimeTypeLogoInLIst.stateFlow.collectAsState()
    val showProgressIndicator by context.prefsService.showProgressIndicatorInList.stateFlow.collectAsState()

    val modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clickable {
            onClick(photo, index)
        }
        .letIf(showDataFromLogo) {
            it.dataFromLogo(dataFromLogoState)
        }
        .letIf(showMimeTypeLogo) {
            it.mimeTypeLogo(mimeTypeLogoState, margin = 4.dp)
        }
        .letIf(showProgressIndicator) {
            it.progressIndicator(progressIndicatorState)
        }
    // listener 会导致两次创建的 DisplayRequest equals 为 false，从而引发重组，所以这里必须用 remember
    val request = remember(photo.listThumbnailUrl) {
        DisplayRequest(context, photo.listThumbnailUrl) {
            setApplySettings(LIST)
            if (animatedPlaceholder) {
                placeholder(drawable.ic_placeholder_eclipse_animated)
            } else {
                placeholder(
                    IconStateImage(
                        drawable.ic_image_outline,
                        ResColor(color.placeholder_bg)
                    )
                )
            }
            error(IconStateImage(drawable.ic_error, ResColor(color.placeholder_bg))) {
                saveCellularTrafficError(
                    IconStateImage(drawable.ic_signal_cellular, ResColor(color.placeholder_bg))
                )
            }
            crossfade()
            resizeApplyToDrawable()
            listener(
                onStart = { _ ->
                    dataFromLogoState.dataFrom = null
                    mimeTypeLogoState.mimeType = null
                    progressIndicatorState.progress = null
                },
                onSuccess = { _, result ->
                    dataFromLogoState.dataFrom = result.dataFrom
                    mimeTypeLogoState.mimeType = result.imageInfo.mimeType
                },
                onError = { _, _ ->
                    dataFromLogoState.dataFrom = null
                    mimeTypeLogoState.mimeType = null
                    progressIndicatorState.progress = null
                }
            )
            progressListener { _, totalLength: Long, completedLength: Long ->
                val progress = if (totalLength > 0) completedLength.toFloat() / totalLength else 0f
                Log.d("ProgressTest", "progressListener. setProgress. progress=$progress")
                progressIndicatorState.progress = progress
            }
        }
    }
    when (index % 3) {
        0 -> {
            com.github.panpf.sketch.compose.AsyncImage(
                request = request,
                modifier = modifier,
                contentScale = ContentScale.Crop,
                contentDescription = "photo",
            )
        }

        1 -> {
            com.github.panpf.sketch.compose.SubcomposeAsyncImage(
                request = request,
                modifier = modifier,
                contentScale = ContentScale.Crop,
                contentDescription = "photo",
            )
        }

        else -> {
            Image(
                painter = com.github.panpf.sketch.compose.rememberAsyncImagePainter(
                    request = request,
                ),
                modifier = modifier,
                contentScale = ContentScale.Crop,
                contentDescription = "photo"
            )
        }
    }
}