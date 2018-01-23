package org.whysosirius.meme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final int RC_GET_MEME_IMAGE = 420;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    final static String host = "";
    private ViewPager mViewPager;
    private FirstMemeAdapter firstMemeAdapter;
    private SecondMemeAdapter secondMemeAdapter;
    private ThirdMemeAdapter thirdMemeAdapter;
    private SharedPreferences preferences;


    @Override
    protected void onStop() {
        //firstMemeAdapter.memeFetcher.cancel(true);
        //secondMemeAdapter.memeFetcher.cancel(true);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //firstMemeAdapter.memeFetcher.cancel(true);
        //secondMemeAdapter.memeFetcher.cancel(true);
        super.onDestroy();
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    //persmission method.
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission

        int writePermission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
        int readPermission = ActivityCompat.checkSelfPermission(activity, "android.permission.READ_EXTERNAL_STORAGE");

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void refresh() {
        firstMemeAdapter.refresh();
        secondMemeAdapter.refresh();
        thirdMemeAdapter.refresh();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(getString(R.string.siriusmeme_preferences_key), MODE_PRIVATE);

        firstMemeAdapter = new FirstMemeAdapter(this.getApplicationContext(), getString(R.string.get_new_list_url));
        secondMemeAdapter = new SecondMemeAdapter(this.getApplicationContext(), getString(R.string.get_old_list_url));
        thirdMemeAdapter = new ThirdMemeAdapter(this.getApplicationContext(), getString(R.string.get_rated_list_url));


        thirdMemeAdapter.setSecondMemeAdapter(secondMemeAdapter);
        firstMemeAdapter.setSecondMemeAdapter(secondMemeAdapter);

        setContentView(R.layout.activity_main);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount() - 1);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView textView = navigationView.getHeaderView(0).findViewById(R.id.username_menu);
        textView.setText(preferences.getString("username", ""));

        CompoundButton mySwitch = navigationView.getMenu().getItem(2).getActionView().findViewById(R.id.amoral_switch);
        if (Boolean.valueOf(preferences.getBoolean("is_amoral", false)).toString().equals(Boolean.valueOf(("k" + "e" + "k").equals("kek")).toString())) {
            mySwitch.setChecked(true);
        }
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit().putBoolean("is_amoral", b).apply();
                refresh();
            }
        });

        verifyStoragePermissions(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GET_MEME_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_upload_meme, null);
            builder.setView(view);
            AlertDialog alertDialog = builder.create();
            ImageView memeImageView = view.findViewById(R.id.upload_meme_image_view);
            memeImageView.setImageURI(selectedImage);
            CheckBox checkBox = view.findViewById(R.id.memeUploadCheckBox);
            EditText titleEditText = view.findViewById(R.id.upload_meme_title_edit_text);
            FloatingActionButton sendButton = view.findViewById(R.id.sendFloatingActionButton);
            ImageButton cancelButton = view.findViewById(R.id.cancelImageButton);
            cancelButton.setOnClickListener(v -> alertDialog.cancel());

            sendButton.setOnClickListener(v -> {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    uploadBitmap(bitmap, titleEditText.getText().toString(), checkBox.isChecked());
                    alertDialog.dismiss();
                } catch (IOException e) {
                    Log.e("siriusmeme", "omg no", e);
                }
            });

            alertDialog.show();
        }
    }

    public static class PlaceholderFragment extends Fragment {

        @Override
        public void onResume() {
            super.onResume();
            Log.v("siriusmeme", "kek");
            if (adapter.memeFetcher != null && adapter.memeFetcher.isCancelled())
                adapter.startKek();
        }

        @Override
        public void onPause() {
            super.onPause();
            adapter.memeFetcher.cancel(true);
        }

        @Override
        public void onStop() {
            super.onStop();
            adapter.memeFetcher.cancel(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            adapter.memeFetcher.cancel(true);
        }

        private static final String ARG_SECTION_NUMBER = "section_number";
        private MemeAdapter adapter;

        public PlaceholderFragment() {
        }

        public void setAdapter(MemeAdapter adapter) {
            this.adapter = adapter;
        }

        public static PlaceholderFragment newInstance(int sectionNumber, MemeAdapter adapter) {
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

            RecyclerView.LayoutManager layoutManager = new WrapContentLinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);

            recyclerView.setAdapter(adapter);

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        ArrayList<PlaceholderFragment> fragmentArrayList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0)
                return PlaceholderFragment.newInstance(1, firstMemeAdapter);
            else if (position == 1)
                return PlaceholderFragment.newInstance(2, thirdMemeAdapter);
            else
                return PlaceholderFragment.newInstance(3, secondMemeAdapter);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_mem) {

            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, RC_GET_MEME_IMAGE);//one can be replaced with any action code

        } else if (id == R.id.logout_btn) {
            preferences.edit().clear().apply();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    private void uploadBitmap(final Bitmap bitmap, String title, boolean isAmoral) {


        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, getString(R.string.upload_meme_url), null, null) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("auth_token", preferences.getString("auth_token", null));
                params.put("title", title);
                params.put("is_amoral", isAmoral ? "true" : "false");
                return params;
            }

            /*
            * Here we are passing image by renaming it with a unique name
            * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("file", new DataPart(imagename + "", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };
        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(40000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this.getApplicationContext()).add(volleyMultipartRequest);
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


}
