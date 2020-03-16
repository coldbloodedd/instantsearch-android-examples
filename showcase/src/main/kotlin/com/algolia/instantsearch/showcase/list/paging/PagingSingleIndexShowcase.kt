package com.algolia.instantsearch.showcase.list.paging

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.searchbox.SearchBoxView
import com.algolia.instantsearch.showcase.*
import com.algolia.instantsearch.showcase.list.movie.Movie
import com.algolia.instantsearch.showcase.list.movie.MovieAdapterPaged
import com.algolia.instantsearch.helper.android.list.SearcherSingleIndexDataSource
import com.algolia.instantsearch.helper.searcher.SearcherScope
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.showcase.list.movie.SearchArticle
import com.algolia.instantsearch.showcase.list.movie.SearchArticlePaged
import kotlinx.android.synthetic.main.showcase_paging.*
import kotlinx.android.synthetic.main.include_search.*
import com.algolia.instantsearch.core.searcher.Searcher
import com.algolia.instantsearch.core.searcher.Sequencer
import com.algolia.instantsearch.core.subscription.SubscriptionValue
import com.algolia.search.client.Index
import com.algolia.search.model.filter.FilterGroup
import com.algolia.search.model.response.ResponseSearch
import com.algolia.search.model.search.Query
import com.algolia.search.transport.RequestOptions
import kotlinx.coroutines.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.algolia.instantsearch.core.connection.Connection
import com.algolia.instantsearch.core.connection.ConnectionImpl
import com.algolia.instantsearch.core.searchbox.SearchBoxViewModel
import com.algolia.instantsearch.core.searchbox.connectView
import com.algolia.instantsearch.core.searcher.Debouncer
import com.algolia.instantsearch.core.searcher.debounceSearchInMillis
import com.algolia.instantsearch.helper.android.searchbox.*
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import com.algolia.instantsearch.helper.android.searchbox.connectSearcher
import com.algolia.instantsearch.helper.android.searchbox.connectView
import com.algolia.instantsearch.helper.searchbox.SearchMode
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class PagingSingleIndexShowcase : AppCompatActivity() {

    private val searcher = SearcherSingleIndex2(stubIndex2)
    private val dataSourceFactory = SearcherSingleIndexDataSource2.Factory(searcher) {

        it.deserialize(
        SearchArticle.serializer()) }

    private val pagedListConfig = PagedList.Config.Builder().setPageSize(10).setEnablePlaceholders(false).build()
    private val movies = LivePagedListBuilder(dataSourceFactory, pagedListConfig).build()
    private val searchBox = SearchBoxConnectorPagedList2(searcher, listOf(movies), debouncer = Debouncer(0))
    private val connection = ConnectionHandler(searchBox)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.showcase_paging)

        val adapter = SearchArticlePaged()
        val searchBoxView = SearchBoxViewAppCompat(searchView)

        connection += searchBox.connectView(searchBoxView)

        movies.observe(this, Observer { hits -> adapter.submitList(hits) })

        configureToolbar(toolbar)
        //configureSearcher2(searcher)
        configureSearchView(searchView, getString(R.string.search_movies))
        configureRecyclerView(list, adapter)
        //onResponseChangedThenUpdateNbHits(searcher, nbHits, connection)
    }

    override fun onDestroy() {
        super.onDestroy()
        searcher.cancel()
        connection.clear()
    }
}

public class SearchBoxViewAppCompat2(
    public val searchView: SearchView
) : SearchBoxView {

    override var onQueryChanged: Callback<String?>? = null
    override var onQuerySubmitted: Callback<String?>? = null

    init {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                onQuerySubmitted?.invoke(query)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                Log.d("QueryTextChange", query)
                onQueryChanged?.invoke(query)
                return false
            }
        })
    }

    override fun setText(text: String?, submitQuery: Boolean) {
        searchView.setQuery(text, submitQuery)
    }
}

public typealias Callback<T> = ((T) -> Unit)




public class SearcherSingleIndex2(
    public var index: Index,
    public val query: Query = Query(),
    public val requestOptions: RequestOptions? = null,
    public val isDisjunctiveFacetingEnabled: Boolean = true,
    override val coroutineScope: CoroutineScope = SearcherScope()
) : Searcher<ResponseSearch> {

    internal val sequencer = Sequencer()

    override val isLoading = SubscriptionValue(false)
    override val error = SubscriptionValue<Throwable?>(null)
    override val response = SubscriptionValue<ResponseSearch?>(null)

    private val options = RequestOptions()
    private val exceptionHandler = SearcherExceptionHandler(this)

    internal var filterGroups: Set<FilterGroup<*>> = setOf()

    override fun setQuery(text: String?) {
        this.query.query = text
    }

    override fun searchAsync(): Job {

        return coroutineScope.launch(exceptionHandler) {
            isLoading.value = true
            response.value = withContext(Dispatchers.Default) { search() }
            isLoading.value = false
        }.also {
            sequencer.addOperation(it)
        }
    }

    override suspend fun search(): ResponseSearch {
        Log.d("BOOMOO3", "search " + query.query)
        return if (isDisjunctiveFacetingEnabled) {
            index.advancedSearch(query, filterGroups, options)
        } else {
            index.search(query, options)
        }
    }

    override fun cancel() {
        sequencer.cancelAll()
    }
}

internal class SearcherExceptionHandler<R>(
    private val searcher: Searcher<R>
) : AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        searcher.error.value = exception
        searcher.isLoading.value = false
    }
}



