package com.algolia.instantsearch.showcase.sortby

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.hits.connectHitsView
import com.algolia.instantsearch.helper.android.list.SearcherSingleIndexDataSource
import com.algolia.instantsearch.helper.android.sortby.SortByViewAutocomplete
import com.algolia.instantsearch.helper.android.sortby.connectPagedList
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.sortby.SortByConnector
import com.algolia.instantsearch.helper.sortby.SortByViewModel
import com.algolia.instantsearch.helper.sortby.connectView
import com.algolia.instantsearch.showcase.R
import com.algolia.instantsearch.showcase.client
import com.algolia.instantsearch.showcase.configureRecyclerView
import com.algolia.instantsearch.showcase.configureToolbar
import com.algolia.instantsearch.showcase.list.movie.Movie
import com.algolia.instantsearch.showcase.list.movie.MovieAdapter
import com.algolia.search.helper.deserialize
import com.algolia.search.model.IndexName
import kotlinx.android.synthetic.main.showcase_sort_by.*

class SortByShowcase : AppCompatActivity() {

    private val indexTitle = client.initIndex(IndexName("mobile_demo_movies"))
    private val indexYearAsc = client.initIndex(IndexName("mobile_demo_movies_year_asc"))
    private val indexYearDesc = client.initIndex(IndexName("mobile_demo_movies_year_desc"))
    private val searcher = SearcherSingleIndex(indexTitle)
    private val indexes = mapOf(
        0 to indexTitle,
        1 to indexYearAsc,
        2 to indexYearDesc
    )

    private val sortByViewModel = SortByViewModel(indexes, selected = 0)
    private val sortBy = SortByConnector(searcher, sortByViewModel)
    private val connection = ConnectionHandler(sortBy)

    private val dataSourceFactory =
        SearcherSingleIndexDataSource.Factory(searcher) { it.deserialize(Movie.serializer()) }
    private val pagedListConfig =
        PagedList.Config.Builder().setPageSize(10).setEnablePlaceholders(false).build()
    private val movies = LivePagedListBuilder(dataSourceFactory, pagedListConfig).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.showcase_sort_by)

        val adapter = ArrayAdapter<String>(this, R.layout.menu_item)
        val view = SortByViewAutocomplete(autoCompleteTextView, adapter)
        val adapterMovie = MovieAdapter()

        connection += sortBy.connectView(view) { index ->
            when (index) {
                indexTitle -> "Default"
                indexYearAsc -> "Year Asc"
                indexYearDesc -> "Year Desc"
                else -> index.indexName.raw
            }
        }
        connection += searcher.connectHitsView(adapterMovie) { response ->
            response.hits.deserialize(Movie.serializer())
        }

        connection += sortBy.connectPagedList(movies)

        configureToolbar(toolbar)
        configureRecyclerView(hits, adapterMovie)

        searcher.searchAsync()
    }

    override fun onDestroy() {
        super.onDestroy()
        searcher.cancel()
        connection.clear()
    }
}
