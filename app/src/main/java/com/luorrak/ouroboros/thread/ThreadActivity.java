package com.luorrak.ouroboros.thread;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogActivity;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.Util;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ThreadActivity extends AppCompatActivity {
    private Toolbar toolbar;
    InfiniteDbHelper infiniteDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.onActivityCreateSetTheme(this, Util.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        Ion.getDefault(getApplicationContext()).getCache().setMaxSize(150 * 1024 * 1024);
        infiniteDbHelper = new InfiniteDbHelper(getApplicationContext());

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.placeholder_card_dialog) != null){
            if(savedInstanceState != null){
                return;
            }

            String resto = getIntent().getStringExtra(CatalogAdapter.THREAD_NO);
            String boardName = getIntent().getStringExtra(CatalogAdapter.BOARD_NAME);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ThreadFragment threadFragment = new ThreadFragment().newInstance(resto, boardName);
            fragmentTransaction.replace(R.id.placeholder_card, threadFragment)
                    .commit();

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Life cycle //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDestroy() {
        if (!isChangingConfigurations()){
            infiniteDbHelper.deleteThreadCache();
        }
        super.onDestroy();
    }

    // Callbacks ///////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home){
            this.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
       if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
       } else {
           this.finish();
       }
    }

    public void doPositiveClickExternal(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void doPositiveClickInternal(String threadNo, String boardName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //clear dialog fragments
        fragmentManager.popBackStack("threadDialog", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (threadNo != "0"){
            ThreadFragment threadFragment = new ThreadFragment().newInstance(threadNo, boardName);
            fragmentTransaction.replace(R.id.placeholder_card, threadFragment)
                    .addToBackStack("thread")
                    .commit();
        } else {
            Intent intent = new Intent(this, CatalogActivity.class);
            intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
            startActivity(intent);

        }

    }

    public void doNegativeClick() {
    }
}