public class SearcherSingleIndexDataSource2<T>(
    private val searcher: SearcherSingleIndex2,
    private val transformer: (ResponseSearch.Hit) -> T
) : PageKeyedDataSource<Int, T>() {

    public class Factory<T>(
        private val searcher: SearcherSingleIndex2,
        private val transformer: (ResponseSearch.Hit) -> T
    ) : DataSource.Factory<Int, T>() {

        override fun create(): DataSource<Int, T> {
            return SearcherSingleIndexDataSource2(searcher, transformer)
        }
    }

    private var initialLoadSize: Int = 30

//    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, T>) {
//        initialLoadSize = params.requestedLoadSize
//        searcher.query.hitsPerPage = initialLoadSize
//        searcher.query.page = 0
//
//        Log.d("BOOMOO1", "loadinitial for " + searcher.query.query)
//
//        searcher.searchAsync()
//        searcher.response.subscribe {
//            val response = it
//            if (response != null) {
//                Log.d("BOOMOO2", "search inside loadinitial " + searcher.query.query)
//                val nextKey = if (response.nbHits > initialLoadSize) 1 else null
//
//                callback.onResult(response.hits.map(transformer), 0, response.nbHits, null, nextKey)
//            }
//        }
//
//
//    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, T>) {
        initialLoadSize = params.requestedLoadSize
        searcher.query.hitsPerPage = initialLoadSize
        searcher.query.page = 0
        searcher.isLoading.value = true
        val queryLoaded = searcher.query.query
        Log.d("BOOMOO1", "loadinitial for " + searcher.query.query)
        runBlocking {
            try {
                val response = searcher.search()
                
//                if (queryLoaded != searcher.query.query) {
//                    invalidate()
//                    Log.d("BOOMOO5", "is query different " + searcher.query.query)
//                }
                Log.d("BOOMOO2", "search inside loadinitial " + searcher.query.query)
                val nextKey = if (response.nbHits > initialLoadSize) 1 else null

                withContext(searcher.coroutineScope.coroutineContext) {
                    searcher.response.value = response
                    searcher.isLoading.value = false
                }
                callback.onResult(response.hits.map(transformer), 0, response.nbHits, null, nextKey)
            } catch (throwable: Throwable) {
                withContext(searcher.coroutineScope.coroutineContext) {
                    searcher.error.value = throwable
                    searcher.isLoading.value = false
                }
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, T>) {
        val initialOffset = (initialLoadSize / params.requestedLoadSize) - 1
        val page = params.key + initialOffset

        searcher.query.page = page
        searcher.query.hitsPerPage = params.requestedLoadSize
        searcher.isLoading.value = true
        runBlocking {
            try {
                val response = searcher.search()
                val nextKey = if (page + 1 < response.nbPages) params.key + 1 else null

                withContext(searcher.coroutineScope.coroutineContext) {
                    searcher.response.value = response
                    searcher.isLoading.value = false
                }
                callback.onResult(response.hits.map(transformer), nextKey)
            } catch (throwable: Throwable) {
                withContext(searcher.coroutineScope.coroutineContext) {
                    searcher.error.value = throwable
                    searcher.isLoading.value = false
                }
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, T>) = Unit
}
public data class SearchBoxConnectorPagedList2<R>(
    public val searcher: Searcher<R>,
    public val pagedList: List<LiveData<out PagedList<out Any>>>,
    public val viewModel: SearchBoxViewModel = SearchBoxViewModel(),
    public val searchMode: SearchMode = SearchMode.AsYouType,
    public val debouncer: Debouncer = Debouncer(debounceSearchInMillis)
) : ConnectionImpl() {

    private val connectionSearcher = viewModel.connectSearcher2(searcher, pagedList, searchMode, debouncer)

    override fun connect() {
        super.connect()
        connectionSearcher.connect()
    }

    override fun disconnect() {
        super.disconnect()
        connectionSearcher.disconnect()
    }
}

public fun <R> SearchBoxViewModel.connectSearcher2(
    searcher: Searcher<R>,
    pagedList: List<LiveData<out PagedList<out Any>>>,
    searchAsYouType: SearchMode = SearchMode.AsYouType,
    debouncer: Debouncer = Debouncer(debounceSearchInMillis)
): Connection {
    return SearchBoxConnectionSearcherPagedList2(this, searcher, pagedList, searchAsYouType, debouncer)
}

public fun <R> SearchBoxConnectorPagedList2<R>.connectView(
    view: SearchBoxView
): Connection {
    return viewModel.connectView(view)
}

internal data class SearchBoxConnectionSearcherPagedList2<R>(
    private val viewModel: SearchBoxViewModel,
    private val searcher: Searcher<R>,
    private val pagedList: List<LiveData<out PagedList<out Any>>>,
    private val searchMode: SearchMode,
    private val debouncer: Debouncer
) : ConnectionImpl() {

    private val searchAsYouType: Callback<String?> = { query ->

        searcher.setQuery(query)

        debouncer.debounce(searcher) {
            pagedList.forEach {
                if (it.value?.dataSource == null) {
                    Log.d("ddddd", "null")
                }
                Log.d("ddddd", it.value?.dataSource.toString())
                Log.d("BOOMOO4", "invalidating data " + query)

                it.value?.dataSource?.invalidate()
            }
        }

    }
    private val searchOnSubmit: Callback<String?> = { query ->
        searcher.setQuery(query)
        pagedList.forEach {
            it.value?.dataSource?.invalidate()
        }
    }

    override fun connect() {
        super.connect()
        when (searchMode) {
            SearchMode.AsYouType -> viewModel.query.subscribe(searchAsYouType)
            SearchMode.OnSubmit -> viewModel.eventSubmit.subscribe(searchOnSubmit)
        }
    }

    override fun disconnect() {
        super.disconnect()
        when (searchMode) {
            SearchMode.AsYouType -> viewModel.query.unsubscribe(searchAsYouType)
            SearchMode.OnSubmit -> viewModel.eventSubmit.unsubscribe(searchOnSubmit)
        }
    }
}