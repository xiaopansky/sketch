package com.github.panpf.sketch.request

import androidx.compose.runtime.Composable
import com.github.panpf.sketch.state.ComposableErrorStateImage
import com.github.panpf.sketch.state.ErrorStateImage
import com.github.panpf.sketch.state.rememberPainterStateImage
import org.jetbrains.compose.resources.DrawableResource

/**
 * Set Drawable placeholder image when loading
 */
@Composable
fun ImageOptions.Builder.placeholder(resource: DrawableResource): ImageOptions.Builder =
    placeholder(rememberPainterStateImage(resource))

/**
 * Set Drawable placeholder image when uri is invalid
 */
@Composable
fun ImageOptions.Builder.fallback(resource: DrawableResource): ImageOptions.Builder =
    fallback(rememberPainterStateImage(resource))

/**
 * Set Color image to display when loading fails.
 *
 * You can also set image of different error types via the trailing lambda function
 */
@Composable
fun ImageOptions.Builder.error(
    defaultResource: DrawableResource,
): ImageOptions.Builder = error(ComposableErrorStateImage(defaultResource))

/**
 * Set Color image to display when loading fails.
 *
 * You can also set image of different error types via the trailing lambda function
 *
 * [configBlock] must be inline so that the status used internally will be correctly monitored and updated.
 */
@Composable
inline fun ImageOptions.Builder.composableError(
    defaultResource: DrawableResource,
    crossinline configBlock: @Composable (ErrorStateImage.Builder.() -> Unit)
): ImageOptions.Builder = error(ComposableErrorStateImage(defaultResource, configBlock))