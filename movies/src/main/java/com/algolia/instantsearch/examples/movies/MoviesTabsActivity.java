package com.algolia.instantsearch.examples.movies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.viewpager.widget.ViewPager;

import com.algolia.instantsearch.core.helpers.Searcher;
import com.algolia.instantsearch.ui.helpers.InstantSearch;
import com.algolia.instantsearch.ui.views.Hits;
import com.google.android.material.tabs.TabLayout;

public class MoviesTabsActivity extends MoviesActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        initWithLayout();

        // Set up the ViewPager with an adapter that will return a fragment
        // for each of the two primary sections of the activity.
        ViewPager viewPager = findViewById(R.id.container);
        viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.tabs);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

    }

    private void linkFragmentToSearcher(LayoutFragment layoutFragment) {
        Searcher searcher = (layoutFragment instanceof MoviesFragment) ? searcherMovies : searcherActors;

        // link the Searcher to the Fragment's UI
        new InstantSearch(this, searcher, layoutFragment).registerSearchView(this, searchBoxViewModel);
        // Show results for empty query (on app launch) / voice query (from intent)
        searcher.search(getIntent());
        // If the activity was started with an intent, apply any query it contains
        setQueryFromIntent(getIntent());
    }

    public static abstract class LayoutFragment extends Fragment {
        private final int layout;

        public LayoutFragment(@LayoutRes int layout) {
            this.layout = layout;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(layout, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((MoviesTabsActivity) getActivity()).linkFragmentToSearcher(this);
        }
    }

    public static class MoviesFragment extends LayoutFragment {
        public MoviesFragment() {
            super(R.layout.fragment_movies);
        }
    }

    public static class ActorsFragment extends LayoutFragment {
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getActivity().<Hits>findViewById(R.id.hits_movies)
                    .addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        }

        public ActorsFragment() {
            super(R.layout.fragment_actors);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return new MoviesFragment();
                default:
                    return new ActorsFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}