package com.algolia.custombackend.elasticbackend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ElasticSearchParameters {

    /** The query text */
    @NonNull
    public String query;

    public ElasticSearchParameters(@Nullable String query) {
        this.query = query != null ? query : "";
    }
}
