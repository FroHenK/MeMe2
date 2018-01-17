package org.whysosirius.meme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.whysosirius.meme.database.Meme;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final static String APP_PREFERENCES = "prefs";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    final static String host = "";
    private ViewPager mViewPager;
    private FirstMemeAdapter firstMemeAdapter;
    private SecondMemeAdapter secondMemeAdapter;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        ArrayList<Meme> memes = new ArrayList<>();
        ArrayList<Meme> memes2 = new ArrayList<>();
        memes2.add(new Meme("http://azatismagilov00.siteme.org/kek/HvCNOep1oRLhKT5ubjR41.jpg").setTitle("kek"));


        firstMemeAdapter = new FirstMemeAdapter(this.getApplicationContext(), "https://memkekkekmem.herokuapp.com/get_new_list");
        secondMemeAdapter = new SecondMemeAdapter(this.getApplicationContext(), memes2);

        firstMemeAdapter.setSecondMemeAdapter(secondMemeAdapter);


        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1337);
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337) {
            Log.i("Login", "Success");
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // ...
            } else {
                // Sign in failed, check response for error code
                // ...
            }
        }
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private RecyclerView.Adapter adapter;

        public PlaceholderFragment() {
        }

        public void setAdapter(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        public static PlaceholderFragment newInstance(int sectionNumber, RecyclerView.Adapter adapter) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setAdapter(adapter);

            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            RecyclerView recyclerView = rootView.findViewById(R.id.main_recycler_view);
            ((RecyclerViewContainer) adapter).setRecyclerView(recyclerView);
            recyclerView.setHasFixedSize(true);//if memes become expandable delete this

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);


            recyclerView.setAdapter(adapter);

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            RecyclerView.Adapter adapter = null;

            if (position == 0)
                return PlaceholderFragment.newInstance(position + 1, firstMemeAdapter);
            else
                return PlaceholderFragment.newInstance(position + 1, secondMemeAdapter);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}
