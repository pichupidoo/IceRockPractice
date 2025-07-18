package dev.icerock.education.practicetask.presentation.screens.detail_info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.m2mobi.markymark.MarkyMark
import com.m2mobi.markymarkandroid.MarkyMarkAndroid
import com.m2mobi.markymarkcontentful.ContentfulFlavor
import dagger.hilt.android.AndroidEntryPoint
import dev.icerock.education.practicetask.BuildConfig
import dev.icerock.education.practicetask.R
import dev.icerock.education.practicetask.databinding.FragmentDetailInfoBinding
import dev.icerock.education.practicetask.error_types.ApiError
import dev.icerock.education.practicetask.presentation.screens.detail_info.DetailInfoViewModel.ReadmeState
import dev.icerock.education.practicetask.presentation.screens.detail_info.DetailInfoViewModel.State.Loaded
import dev.icerock.education.practicetask.presentation.screens.detail_info.DetailInfoViewModel.State.Loading
import dev.icerock.education.practicetask.utils.PicassoImageLoader

private const val TAG = "DetailInfo"

@AndroidEntryPoint
class DetailInfoFragment : Fragment() {

    private lateinit var navController: NavController

    private lateinit var binding: FragmentDetailInfoBinding

    private val viewModel by viewModels<DetailInfoViewModel>()

    private val repoName: String
        get() = requireNotNull(requireArguments().getString(REPOSITORY_NAME_KEY))


    private lateinit var markDownRenderer: MarkyMark<View>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDetailInfoBinding.inflate(
            inflater,
            container,
            false
        )

        if (viewModel.state.value !is Loaded) viewModel.fetchRepository(repoName)

        binding.detailInfoErrorButton.setOnClickListener {
            //If details are loaded but readme not
            if (viewModel.state.value is Loaded) {
                (viewModel.state.value as Loaded).githubRepo.let { repo ->
                    viewModel.fetchReadme(
                        repo.name,
                        repo.defaultBranch,
                        repo.owner.login
                    )
                }
            } else {
                viewModel.fetchRepository(repoName)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (BuildConfig.DEBUG) Log.i(TAG, state.toString())
            binding.apply {
                bindDetailInfo(state = state)

                bindErrorScreen(
                    visibility = state is DetailInfoViewModel.State.Error,
                    error = if (state is DetailInfoViewModel.State.Error) state.error else null,
                )

            }
        }

        viewModel.readmeState.observe(viewLifecycleOwner) { readmeState ->
            if (BuildConfig.DEBUG) Log.i(TAG, readmeState.toString())
            binding.apply {
                bindReadme(state = readmeState)

                bindErrorScreen(
                    visibility = readmeState is ReadmeState.Error,
                    error = if (readmeState is ReadmeState.Error) readmeState.error else null,
                )
            }

        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.log_out_button, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_action_logout -> {
                viewModel.logOut()
                navController.navigate(R.id.action_detailFragment_to_authFragment)
                true
            }
            android.R.id.home -> {
                navController.navigate(R.id.action_detailFragment_to_repositoriesListFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = repoName
            setHomeAsUpIndicator(R.drawable.ic_arrow_left)
            setDisplayHomeAsUpEnabled(true)
            show()
        }
        markDownRenderer = MarkyMarkAndroid.getMarkyMark(
            requireActivity(), ContentfulFlavor(),
            PicassoImageLoader(requireContext())
        )
    }

    private fun FragmentDetailInfoBinding.bindReadme(
        state: ReadmeState,
    ) {
        readmeText.visibility =
            if (state !is ReadmeState.Loading) View.VISIBLE else View.INVISIBLE

        noReadmeLabel.visibility =
            if (state is ReadmeState.Empty) View.VISIBLE else View.INVISIBLE

        if (state is ReadmeState.Loaded) {
            readmeText.removeAllViews()
            val paragraphs = markDownRenderer.parseMarkdown(state.markdownToString())
            for (paragraph in paragraphs) {
                if (paragraph is TextView) Log.i(TAG, "Paragraph: ${paragraph.text}")
                readmeText.addView(paragraph)
            }
        }
        readmeLoadingPb.visibility = if (state is ReadmeState.Loading) View.VISIBLE else
            View.INVISIBLE
    }

    private fun FragmentDetailInfoBinding.bindDetailInfo(
        state: DetailInfoViewModel.State,
    ) {
        repositoryDetailInfoBlock.visibility =
            if (state is Loaded) View.VISIBLE else View
                .INVISIBLE
        forksCount.text =
            if (state is Loaded) state.githubRepo.forksCount.toString() else ""
        starsCount.text =
            if (state is Loaded) state.githubRepo.starsCount.toString() else ""
        watchersCount.text =
            if (state is Loaded) state.githubRepo.watchersCount.toString() else ""
        license.text =
            if (state is Loaded)
                state.githubRepo.license?.name ?: "No license"
            else ""
        link.text =
            if (state is Loaded) state.githubRepo.url.substringAfter("//") else ""

        link.setOnClickListener {
            if (state is Loaded) openUrl(state.githubRepo.url)
        }

        detailInfoLoadingPb.visibility = if (state is Loading) View.VISIBLE else View
            .INVISIBLE
    }

    private fun FragmentDetailInfoBinding.bindErrorScreen(
        visibility: Boolean = false,
        error: ApiError?,
    ) {
        detailInfoErrorButton.apply {
            setOnClickListener {
                viewModel.fetchRepository(repoName)
            }
            text = when (error) {
                is ApiError.NetworkError -> getString(R.string.retry_button_label)
                else -> getString(R.string.refresh_button_label)
            }
        }

        detailInfoErrorScreen.visibility = if (visibility)
            View.VISIBLE
        else
            View.INVISIBLE


        detailInfoErrorTitle.apply {
            text = when (error) {
                is ApiError.NetworkError -> getString(R.string.connection_error)
                else -> getString(R.string.error)
            }
            setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
        }

        detailInfoErrorDescription.text = when (error) {
            is ApiError.NetworkError -> {
                getString(R.string.check_your_internet_connection)
            }
            else -> {
                getString(
                    R.string.error_repositories,
                    if (error is ApiError.Error) error.message else "",
                )
            }
        }

        detailInfoErrorImage.setImageDrawable(
            context?.let {
                when (error) {
                    is ApiError.NetworkError -> ContextCompat.getDrawable(
                        it,
                        R.drawable.ic_no_connection
                    )
                    is ApiError.Error -> ContextCompat.getDrawable(it, R.drawable.ic_error)
                    else -> ContextCompat.getDrawable(it, R.drawable.ic_error)
                }
            }
        )
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    companion object {
        private const val REPOSITORY_NAME_KEY = "repoNameKey"

        fun createArguments(repoName: String): Bundle {
            return bundleOf(REPOSITORY_NAME_KEY to repoName)
        }
    }
}