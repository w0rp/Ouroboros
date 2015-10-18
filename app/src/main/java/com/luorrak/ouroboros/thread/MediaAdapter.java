package com.luorrak.ouroboros.thread;

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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.deepzoom.DeepZoomActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.Media;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.Util;

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


public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    final int VERTICAL = 0;
    final int HORIZONTAL = 1;
    private String boardName;
    private String resto;
    private ArrayList<Media> mediaItems;
    private List<String> validExt = Arrays.asList(".png", ".jpg", ".jpeg", ".gif");
    private Context context;

    private final int W = 0, H = 1;
    private int maxImgWidth;
    private int maxImgHeight;
    private int minImgHeight;


    public MediaAdapter(ArrayList<Media> mediaItems, String boardName, String resto, Context context) {
        this.mediaItems = mediaItems;
        this.boardName = boardName;
        this.resto = resto;
        this.context = context;
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        NetworkHelper networkHelper = new NetworkHelper();
        final MediaViewHolder mediaViewHolder = holder;
        final Media media = mediaItems.get(position);
        mediaViewHolder.playButton.setVisibility(View.GONE);

        updateImageBounds();
        final int[] size = new int[2]; calcSize(size, Double.parseDouble(media.height), Double.parseDouble(media.width));

        final int threadValue = Util.getThreadView(context);

        switch (threadValue){
            default:
            case Util.THREAD_LAYOUT_VERTICAL: {
                if (mediaItems.size() <= 1){
                    mediaViewHolder.mediaImage.getLayoutParams().height = size[H];
                    mediaViewHolder.mediaImage.getLayoutParams().width = maxImgWidth;
                } else {
                    mediaViewHolder.mediaImage.getLayoutParams().height =
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, context.getResources().getDisplayMetrics());

                    mediaViewHolder.mediaImage.getLayoutParams().width = maxImgWidth;
                    mediaViewHolder.mediaImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                break;
            }
            case Util.THREAD_LAYOUT_HORIZONTAL: {
                break;
            }
        }

        if (validExt.contains(media.ext)){
            String imageUrl = ChanUrls.getThumbnailUrl(boardName, media.fileName);
            Ion.with(mediaViewHolder.mediaImage)
                    .smartSize(true)
                    .crossfade(true)
                    .load(imageUrl)
                    .withBitmapInfo()
                    .setCallback(new FutureCallback<ImageViewBitmapInfo>() {
                        @Override
                        public void onCompleted(Exception e, ImageViewBitmapInfo result) {
                            if (e != null || result.getBitmapInfo() == null) {
                                return;
                            }

                            if (threadValue == Util.THREAD_LAYOUT_HORIZONTAL){
                                return;
                            }

                            generateSwatch(result.getBitmapInfo().bitmap, mediaViewHolder);

                            Ion.with(result.getImageView())
                                    .crossfade(true)
                                    .smartSize(true)
                                    .load(ChanUrls.getImageUrl(boardName, media.fileName, media.ext))
                                    .withBitmapInfo();
                        }
                    });

            mediaViewHolder.mediaImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DeepZoomActivity.class);
                    intent.putExtra(CatalogAdapter.TIM, media.fileName);
                    intent.putExtra(CatalogAdapter.THREAD_NO, resto);
                    intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
                    context.startActivity(intent);
                }
            });
        } else if (media.ext.equals(".webm") || media.ext.equals(".mp4")){
            String imageUrl = ChanUrls.getThumbnailUrl(boardName, media.fileName);
            Ion.with(mediaViewHolder.mediaImage)
                    .smartSize(true)
                    .crossfade(true)
                    .load(imageUrl)
                    .withBitmapInfo()
                    .setCallback(new FutureCallback<ImageViewBitmapInfo>() {
                        @Override
                        public void onCompleted(Exception e, ImageViewBitmapInfo result) {
                            if (e != null || result.getBitmapInfo() == null) {
                                return;
                            }

                            generateSwatch(result.getBitmapInfo().bitmap, mediaViewHolder);
                        }
                    });
            mediaViewHolder.playButton.setVisibility(View.VISIBLE);

            if (media.ext.equals(".webm")){
                mediaViewHolder.playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(ChanUrls.getImageUrl(boardName, media.fileName, media.ext));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "video/webm");
                        mediaViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            } else if (media.ext.equals(".mp4")) {
                mediaViewHolder.playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(ChanUrls.getImageUrl(boardName, media.fileName, media.ext));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "video/mp4");
                        mediaViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
    }

    private void generateSwatch(Bitmap bitmap, final MediaViewHolder mediaViewHolder) {
        Palette.generateAsync(bitmap,
                new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        Palette.Swatch vibrant =
                                palette.getLightMutedSwatch();
                        if (vibrant != null) {
                            mediaViewHolder.mediaHolder.setBackgroundColor(
                                    vibrant.getRgb());
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    @Override
    public void onViewRecycled(MediaViewHolder holder) {
        holder.mediaImage.setImageDrawable(null);
        super.onViewRecycled(holder);
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout mediaHolder;
        public ImageView mediaImage;
        public ImageView playButton;

        public MediaViewHolder(View itemView) {
            super(itemView);
            mediaHolder = (FrameLayout) itemView.findViewById(R.id.media_holder);
            mediaImage = (ImageView) itemView.findViewById(R.id.thread_media_item);
            playButton = (ImageView) itemView.findViewById(R.id.thread_media_play_button);
        }
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
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int screenWidth, screenHeight;
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        maxImgWidth = (int) (screenWidth * 0.945);
        maxImgHeight = (int) (screenHeight * 0.8);
        minImgHeight = (int) (screenHeight * 0.15);
    }
}

