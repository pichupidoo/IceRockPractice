package dev.icerock.education.practicetask.presentation.screens.repositories_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.icerock.education.practicetask.data.entities.Repo
import dev.icerock.education.practicetask.databinding.RepositoriesListItemBinding


class RepositoriesListAdapter(private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<RepositoriesListAdapter.ViewHolder>() {

    var items: List<Repo> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val itemViewBinding = RepositoriesListItemBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false,
        )
        return ViewHolder(itemViewBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val repoItem = items[position]
        viewHolder.bind(repoItem)
        viewHolder.itemView.setOnClickListener { onItemClick(position) }
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val itemViewBinding: RepositoriesListItemBinding) : RecyclerView
    .ViewHolder(itemViewBinding.root) {
        fun bind(repo: Repo) {
            itemViewBinding.apply {
                repositoriesListItemTitle.text = repo.name

                repositoriesListItemDescription.visibility =
                    if (repo.description.isNullOrEmpty()) View.GONE else View.VISIBLE

                repositoriesListItemDescription.text = repo.description
                repositoriesListItemLanguage.text = repo.language
                repositoriesListItemLanguage.setTextColor(repo.color)
            }
        }
    }
}