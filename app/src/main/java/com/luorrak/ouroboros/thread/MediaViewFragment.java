package com.luorrak.ouroboros.thread;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.deepzoom.DeepZoomActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.Media;
import com.luorrak.ouroboros.util.Util;

import java.util.ArrayList;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
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
public class MediaViewFragment extends Fragment {
    private final int W = 0, H = 1;
    private int maxImgWidth;
    private int maxImgHeight;
    private int minImgHeight;

    public Fragment newInstance(String boardName, int position, byte[] mediaArrayList, String embed, String resto) {
        MediaViewFragment mediaViewFragment = new MediaViewFragment();
        Bundle args = new Bundle();
        args.putString("boardName", boardName);
        args.putByteArray("mediaList", mediaArrayList);
        args.putInt("position", position);
        args.putString("embed", embed);
        args.putString("resto", resto);
        mediaViewFragment.setArguments(args);
        return mediaViewFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String boardName = null;
        byte[] serializedMediaList = null;
        int position = 0;
        String embed = null;
        String resto = null;
        final String[] youtubeData;

        View view = inflater.inflate(R.layout.media_item, container, false);
        ImageView mediaView = (ImageView) view.findViewById(R.id.thread_media_item);
        ImageView videoPlayButton = (ImageView) view.findViewById(R.id.thread_media_play_button);

        super.onCreateView(inflater, container, savedInstanceState);
        if (getArguments() != null){
            boardName = getArguments().getString("boardName");
            serializedMediaList = getArguments().getByteArray("mediaList");
            position = getArguments().getInt("position");
            embed = getArguments().getString("embed");
            resto = getArguments().getString("resto");
        }

        ArrayList<Media> mediaList = (ArrayList<Media>) Util.deserializeObject(serializedMediaList);

        final Media mediaItem = mediaList.get(position);

        if (mediaItem.fileName != null && mediaItem.ext != null){
            mediaView.setVisibility(View.VISIBLE);
            updateImageBounds();
            final int[] size = new int[2]; calcSize(size, Double.parseDouble(mediaItem.height), Double.parseDouble(mediaItem.width));
            mediaView.getLayoutParams().height = size[H];
            final String finalBoardName = boardName;

            Ion.with(mediaView)
                    .smartSize(true)
                    .crossfade(true)
                    .load(ChanUrls.getThumbnailUrl(boardName, mediaItem.fileName))
                    .withBitmapInfo()
                    .setCallback(new FutureCallback<ImageViewBitmapInfo>() {
                        @Override
                        public void onCompleted(Exception e, ImageViewBitmapInfo imageViewBitmapInfo) {
                            if (e != null || imageViewBitmapInfo.getBitmapInfo() == null || mediaItem.ext.equals(".webm") || mediaItem.ext.equals(".mp4") ) {
                                return;
                            }
                            Ion.with(imageViewBitmapInfo.getImageView())
                                    .crossfade(true)
                                    .smartSize(true)
                                    .load(ChanUrls.getImageUrl(finalBoardName, mediaItem.fileName, mediaItem.ext))
                                    .withBitmapInfo();
                        }
                    });
            if (mediaItem.ext.equals(".webm") || mediaItem.ext.equals(".mp4")){
                videoPlayButton.setVisibility(View.VISIBLE);
                initVideoIntent(boardName, mediaItem.fileName, mediaItem.ext);
                mediaView.setOnClickListener(null);
            } else {
                final String finalResto = resto;
                mediaView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initDeepZoomIntent(finalBoardName, mediaItem.fileName, finalResto);
                    }
                });
            }
        } else if (embed != null && embed.contains("youtube.com")){
            mediaView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            youtubeData = Util.parseYoutube(embed);
            mediaView.setOnClickListener(null);

            if (youtubeData[0] != null){
                videoPlayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initYoutubeIntent(youtubeData[0]);
                    }
                });
            }
        }
        return view;
    }

    private void initVideoIntent(String boardName, String tim, String ext){
        Uri uri = Uri.parse(ChanUrls.getImageUrl(boardName, tim, ext));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/" + ext);
        getActivity().startActivity(intent);
    }

    private void initDeepZoomIntent(String boardName, String tim, String resto){
        Intent intent = new Intent(getActivity(), DeepZoomActivity.class);
        intent.putExtra(CatalogAdapter.TIM, tim);
        intent.putExtra(CatalogAdapter.THREAD_NO, resto);
        intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
        getActivity().startActivity(intent);
    }

    private void initYoutubeIntent(String youtubeVideoUrl){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeVideoUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
    }

    //Adapted from Chanobol
    private void calcSize(int[] size, double imageHeight, double imageWidth) {
        double w = imageWidth, h = imageHeight;
        if (w < maxImgWidth) {
            double w_old = w;
            w = Math.min(maxImgWidth, w_old * 2);
            h *= w / w_old;
        }
        if (h < minImgHeight) {
            double h_old = h;
            h = minImgHeight;
            w *= h / h_old;
        }

        if (w > maxImgWidth) {
            double w_old = w;
            w = maxImgWidth;
            h *= w / w_old;
        }
        if (h > maxImgHeight) {
            double h_old = h;
            h = maxImgHeight;
            w *= h / h_old;
        }

        size[W] = (int) w;
        size[H] = (int) h;
    }

    //Adapted from Chanobol
    private void updateImageBounds() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int screenWidth, screenHeight;
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        maxImgWidth = (int) (screenWidth * 0.9);
        maxImgHeight = (int) (screenHeight * 0.8);
        minImgHeight = (int) (screenHeight * 0.15);
    }
}
