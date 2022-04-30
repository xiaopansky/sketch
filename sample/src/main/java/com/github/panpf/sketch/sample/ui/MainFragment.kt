package com.github.panpf.sketch.sample.ui

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.assemblyadapter.recycler.AssemblyRecyclerAdapter
import com.github.panpf.sketch.sample.R
import com.github.panpf.sketch.sample.databinding.MainFragmentBinding
import com.github.panpf.sketch.sample.model.Link
import com.github.panpf.sketch.sample.model.ListSeparator
import com.github.panpf.sketch.sample.ui.base.ToolbarBindingFragment
import com.github.panpf.sketch.sample.ui.common.link.LinkItemFactory
import com.github.panpf.sketch.sample.ui.common.list.ListSeparatorItemFactory

class MainFragment : ToolbarBindingFragment<MainFragmentBinding>() {

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: MainFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        toolbar.menu.add(0, 0, 0, "Settings").apply {
            setIcon(R.drawable.ic_settings)
            setOnMenuItemClickListener {
                findNavController().navigate(MainFragmentDirections.actionSettingsFragment())
                true
            }
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        binding.mainRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = AssemblyRecyclerAdapter(
                listOf(LinkItemFactory(), ListSeparatorItemFactory()),
                listOf(
                    ListSeparator("Samples"),
                    Link(
                        "Pexels Photos",
                        MainFragmentDirections.actionPexelsPhotoListFragment()
                    ),
                    Link(
                        "Pexels Photos（Compose）",
                        MainFragmentDirections.actionPexelsPhotoListComposeFragment(),
                        Build.VERSION_CODES.LOLLIPOP
                    ),
                    Link("Giphy GIF", MainFragmentDirections.actionGiphyGifListFragment()),
                    Link(
                        "Giphy GIF（Compose）",
                        MainFragmentDirections.actionGiphyGifListComposeFragment(),
                        Build.VERSION_CODES.LOLLIPOP
                    ),
                    Link(
                        "Local Photos",
                        MainFragmentDirections.actionLocalPhotoListFragment()
                    ),
                    Link(
                        "Local Video",
                        MainFragmentDirections.actionLocalVideoListFragment()
                    ),
                    Link("Huge Image", MainFragmentDirections.actionHugeImageHomeFragment()),

                    ListSeparator("Test"),
                    Link(
                        "Image Format",
                        MainFragmentDirections.actionImageFormatTestFragment()
                    ),
                    Link("Fetcher", MainFragmentDirections.actionFetcherTestFragment()),
                    Link(
                        "Insanity Test",
                        MainFragmentDirections.actionInsanityTestFragment()
                    ),
                    Link(
                        "Insanity Test（Compose）",
                        MainFragmentDirections.actionInsanityTestComposeFragment(),
                        Build.VERSION_CODES.LOLLIPOP
                    ),
                    // todo 增加更多的示例
//                    Link(
//                        "ImageProcessor Test",
//                        MainFragmentDirections.actionImageProcessorTestFragment()
//                    ),
//                    Link(
//                        "ImageShaper Test",
//                        MainFragmentDirections.actionImageShaperTestFragment()
//                    ),
//                    Link(
//                        "Repeat Load Or Download Test",
//                        MainFragmentDirections.actionRepeatLoadOrDownloadTestFragment()
//                    ),
//                    Link("inBitmap Test", MainFragmentDirections.actionInBitmapTestFragment()),
//                    Link(
//                        "Image Orientation Test",
//                        MainFragmentDirections.actionImageOrientationTestHomeFragment()
//                    ),
//                    Link("Other Test", MainFragmentDirections.actionOtherTestFragment()),
                    Link("Other Test", MainFragmentDirections.actionTestFragment()),
//
//                    ListSeparator("App"),
//                    Link("Settings", MainFragmentDirections.actionSettingsFragment()),
//                    Link("About", MainFragmentDirections.actionAboutFragment()),
                )
            )
        }
    }
}