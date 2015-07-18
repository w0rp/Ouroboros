package com.luorrak.ouroboros.thread;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.reply.ReplyCommentActivity;
import com.luorrak.ouroboros.api.CommentParser;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.Util;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.Ion;


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
public class ThreadAdapter extends CursorRecyclerAdapter {
    private final String LOG_TAG = ThreadAdapter.class.getSimpleName();

    NetworkHelper networkHelper = new NetworkHelper();
    CommentParser commentParser = new CommentParser();
    private LayoutInflater inflater;
    private FragmentManager fragmentManager;
    private String boardName;
    private boolean visibile;
    private Context context;
    private InfiniteDbHelper infiniteDbHelper;
    private final int W = 0, H = 1;
    private int maxImgWidth;
    private int maxImgHeight;
    private int minImgHeight;

    public ThreadAdapter(Cursor cursor, FragmentManager fragmentManager, String boardName, Context context) {
        super(cursor);
        this.fragmentManager = fragmentManager;
        this.boardName = boardName;
        this.context = context;
        this.infiniteDbHelper = new InfiniteDbHelper(context);
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        String name;
        String tripcode;
        final String no;
        String id;
        String sub;
        String com;
        String email;
        long threadTime;
        final String tim;
        final String ext;
        final String embed;
        int imageWidth;
        int imageHeight;
        String imageUrl = null;
        boolean isVideo = false;
        boolean isWebm = false;
        boolean isMp4 = false;
        final String resto;
        //Should make this into an object to make it more obvious
        String[] youtubeData = {null, null}; //youtubeData[0] is video url youtubeData[1] is image url to load for thumbnail

        ThreadViewHolder threadViewHolder = (ThreadViewHolder)holder;

        //Prevent refresh flickering
        threadViewHolder.image_0.setVisibility(View.GONE);
        threadViewHolder.threadName.setVisibility(View.GONE);
        threadViewHolder.threadTripcode.setVisibility(View.GONE);
        threadViewHolder.threadNo.setVisibility(View.GONE);
        threadViewHolder.id.setVisibility(View.GONE);
        threadViewHolder.threadSub.setVisibility(View.GONE);
        threadViewHolder.threadCom.setVisibility(View.GONE);
        threadViewHolder.threadEmail.setVisibility(View.GONE);
        threadViewHolder.threadTime.setVisibility(View.GONE);
        threadViewHolder.threadReplies.setVisibility(View.GONE);
        threadViewHolder.videoPlayButton.setVisibility(View.GONE);

        name = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_NAME));
        tripcode = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_TRIP));
        no = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_NO));
        sub = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_SUB));
        com = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_COM));
        email = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_EMAIL));
        threadTime = cursor.getLong(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_TIME));
        tim = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_TIMS));
        ext = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_EXTS));
        embed = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_EMBED));
        resto = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_RESTO));
        imageHeight = Integer.valueOf(cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_IMAGE_HEIGHT)));
        imageWidth = Integer.valueOf(cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_IMAGE_WIDTH)));
        id = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_ID));

        Cursor repliesCursor = infiniteDbHelper.getReplies(no);
        String replies = String.valueOf(repliesCursor.getCount());
        repliesCursor.close();

        if (infiniteDbHelper.isNoUserPost(boardName, no)){
            name = "(You) " + name;
        }
        threadViewHolder.threadName.setText(name);
        threadViewHolder.threadName.setVisibility(View.VISIBLE);

        threadViewHolder.threadTripcode.setText(tripcode);
        threadViewHolder.threadTripcode.setVisibility(View.VISIBLE);

        threadViewHolder.threadNo.setText(no);
        threadViewHolder.threadNo.setVisibility(View.VISIBLE);

        if (id != null){
            threadViewHolder.id.setText(commentParser.parseId(id));
            threadViewHolder.id.setVisibility(View.VISIBLE);
        }

        //does subject exist
        if(sub != null){
            threadViewHolder.threadSub.setText(sub);
            threadViewHolder.threadSub.setVisibility(View.VISIBLE);
        }

        //Does comment exist
        if (com != null){
            Spannable spannableCom = commentParser.parseCom(com,
                    boardName,
                    cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_RESTO)),
                    fragmentManager,
                    infiniteDbHelper
            );
            threadViewHolder.threadCom.setVisibility(View.VISIBLE);
            threadViewHolder.threadCom.setText(spannableCom);
        }

        if (email != null && email.equals("sage")){
            threadViewHolder.threadEmail.setVisibility(View.VISIBLE);
        }

        threadViewHolder.threadTime.setText(
                DateUtils.getRelativeTimeSpanString(
                        threadTime * 1000,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                ));
        threadViewHolder.threadTime.setVisibility(View.VISIBLE);

        //does image exist?
        if (tim != null && ext != null){
            switch (ext) {
                case ".webm":
                    isVideo = true;
                    isWebm = true;
                    imageUrl = ChanUrls.getThumbnailUrl(boardName, tim);
                    break;
                case ".mp4":
                    isVideo = true;
                    isMp4 = true;
                    imageUrl = ChanUrls.getThumbnailUrl(boardName, tim);
                    break;
                default:
                    imageUrl = ChanUrls.getThumbnailUrl(boardName, tim);
                    break;
            }
        } else if (embed != null && embed.contains("youtube.com")){
            isVideo = true;
            youtubeData = Util.parseYoutube(embed);
            imageUrl = "https://" + youtubeData[1];
        }

        if (imageUrl != null){
            threadViewHolder.image_0.setVisibility(View.VISIBLE);
            if (embed == null){
                updateImageBounds();
                final int[] size = new int[2]; calcSize(size, imageHeight, imageWidth);
                threadViewHolder.image_0.getLayoutParams().height = size[H];
            } else {
                //Jumping bug is back again here
                threadViewHolder.image_0.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            final boolean finalIsVideo = isVideo;
            Ion.with(threadViewHolder.image_0)
                    .smartSize(true)
                    .crossfade(true)
                    .load(imageUrl)
                    .withBitmapInfo()
                    .setCallback(new FutureCallback<ImageViewBitmapInfo>() {
                        @Override
                        public void onCompleted(Exception e, ImageViewBitmapInfo imageViewBitmapInfo) {
                            if (e != null || imageViewBitmapInfo.getBitmapInfo() == null || finalIsVideo){
                                return;
                            }
                            Ion.with(imageViewBitmapInfo.getImageView())
                                    .crossfade(true)
                                    .smartSize(true)
                                    .load(ChanUrls.getImageUrl(boardName, tim, ext))
                                    .withBitmapInfo();
                        }
                    });
            if (isVideo) {
                threadViewHolder.videoPlayButton.setVisibility(View.VISIBLE);
                if (isWebm){
                    threadViewHolder.videoPlayButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse(ChanUrls.getImageUrl(boardName, tim, ext));
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "video/webm");
                            context.startActivity(intent);
                        }
                    });
                } else if (isMp4) {
                    threadViewHolder.videoPlayButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse(ChanUrls.getImageUrl(boardName, tim, ext));
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "video/mp4");
                            context.startActivity(intent);
                        }
                    });
                } else {
                        final String youtubeVideoUrl = youtubeData[0];
                        //isembed
                        threadViewHolder.videoPlayButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeVideoUrl));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        });
                }
                threadViewHolder.image_0.setOnClickListener(null);

            } else {
                threadViewHolder.image_0.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, DeepZoom.class);
                        intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
                        intent.putExtra(CatalogAdapter.TIM, tim);
                        intent.putExtra(CatalogAdapter.EXT, ext);
                        context.startActivity(intent);
                    }
                });
            }
        }

        if (!replies.equals("0")){
            threadViewHolder.threadReplies.setVisibility(View.VISIBLE);
            threadViewHolder.threadReplies.setText(replies + " Replies");
        }


        // OnClick /////////////////////////////////////////////////////////////////////////////////

        threadViewHolder.threadReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Reply button pressed " + no);
                Intent intent =  new Intent(context, ReplyCommentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(CatalogAdapter.THREAD_NO, resto);
                intent.putExtra(CatalogAdapter.REPLY_NO, no);
                intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
                context.startActivity(intent);
            }
        });

        threadViewHolder.threadCom.setMovementMethod(LinkMovementMethod.getInstance());
        threadViewHolder.threadReplies.setMovementMethod(LinkMovementMethod.getInstance());
        threadViewHolder.threadReplyButton.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        ThreadViewHolder threadViewHolder = (ThreadViewHolder) holder;
        threadViewHolder.image_0.setImageDrawable(null);
        super.onViewRecycled(holder);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_thread, parent, false);
        return new ThreadViewHolder(view);
    }

    // TextHandling ////////////////////////////////////////////////////////////////////////////////



    // Utility /////////////////////////////////////////////////////////////////////////////////////

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

        maxImgWidth = (int) (screenWidth * 0.9);
        maxImgHeight = (int) (screenHeight * 0.8);
        minImgHeight = (int) (screenHeight * 0.15);
    }

    class ThreadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView image_0;
        public TextView threadName;
        public TextView threadTripcode;
        public TextView threadNo;
        public TextView id;
        public TextView threadSub;
        public TextView threadCom;
        public TextView threadEmail;
        public TextView threadTime;
        public Button threadReplies;
        public ImageView videoPlayButton;
        public Button threadReplyButton;

        public ThreadViewHolder(View itemView) {
            super(itemView);
            image_0 = (ImageView) itemView.findViewById(R.id.thread_image_0);
            threadName = (TextView) itemView.findViewById(R.id.thread_name);
            threadTripcode = (TextView) itemView.findViewById(R.id.thread_tripcode);
            threadNo = (TextView) itemView.findViewById(R.id.thread_post_no);
            id = (TextView) itemView.findViewById(R.id.thread_id);
            threadSub = (TextView) itemView.findViewById(R.id.thread_sub_text);
            threadCom = (TextView) itemView.findViewById(R.id.thread_com_text);
            threadEmail = (TextView) itemView.findViewById(R.id.thread_email);
            threadTime = (TextView) itemView.findViewById(R.id.thread_time);
            threadReplies = (Button) itemView.findViewById(R.id.thread_view_replies_button);
            videoPlayButton = (ImageView) itemView.findViewById(R.id.thread_video_play_button);
            threadReplyButton = (Button) itemView.findViewById(R.id.thread_submit_reply_button);

            threadReplies.setOnClickListener(this);
            image_0.setOnClickListener(this);
            videoPlayButton.setOnClickListener(this);
            threadReplyButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.thread_view_replies_button:{
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    CardDialogFragment cardDialogFragment = CardDialogFragment.showReplies(threadNo.getText().toString(), boardName);
                    fragmentTransaction.add(R.id.placeholder_card_dialog, cardDialogFragment)
                            .addToBackStack("threadDialog")
                            .commit();
                    break;
                }
            }
        }
    }
}
