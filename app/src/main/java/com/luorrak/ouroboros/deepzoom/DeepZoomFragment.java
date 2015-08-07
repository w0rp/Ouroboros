package com.luorrak.ouroboros.deepzoom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.util.Media;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.NetworkHelper;

import uk.co.senab.photoview.PhotoView;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  NothingOfNote
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class DeepZoomFragment extends Fragment {
    PhotoView photoView;
    ProgressBar progressBar;
    NetworkHelper networkHelper;
    InfiniteDbHelper infiniteDbHelper;
    int position;
    String boardName;
    String resto;
    Media mediaItem;
    private ActionProvider shareActionProvider;

    public Fragment newInstance(String boardName, String resto, int position) {
        DeepZoomFragment deepZoomFragment = new DeepZoomFragment();
        Bundle args = new Bundle();
        args.putString("boardName", boardName);
        args.putString("resto", resto);
        args.putInt("position", position);
        deepZoomFragment.setArguments(args);
        return deepZoomFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkHelper = new NetworkHelper();
        infiniteDbHelper = new InfiniteDbHelper(getActivity());
        if (getArguments() != null){
            boardName = getArguments().getString("boardName");
            resto = getArguments().getString("resto");
            position = getArguments().getInt("position");
        }

        if (savedInstanceState != null){
            boardName = savedInstanceState.getString("boardName");
            resto = savedInstanceState.getString("resto");
            position = savedInstanceState.getInt("position");
        }
        ((DeepZoomActivity) getActivity()).newMediaListInstance(infiniteDbHelper, resto);
        mediaItem = ((DeepZoomActivity) getActivity()).getMediaItem(position);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_deepzoom, container, false);
        setHasOptionsMenu(true);

        photoView = (PhotoView) rootView.findViewById(R.id.deepzoom_photoview);
        photoView.setMaximumScale(16);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        Ion.with(photoView)
                .load(ChanUrls.getImageUrl(boardName, mediaItem.fileName, mediaItem.ext))
                .setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, android.widget.ImageView imageView) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("boardName", boardName);
        outState.putString("resto", resto);
        outState.putInt("position", position);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem saveImage = menu.findItem(R.id.action_save_image);
        MenuItem openExternalButton = menu.findItem(R.id.action_external_browser);
        MenuItem shareButton = menu.findItem(R.id.menu_item_share);

        shareButton.setVisible(true);
        saveImage.setVisible(true);
        openExternalButton.setVisible(true);

        shareActionProvider = MenuItemCompat.getActionProvider(shareButton);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save_image: {
                Toast.makeText(getActivity(), "Downloading...", Toast.LENGTH_SHORT).show();
                networkHelper.downloadFile(boardName, mediaItem.fileName, mediaItem.ext, getActivity());
                break;
            }
            case R.id.action_external_browser: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ChanUrls.getImageUrl(boardName, mediaItem.fileName, mediaItem.ext)));
                startActivity(browserIntent);
                break;
            }
            case R.id.menu_item_share: {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = ChanUrls.getImageUrl(boardName, mediaItem.fileName, mediaItem.ext);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
