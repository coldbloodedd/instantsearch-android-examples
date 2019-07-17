package com.algolia.instantsearch.examples.querysuggestions;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class HighlightedTextView extends AppCompatTextView {
    public HighlightedTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(Html.fromHtml(String.valueOf(text)), type);
    }
}
