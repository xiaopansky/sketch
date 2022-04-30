/*
 * Copyright (C) 2019 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.sketch.sample.ui.viewer

import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.github.panpf.assemblyadapter.pager2.AssemblyFragmentStateAdapter
import com.github.panpf.sketch.sample.databinding.ImageViewerPagerFragmentBinding
import com.github.panpf.sketch.sample.model.ImageDetail
import com.github.panpf.sketch.sample.ui.base.BindingFragment
import com.github.panpf.tools4a.display.ktx.getStatusBarHeight
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ImageViewerPagerFragment : BindingFragment<ImageViewerPagerFragmentBinding>() {

    private val args by navArgs<ImageViewerPagerFragmentArgs>()

    override fun onViewCreated(
        binding: ImageViewerPagerFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        binding.imageViewerPagerPager.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin += requireContext().getStatusBarHeight()
                }
            }
            val imageList = Json.decodeFromString<List<ImageDetail>>(args.imageDetailJsonArray)
            adapter = AssemblyFragmentStateAdapter(
                this@ImageViewerPagerFragment,
                listOf(ImageViewerFragment.ItemFactory()),
                imageList
            )
            post {
                setCurrentItem(args.defaultPosition, false)
            }
        }

        binding.imageViewerPagerPageNumber.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin += requireContext().getStatusBarHeight()
                }
            }
            text = "%d/%d".format(
                args.defaultPosition + 1,
                binding.imageViewerPagerPager.adapter!!.itemCount
            )
            binding.imageViewerPagerPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    text = "%d/%d"
                        .format(position + 1, binding.imageViewerPagerPager.adapter!!.itemCount)
                }
            })
        }
    }
}
