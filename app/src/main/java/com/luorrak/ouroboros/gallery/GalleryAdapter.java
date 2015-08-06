package com.luorrak.ouroboros.gallery;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.deepzoom.DeepZoomActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.NetworkHelper;

import java.util.ArrayList;
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


public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    NetworkHelper networkHelper = new NetworkHelper();
    String boardName;
    String resto;
    FragmentManager fragmentManager;
    ArrayList<Media> mediaItems;
    List<String> validExt = Arrays.asList(".png", ".jpg", ".jpeg", ".gif");
    Context context;
    public GalleryAdapter(ArrayList<Media> mediaItems, String boardName, String resto, FragmentManager fragmentManager, Context context) {
        this.mediaItems = mediaItems;
        this.boardName = boardName;
        this.resto = resto;
        this.fragmentManager = fragmentManager;
        this.context = context;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        final GalleryViewHolder galleryViewHolder = (GalleryViewHolder)holder;
        final Media media = mediaItems.get(position);
        galleryViewHolder.playButton.setVisibility(View.GONE);

        if (validExt.contains(media.ext)){
            String imageUrl = ChanUrls.getThumbnailUrl(boardName, media.fileName);
            networkHelper.getImageNoCrossfade(galleryViewHolder.galleryImage, imageUrl);

            galleryViewHolder.galleryImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DeepZoomActivity.class);
                    intent.putExtra(CatalogAdapter.TIM, media.fileName);
                    intent.putExtra(CatalogAdapter.THREAD_NO, resto);
                    intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
                    context.startActivity(intent);
                }
            });
        } else {
            if (media.ext.equals(".webm")){
                String imageUrl = ChanUrls.getThumbnailUrl(boardName, media.fileName);
                networkHelper.getImageNoCrossfade(galleryViewHolder.galleryImage, imageUrl);
                galleryViewHolder.playButton.setVisibility(View.VISIBLE);

                galleryViewHolder.playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(ChanUrls.getImageUrl(boardName, media.fileName, media.ext));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "video/webm");
                        galleryViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            } else if (media.ext.equals(".mp4")) {
                String imageUrl = ChanUrls.getThumbnailUrl(boardName, media.fileName);
                networkHelper.getImageNoCrossfade(galleryViewHolder.galleryImage, imageUrl);
                galleryViewHolder.playButton.setVisibility(View.VISIBLE);

                galleryViewHolder.playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(ChanUrls.getImageUrl(boardName, media.fileName, media.ext));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "video/mp4");
                        galleryViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }
    
    class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView galleryImage;
        ImageView playButton;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            galleryImage = (ImageView) itemView.findViewById(R.id.gallery_image);
            playButton = (ImageView) itemView.findViewById(R.id.gallery_video_play_button);
        }
    }
}
