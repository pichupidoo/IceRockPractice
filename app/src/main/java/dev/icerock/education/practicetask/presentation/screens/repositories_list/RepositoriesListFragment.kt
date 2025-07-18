package dev.icerock.education.practicetask.presentation.screens.repositories_list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.icerock.education.practicetask.BuildConfig
import dev.icerock.education.practicetask.R
import dev.icerock.education.practicetask.data.entities.Repo
import dev.icerock.education.practicetask.databinding.FragmentRepositoriesListBinding
import dev.icerock.education.practicetask.error_types.ApiError
import dev.icerock.education.practicetask.presentation.screens.detail_info.DetailInfoFragment
import dev.icerock.education.practicetask.presentation.screens.repositories_list.RepositoriesListViewModel.State.*


private const val TAG = "RepositoriesListScreen"

@AndroidEntryPoint
class RepositoriesListFragment : Fragment() {

    private lateinit var navController: NavController

    private lateinit var binding: FragmentRepositoriesListBinding

    private lateinit var recyclerView: RecyclerView

    private val viewModel by viewModels<RepositoriesListViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRepositoriesListBinding.inflate(
            inflater,
            container,
            false,
        )

        if (viewModel.state.value !is Loaded) viewModel.fetchRepositories()

        binding.repositoriesListErrorButton.setOnClickListener {
            viewModel.fetchRepositories() // Retry | Refresh button listener
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->

            if (BuildConfig.DEBUG) if (state is Loaded) Log.i(
                TAG,
                "Repos list size: ${state.repos.size}"
            )

            binding.apply {
                bindRepositoriesList(state)
                bindErrorScreen(state)
            }

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // configuring action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.repositories_label)
            setDisplayHomeAsUpEnabled(false)
            show()
        }

        navController = Navigation.findNavController(view)

        recyclerView = binding.repositoriesListRecyclerView // setting up recycler view
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.log_out_button, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_action_logout) {
            viewModel.logOut()
            navController.navigate(R.id.action_repositoriesListFragment_to_authFragment)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onListItemClick(repo: Repo) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Tapped on ${repo.name} element")
        navController.navigate(
            R.id.action_repositoriesListFragment_to_detailFragment,
            DetailInfoFragment.createArguments(repoName = repo.name)
        )
    }

    private fun FragmentRepositoriesListBinding.bindRepositoriesList(
        state: RepositoriesListViewModel.State,
    ) {
        repositoriesListLoadingPb.visibility =
            if (state is Loading) View.VISIBLE else
                View.INVISIBLE

        repositoriesListRecyclerView.visibility = if (state is Loaded) View.VISIBLE else
            View.INVISIBLE

        if (state is Loaded) {
            val repositoriesListAdapter = RepositoriesListAdapter { position ->
                onListItemClick(state.repos[position])
            }
            // taking first 10 repositories
            repositoriesListAdapter.items = state.repos

            repositoriesListRecyclerView.adapter = repositoriesListAdapter
        } else {
            repositoriesListRecyclerView.adapter = null
        }
    }


    private fun FragmentRepositoriesListBinding.bindErrorScreen(
        state: RepositoriesListViewModel.State,
    ) {
        repositoriesListErrorButton.apply {
            setOnClickListener {
                viewModel.fetchRepositories()
            }
            text = if (state is Error) when (state.error) {
                is ApiError.NetworkError -> getString(R.string.retry_button_label)
                else -> getString(R.string.refresh_button_label)
            } else getString(R.string.refresh_button_label)
        }

        repositoriesListErrorScreen.visibility = if (state is Error || state is Empty
        ) View.VISIBLE else View.INVISIBLE


        repositoriesListErrorTitle.apply {
            text = when (state) {
                is Error -> when (state.error) {
                    is ApiError.Error -> getString(R.string.error)
                    is ApiError.NetworkError -> getString(R.string.connection_error)
                }
                is Empty -> getString(R.string.empty_message)
                else -> ""
            }
            setTextColor(
                when (state) {
                    is Error -> getColor(context, R.color.error)
                    else -> getColor(context, R.color.secondary)
                }
            )
        }
        repositoriesListErrorDescription.text = when (state) {
            is Error -> when (state.error) {
                is ApiError.NetworkError -> {
                    getString(R.string.check_your_internet_connection)
                }
                is ApiError.Error -> {
                    getString(
                        R.string.error_repositories,
                        state.error.message,
                    )
                }
            }
            is Empty -> getString(R.string.empty_repositories_list)
            else -> ""
        }
        repositoriesListErrorImage.setImageDrawable(when (state) {
            is Error -> context?.let { context ->
                when (state.error) {
                    is ApiError.NetworkError -> getDrawable(
                        context,
                        R.drawable.ic_no_connection
                    )
                    is ApiError.Error -> getDrawable(context, R.drawable.ic_error)
                }
            }
            else -> context?.let {
                getDrawable(
                    it,
                    R.drawable.ic_empty_repository_list
                )
            }
        })
    }
}
