package com.luorrak.ouroboros.thread;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.Util;

import uk.co.senab.photoview.PhotoView;

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

//https://github.com/koush/ion/blob/master/ion-sample/src/com/koushikdutta/ion/sample/DeepZoomSample.java
public class DeepZoom extends AppCompatActivity{
    NetworkHelper networkHelper = new NetworkHelper();
    String boardName;
    String tim;
    String ext;
    String oldTitle;
    PhotoView photoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Util.onActivityCreateSetTheme(this, Util.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deepzoom);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        photoView = (PhotoView) findViewById(R.id.deepzoom_photoview);
        photoView.setMaximumScale(16);

        progressBar.setVisibility(View.VISIBLE);

        boardName = getIntent().getStringExtra(CatalogAdapter.BOARD_NAME);
        tim = getIntent().getStringExtra(CatalogAdapter.TIM);
        ext = getIntent().getStringExtra(CatalogAdapter.EXT);
        setTitle(tim + ext);

        Ion.with(photoView)
                .load(ChanUrls.getImageUrl(boardName, tim, ext))
                .setCallback(new FutureCallback<android.widget.ImageView>() {
                    @Override
                    public void onCompleted(Exception e, android.widget.ImageView imageView) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem saveImage = menu.findItem(R.id.action_save_image);
        saveImage.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save_image: {
                Toast.makeText(getApplicationContext(), "Downloading...", Toast.LENGTH_SHORT).show();
                networkHelper.downloadFile(boardName, tim, ext, getApplicationContext());
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
