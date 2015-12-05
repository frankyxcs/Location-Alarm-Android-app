package com.keerthan.locationalarm2;


import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.keerthan.locationalarm2.MapHandlers.MapTabFragment;
import com.keerthan.locationalarm2.SlidingTab.SlidingTabLayout;
import com.keerthan.locationalarm2.TabResponders.MyFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private Toolbar toolbar=null;
    ViewPager viewPager;
    MyFragmentPagerAdapter fragPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        fragPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(fragPagerAdapter);
        //viewPager.setOffscreenPageLimit(4);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d(TAG, "onCreateOptionsMenu");

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        setSearchAction(searchView);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public boolean setSearchAction(SearchView searchView){
        boolean successful = false;
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);


            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                public boolean onQueryTextChange(String newText) {
                    // this is your adapter that will be filtered
                    return true;
                }

                public boolean onQueryTextSubmit(String query) {
                    //Here u can get the value "query" which is entered in the search box.

                    MapTabFragment mtf = fragPagerAdapter.mapFrag;
                    if(mtf!=null) {
                        mtf.receiveSearchText(query);
                        viewPager.setCurrentItem(0);
                        return true;
                    }
                    return false;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
            successful = true;
        }
        return successful;
    }













    //----------------ONLY FOR LOGS-----------------------
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }
}
