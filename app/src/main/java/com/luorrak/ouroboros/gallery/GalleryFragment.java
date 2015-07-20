package com.luorrak.ouroboros.gallery;

import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.util.InfiniteDbHelper;

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
    InfiniteDbHelper infiniteDbHelper;

    public GalleryFragment(){
    }

    public GalleryFragment newInstance(String boardName) {
        GalleryFragment frag = new GalleryFragment();
        Bundle args = new Bundle();
        args.putString("boardName", boardName);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        infiniteDbHelper = new InfiniteDbHelper(getActivity());
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        if (getArguments() != null){
            boardName = getArguments().getString("boardName");
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.gallery_list);
        gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        galleryAdapter = new GalleryAdapter(infiniteDbHelper.getGalleryCursor(), boardName);
        recyclerView.setAdapter(galleryAdapter);

        return view;
    }
}
