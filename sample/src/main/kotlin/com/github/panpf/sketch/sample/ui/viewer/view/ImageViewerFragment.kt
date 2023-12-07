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
package com.github.panpf.sketch.sample.ui.viewer.view

import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.sample.databinding.ImageViewerFragmentBinding
import com.github.panpf.sketch.sample.eventService
import com.github.panpf.sketch.sample.model.ImageDetail
import com.github.panpf.sketch.sample.prefsService
import com.github.panpf.sketch.sample.ui.base.BindingFragment
import com.github.panpf.sketch.sample.ui.setting.ImageInfoDialogFragment
import com.github.panpf.sketch.sample.util.lifecycleOwner
import com.github.panpf.sketch.sample.util.repeatCollectWithLifecycle
import com.github.panpf.sketch.stateimage.ThumbnailMemoryCacheStateImage
import com.github.panpf.sketch.viewability.showSectorProgressIndicator
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.valueOf
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ImageViewerFragment : BindingFragment<ImageViewerFragmentBinding>() {

    private val args by navArgs<ImageViewerFragmentArgs>()

    override fun onViewCreated(binding: ImageViewerFragmentBinding, savedInstanceState: Bundle?) {
        binding.root.background = null

        binding.imageViewerZoomImage.apply {
            lifecycleOwner.lifecycleScope.launch {
                prefsService.scrollBarEnabled.stateFlow.collect {
                    scrollBar = if (it) ScrollBarSpec.Default else null
                }
            }
            lifecycleOwner.lifecycleScope.launch {
                prefsService.readModeEnabled.stateFlow.collect {
                    zoomable.readModeState.value = if (it) ReadMode.Default else null
                }
            }
            lifecycleOwner.lifecycleScope.launch {
                prefsService.showTileBounds.stateFlow.collect {
                    subsampling.showTileBoundsState.value = it
                }
            }
            lifecycleOwner.lifecycleScope.launch {
                prefsService.contentScale.stateFlow.collect {
                    zoomable.contentScaleState.value = ContentScaleCompat.valueOf(it)
                }
            }
            lifecycleOwner.lifecycleScope.launch {
                prefsService.alignment.stateFlow.collect {
                    zoomable.alignmentState.value = AlignmentCompat.valueOf(it)
                }
            }

            showSectorProgressIndicator()

            setOnClickListener {
                findNavController().popBackStack()
            }
            setOnLongClickListener {
                startImageInfoDialog(this)
                true
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(State.STARTED) {
                    prefsService.showOriginImage.stateFlow.collect {
                        displayImage(binding)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                prefsService.viewersMergedFlow.collect {
                    displayImage(binding)
                }
            }
        }

        binding.imageViewerRetryButton.setOnClickListener {
            displayImage(binding)
        }
        eventService.viewerPagerRotateEvent
            .repeatCollectWithLifecycle(viewLifecycleOwner, State.STARTED) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val zoomable = binding.imageViewerZoomImage.zoomable
                    zoomable.rotate(zoomable.transformState.value.rotation.roundToInt() + 90)
                }
            }
        eventService.viewerPagerInfoEvent
            .repeatCollectWithLifecycle(viewLifecycleOwner, State.STARTED) {
                startImageInfoDialog(binding.imageViewerZoomImage)
            }
    }

    private fun displayImage(binding: ImageViewerFragmentBinding) {
        val showOriginImage: Boolean = prefsService.showOriginImage.stateFlow.value
        val uri = if (showOriginImage) args.originImageUri else args.previewImageUri
        binding.imageViewerZoomImage.displayImage(uri) {
            merge(prefsService.buildViewerImageOptions())
            placeholder(ThumbnailMemoryCacheStateImage(uri = args.thumbnailImageUrl))
            crossfade(fadeStart = false)
            listener(
                onStart = {
                    binding.imageViewerErrorLayout.isVisible = false
                },
                onError = { _, _ ->
                    binding.imageViewerErrorLayout.isVisible = true
                }
            )
        }
    }

    private fun startImageInfoDialog(imageView: ImageView) {
        val arguments1 =
            ImageInfoDialogFragment.createDirectionsFromImageView(imageView, null).arguments
        childFragmentManager.beginTransaction()
            .add(ImageInfoDialogFragment().apply {
                arguments = arguments1
            }, null)
            .commit()
    }

    class ItemFactory : FragmentItemFactory<ImageDetail>(ImageDetail::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: ImageDetail
        ): Fragment = ImageViewerFragment().apply {
            arguments = ImageViewerFragmentArgs(
                position = data.position,
                originImageUri = data.originUrl,
                previewImageUri = data.mediumUrl,
                thumbnailImageUrl = data.thumbnailUrl,
            ).toBundle()
        }
    }
}