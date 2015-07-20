package com.luorrak.ouroboros.gallery;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.thread.DeepZoom;
import com.luorrak.ouroboros.thread.ThreadActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.NetworkHelper;

import java.util.Arrays;
import java.util.List;

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


public class GalleryAdapter extends CursorRecyclerAdapter {
    NetworkHelper networkHelper = new NetworkHelper();
    String boardName;
    List<String> validExt = Arrays.asList(".png", ".jpg", ".gif");
    public GalleryAdapter(Cursor cursor, String boardName) {
        super(cursor);
        this.boardName = boardName;
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        GalleryViewHolder galleryViewHolder = (GalleryViewHolder)holder;

        final String tim = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_TIMS));
        final String ext = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_EXTS));

        if (validExt.contains(ext)){
            String imageUrl = ChanUrls.getThumbnailUrl(boardName, tim);
            networkHelper.getImageNoCrossfade(galleryViewHolder.galleryImage, imageUrl);

            galleryViewHolder.galleryImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DeepZoom.class);
                    intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
                    intent.putExtra(CatalogAdapter.TIM, tim);
                    intent.putExtra(CatalogAdapter.EXT, ext);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
        return new GalleryViewHolder(view);
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView galleryImage;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            galleryImage = (ImageView) itemView.findViewById(R.id.gallery_image);
        }
    }
}
