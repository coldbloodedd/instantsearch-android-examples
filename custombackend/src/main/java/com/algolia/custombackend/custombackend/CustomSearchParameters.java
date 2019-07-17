package com.algolia.custombackend.custombackend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomSearchParameters {

    /** The query text */
    @NonNull
    public String query;

    public CustomSearchParameters(@Nullable String query) {
        this.query = query != null ? query : "";
    }
}
