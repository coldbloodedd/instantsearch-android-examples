package com.algolia.instantsearch.showcase.list.movie

import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.helper.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.algolia.search.model.ObjectID
import com.algolia.search.model.indexing.Indexable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonObject


@Serializable
data class Movie(
    val title: String,
    val year: String,
    val genre: List<String>,
    val image: String,
    override val objectID: ObjectID,
    override val _highlightResult: JsonObject?
) : Indexable, Highlightable {

    @Transient
    public val highlightedTitle
        get() = getHighlight(Attribute("title"))

    @Transient
    public val highlightedGenres
        get() = getHighlights(Attribute("genre"))

    @Transient
    public val highlightedActors
        get() = getHighlights(Attribute("actors"))
}

@Serializable
data class SearchArticle(
    val categories: List<String>?,
    val chapeau: String?,
    val dateMaj: Int?,
    val datePublication: Long?,
    val pagesVues: Int?,
    val partages: Int?,
    val payant: Boolean?,
    val photos: List<Photo>?,
    val source: String?,
    val texte: String?,
    val titre: String?,
    val url: String?,
    val zonesGeo: List<String>?,
    override val objectID: ObjectID,
    override val _highlightResult: JsonObject?
) : Highlightable, Indexable {
    @Transient
    public val highlightedTitle: HighlightedString?
        get() = getHighlight(Attribute("titre"))
    @Transient
    public val highlightedHeader: HighlightedString?
        get() = getHighlight(Attribute("chapeau"))
}
@Serializable
data class Photo(
    val credits: String?,
    val id: String?,
    val legende: String?
)