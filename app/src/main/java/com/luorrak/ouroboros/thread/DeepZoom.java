package com.luorrak.ouroboros.thread;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.util.ChanUrls;
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
    String boardName;
    String tim;
    String ext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.onActivityCreateSetTheme(this, Util.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deepzoom);
        PhotoView photoView = (PhotoView) findViewById(R.id.deepzoom_photoview);
        photoView.setMaximumScale(16);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final ProgressDialog dlg = new ProgressDialog(this);
        dlg.setTitle("Loading...");
        dlg.setIndeterminate(false);
        dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dlg.show();

        boardName = getIntent().getStringExtra(CatalogAdapter.BOARD_NAME);
        tim = getIntent().getStringExtra(CatalogAdapter.TIM);
        ext = getIntent().getStringExtra(CatalogAdapter.EXT);

        Ion.with(this)
                .load(ChanUrls.getImageUrl(boardName, tim, ext))
                .progressDialog(dlg)
                .setLogging("DeepZoom", Log.VERBOSE)
                .withBitmap()
                .deepZoom()
                .intoImageView(photoView)
                .setCallback(new FutureCallback<android.widget.ImageView>() {
                    @Override
                    public void onCompleted(Exception e, android.widget.ImageView imageView) {
                        dlg.cancel();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem saveImage = menu.findItem(R.id.action_save_image);
        saveImage.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save_image: {
                Toast.makeText(getApplicationContext(), "Placeholder", Toast.LENGTH_SHORT).show();
                //Download manager to continue being lazy
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(ChanUrls.getImageUrl(boardName, tim, ext)));
                request.setDescription(tim + ext);
                request.setTitle(tim + ext);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, tim + ext);

                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
