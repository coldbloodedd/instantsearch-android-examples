package com.algolia.instantsearch.showcase.list.movie

import androidx.recyclerview.widget.DiffUtil


object SearchArticleDiffUtil : DiffUtil.ItemCallback<SearchArticle>() {

    override fun areItemsTheSame(
        oldItem: SearchArticle,
        newItem: SearchArticle
    ): Boolean {
        return oldItem.objectID == newItem.objectID
    }

    override fun areContentsTheSame(
        oldItem: SearchArticle,
        newItem: SearchArticle
    ): Boolean {
        return oldItem == newItem
    }
}

object MovieDiffUtil : DiffUtil.ItemCallback<Movie>() {

    override fun areItemsTheSame(
        oldItem: Movie,
        newItem: Movie
    ): Boolean {
        return oldItem.objectID == newItem.objectID
    }

    override fun areContentsTheSame(
        oldItem: Movie,
        newItem: Movie
    ): Boolean {
        return oldItem == newItem
    }
}