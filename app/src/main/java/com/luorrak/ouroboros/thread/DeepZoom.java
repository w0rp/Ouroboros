package com.luorrak.ouroboros.thread;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.NetworkHelper;

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
public class DeepZoom extends Fragment{
    NetworkHelper networkHelper = new NetworkHelper();
    String boardName;
    String tim;
    String ext;
    String oldTitle;
    PhotoView photoView;

    public DeepZoom() {
    }

    public DeepZoom newInstance(String boardName, String tim, String ext, CharSequence title) {
        DeepZoom frag = new DeepZoom();
        Bundle args = new Bundle();
        args.putString("boardName", boardName);
        args.putString("tim", tim);
        args.putString("ext", ext);
        args.putString("title", title.toString());
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_deepzoom, container, false);
        final ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progress_bar);
        photoView = (PhotoView) view.findViewById(R.id.deepzoom_photoview);
        photoView.setMaximumScale(16);

        progressBar.setVisibility(View.VISIBLE);

        if (getArguments() != null){
            boardName = getArguments().getString("boardName");
            tim = getArguments().getString("tim");
            ext = getArguments().getString("ext");
            oldTitle = getArguments().getString("title");
            getActivity().setTitle(tim + ext);
        }

        Ion.with(photoView)
                .load(ChanUrls.getImageUrl(boardName, tim, ext))
                .setCallback(new FutureCallback<android.widget.ImageView>() {
                    @Override
                    public void onCompleted(Exception e, android.widget.ImageView imageView) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem saveImage = menu.findItem(R.id.action_save_image);
        saveImage.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save_image: {
                Toast.makeText(getActivity(), "Downloading...", Toast.LENGTH_SHORT).show();
                networkHelper.downloadFile(boardName, tim, ext, getActivity());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        getActivity().setTitle(oldTitle);
        super.onDestroyView();
    }
}
