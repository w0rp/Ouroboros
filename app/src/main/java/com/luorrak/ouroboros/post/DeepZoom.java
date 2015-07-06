package com.luorrak.ouroboros.post;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.util.ChanUrls;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

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
public class DeepZoom extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhotoView photoView = new PhotoView(this);
        photoView.setMaximumScale(16);
        setContentView(photoView);

        final ProgressDialog dlg = new ProgressDialog(this);
        dlg.setTitle("Loading...");
        dlg.setIndeterminate(false);
        dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dlg.show();

        String boardName = getIntent().getStringExtra(CatalogAdapter.BOARD_NAME);
        String tim = getIntent().getStringExtra(CatalogAdapter.TIM);
        String ext = getIntent().getStringExtra(CatalogAdapter.EXT);

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
}
