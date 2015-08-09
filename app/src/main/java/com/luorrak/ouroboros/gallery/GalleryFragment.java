package com.luorrak.ouroboros.gallery;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.util.Media;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.Util;

import java.util.ArrayList;
import java.util.Collection;

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

public class GalleryFragment extends Fragment {

    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;
    GalleryAdapter galleryAdapter;
    String boardName;
    String resto;
    InfiniteDbHelper infiniteDbHelper;
    NetworkHelper networkHelper;

    public GalleryFragment(){
    }

    public GalleryFragment newInstance(String boardName, String resto) {
        GalleryFragment frag = new GalleryFragment();
        Bundle args = new Bundle();
        args.putString("boardName", boardName);
        args.putString("resto", resto);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        infiniteDbHelper = new InfiniteDbHelper(getActivity());
        networkHelper = new NetworkHelper();
        ArrayList<Media> mediaArrayList = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        getActivity().setTitle("Gallery");
        if (getArguments() != null){
            boardName = getArguments().getString("boardName");
            resto = getArguments().getString("resto");
        }

        Cursor cursor = infiniteDbHelper.getThreadCursor(resto);
        do {
            byte[] serializedPostMedia = cursor.getBlob(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_MEDIA_FILES));
            if(serializedPostMedia != null){
                mediaArrayList.addAll((Collection<? extends Media>) Util.deserializeObject(serializedPostMedia));
            }
        } while (cursor.moveToNext());

        cursor.close();

        recyclerView = (RecyclerView) view.findViewById(R.id.gallery_list);
        gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        galleryAdapter = new GalleryAdapter(mediaArrayList, boardName, resto, getFragmentManager(), getActivity());
        recyclerView.setAdapter(galleryAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem saveAllImagesButton = menu.findItem(R.id.action_save_all_images);
        saveAllImagesButton.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save_all_images: {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Download All Images")
                        .setMessage("Are you sure you want to download all images?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String tim;
                                String ext;
                                Cursor imageCursor = infiniteDbHelper.getGalleryCursor(resto);
                                do {
                                    tim = imageCursor.getString(imageCursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_TIMS));
                                    ext = imageCursor.getString(imageCursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_EXTS));
                                    networkHelper.downloadFile(boardName, tim, ext, getActivity());

                                } while (imageCursor.moveToNext());

                                imageCursor.close();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
