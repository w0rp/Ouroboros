package com.luorrak.ouroboros.thread;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.CommentParser;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.reply.ReplyCommentActivity;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.Media;
import com.luorrak.ouroboros.util.Util;

import java.util.ArrayList;
import java.util.HashMap;


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

    private CommentParser commentParser = new CommentParser();
    private FragmentManager fragmentManager;
    private String boardName;
    private Context context;
    private InfiniteDbHelper infiniteDbHelper;
    private int viewWidth;
    private HashMap<Integer, MediaAdapter> mediaAdapterHashMap;


    public ThreadAdapter(Cursor cursor, FragmentManager fragmentManager, String boardName, Context context) {
        super(cursor);
        this.fragmentManager = fragmentManager;
        this.boardName = boardName;
        this.context = context;
        this.infiniteDbHelper = new InfiniteDbHelper(context);
        mediaAdapterHashMap = new HashMap<Integer, MediaAdapter>();
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, final Cursor cursor) {
        final ThreadViewHolder threadViewHolder = (ThreadViewHolder)holder;

        resetThreadViewHolder(threadViewHolder);
        createThreadObject(threadViewHolder, cursor);

        if (infiniteDbHelper.isNoUserPost(boardName, threadViewHolder.threadObject.no)){
            threadViewHolder.threadObject.name = "(You) " + threadViewHolder.threadObject.name;
        }
        threadViewHolder.threadName.setText(threadViewHolder.threadObject.name);
        threadViewHolder.threadName.setVisibility(View.VISIBLE);

        threadViewHolder.threadTripcode.setText(threadViewHolder.threadObject.tripcode);
        threadViewHolder.threadTripcode.setVisibility(View.VISIBLE);

        threadViewHolder.threadNo.setText(threadViewHolder.threadObject.no);
        threadViewHolder.threadNo.setVisibility(View.VISIBLE);

        if (threadViewHolder.threadObject.id != null){
            threadViewHolder.id.setText(commentParser.parseId(threadViewHolder.threadObject.id));
            threadViewHolder.id.setVisibility(View.VISIBLE);
        }

        if(threadViewHolder.threadObject.sub != null){
            threadViewHolder.threadSub.setText(threadViewHolder.threadObject.sub);
            threadViewHolder.threadSub.setVisibility(View.VISIBLE);
        }

        if (threadViewHolder.threadObject.email != null && threadViewHolder.threadObject.email.equals("sage")){
            threadViewHolder.threadEmail.setVisibility(View.VISIBLE);
        }

        threadViewHolder.threadTime.setText(
                DateUtils.getRelativeTimeSpanString(
                        threadViewHolder.threadObject.threadTime * 1000,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                ));
        threadViewHolder.threadTime.setVisibility(View.VISIBLE);


        // MediaView ///////////////////////////////////////////////////////////////////////////////

        ArrayList<Media> mediaArrayList;
        if (threadViewHolder.threadObject.serializedMediaList != null) {
            threadViewHolder.threadMediaItemRecycler.setVisibility(View.VISIBLE);
            mediaArrayList = (ArrayList<Media>) Util.deserializeObject(threadViewHolder.threadObject.serializedMediaList);
            if (mediaArrayList.size() > 1){
                threadViewHolder.threadMediaItemRecycler.setScrollbarFadingEnabled(false);
            }
        } else {
            threadViewHolder.threadMediaItemRecycler.setVisibility(View.GONE);
            mediaArrayList = new ArrayList<>();
            // Youtube /////////////////////////////////////////////////////////////////////////////
            if (threadViewHolder.threadObject.embed != null){
                createEmbedObject(threadViewHolder);
                if (threadViewHolder.embedObject.dataUrl != null){
                    threadViewHolder.threadEmbed.setVisibility(View.VISIBLE);
                    threadViewHolder.threadEmbedPlayButton.setVisibility(View.VISIBLE);
                    String imageUrl = "https://" + threadViewHolder.embedObject.imageUrl;

                    Ion.with(threadViewHolder.threadEmbed)
                            .load(imageUrl)
                            .withBitmapInfo()
                            .setCallback(new FutureCallback<ImageViewBitmapInfo>() {
                                @Override
                                public void onCompleted(Exception e, ImageViewBitmapInfo result) {
                                    if (e != null || result.getBitmapInfo() == null) {
                                        return;
                                    }

                                    if (getItemViewType(cursor.getPosition()) != Util.THREAD_LAYOUT_HORIZONTAL){
                                        Palette.generateAsync(result.getBitmapInfo().bitmap,
                                                new Palette.PaletteAsyncListener() {
                                                    @Override
                                                    public void onGenerated(Palette palette) {
                                                        Palette.Swatch vibrant =
                                                                palette.getLightMutedSwatch();
                                                        if (vibrant != null) {
                                                            threadViewHolder.mediaHolder.setBackgroundColor(
                                                                    vibrant.getRgb());
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });

                    final String youtubeVideoUrl = threadViewHolder.embedObject.dataUrl;
                    threadViewHolder.threadEmbedPlayButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeVideoUrl));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    });

                }
            }
            // End Youtube /////////////////////////////////////////////////////////////////////////
        }

        if (!threadViewHolder.threadObject.replyCount.equals("0")){
            threadViewHolder.threadReplies.setVisibility(View.VISIBLE);
            threadViewHolder.threadReplies.setText(threadViewHolder.threadObject.replyCount + " Replies");
        }

        // Create an adapter if none exists
        if (!mediaAdapterHashMap.containsKey(cursor.getPosition())) {
            mediaAdapterHashMap.put(cursor.getPosition(), new MediaAdapter(mediaArrayList, boardName, threadViewHolder.threadObject.resto, fragmentManager, context, viewWidth));
        }

        threadViewHolder.threadMediaItemRecycler.setAdapter(mediaAdapterHashMap.get(cursor.getPosition()));

        // END MediaView ///////////////////////////////////////////////////////////////////////////

        //Does comment exist
        if (threadViewHolder.threadObject.com != null){
            Spannable spannableCom = commentParser.parseCom(threadViewHolder.threadObject.com,
                    boardName,
                    cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_RESTO)),
                    fragmentManager,
                    infiniteDbHelper
            );
            threadViewHolder.threadCom.setVisibility(View.VISIBLE);

            threadViewHolder.threadCom.setText(spannableCom);
        }

        // OnClick /////////////////////////////////////////////////////////////////////////////////

        threadViewHolder.threadCom.setMovementMethod(LinkMovementMethod.getInstance());
        threadViewHolder.threadReplies.setMovementMethod(LinkMovementMethod.getInstance());
        threadViewHolder.threadReplyButton.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public int getItemViewType(int position) {
        return Util.getThreadView(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            default:
            case Util.THREAD_LAYOUT_VERTICAL: {
                ThreadViewHolder threadViewHolder = new ThreadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_thread, parent, false));
                SnappyLinearLayoutManager layoutManager = new SnappyLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                threadViewHolder.threadMediaItemRecycler.setLayoutManager(layoutManager);
                return threadViewHolder;
            }
            case Util.THREAD_LAYOUT_HORIZONTAL: {
                ThreadViewHolder threadViewHolder = new ThreadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_thread_horizontal, parent, false));
                SnappyLinearLayoutManager layoutManager = new SnappyLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                threadViewHolder.threadMediaItemRecycler.setLayoutManager(layoutManager);
                return threadViewHolder;
            }
        }
    }

    // TextHandling ////////////////////////////////////////////////////////////////////////////////


    // Utility /////////////////////////////////////////////////////////////////////////////////////

    private void resetThreadViewHolder(ThreadViewHolder threadViewHolder){
        threadViewHolder.threadName.setVisibility(View.GONE);
        threadViewHolder.threadTripcode.setVisibility(View.GONE);
        threadViewHolder.threadNo.setVisibility(View.GONE);
        threadViewHolder.id.setVisibility(View.GONE);
        threadViewHolder.threadSub.setVisibility(View.GONE);
        threadViewHolder.threadCom.setVisibility(View.GONE);
        threadViewHolder.threadEmail.setVisibility(View.GONE);
        threadViewHolder.threadTime.setVisibility(View.GONE);
        threadViewHolder.threadReplies.setVisibility(View.GONE);
        threadViewHolder.threadEmbed.setVisibility(View.GONE);
        threadViewHolder.threadEmbedPlayButton.setVisibility(View.GONE);
    }

    private String getReplyCount(String no){
        Cursor repliesCursor = infiniteDbHelper.getReplies(no);
        String replies = String.valueOf(repliesCursor.getCount());
        repliesCursor.close();
        return replies;
    }

    private void createThreadObject(ThreadViewHolder threadViewHolder, Cursor cursor){
        threadViewHolder.threadObject.name = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_NAME));
        threadViewHolder.threadObject.tripcode = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_TRIP));
        threadViewHolder.threadObject.no = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_NO));
        threadViewHolder.threadObject.sub = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_SUB));
        threadViewHolder.threadObject.com = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_COM));
        threadViewHolder.threadObject.email = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_EMAIL));
        threadViewHolder.threadObject.threadTime = cursor.getLong(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_TIME));
        threadViewHolder.threadObject.embed = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_EMBED));
        threadViewHolder.threadObject.resto = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_RESTO));
        threadViewHolder.threadObject.id = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_ID));
        threadViewHolder.threadObject.serializedMediaList = cursor.getBlob(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_MEDIA_FILES));
        threadViewHolder.threadObject.replyCount = getReplyCount(threadViewHolder.threadObject.no);
    }

    //This only works for YouTube currently
    private void createEmbedObject(ThreadViewHolder threadViewHolder){
        String[] youtubeData = Util.parseYoutube(threadViewHolder.threadObject.embed);
        threadViewHolder.embedObject.dataUrl = youtubeData[0];
        threadViewHolder.embedObject.imageUrl = youtubeData[1];
    }
    // View Holder /////////////////////////////////////////////////////////////////////////////////

    class ThreadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView threadName;
        public TextView threadTripcode;
        public TextView threadNo;
        public TextView id;
        public TextView threadSub;
        public TextView threadCom;
        public TextView threadEmail;
        public TextView threadTime;
        public Button threadReplies;
        public Button threadReplyButton;
        public ImageView threadEmbed;
        public ImageView threadEmbedPlayButton;
        public FrameLayout mediaHolder;
        public SnappyRecyclerView threadMediaItemRecycler;

        public ThreadObject threadObject;
        public EmbedObject embedObject;

        public ThreadViewHolder(View itemView) {
            super(itemView);

            threadName = (TextView) itemView.findViewById(R.id.thread_name);
            threadTripcode = (TextView) itemView.findViewById(R.id.thread_tripcode);
            threadNo = (TextView) itemView.findViewById(R.id.thread_post_no);
            id = (TextView) itemView.findViewById(R.id.thread_id);
            threadSub = (TextView) itemView.findViewById(R.id.thread_sub_text);
            threadCom = (TextView) itemView.findViewById(R.id.thread_com_text);
            threadEmail = (TextView) itemView.findViewById(R.id.thread_email);
            threadTime = (TextView) itemView.findViewById(R.id.thread_time);
            threadReplies = (Button) itemView.findViewById(R.id.thread_view_replies_button);
            threadReplyButton = (Button) itemView.findViewById(R.id.thread_submit_reply_button);
            threadEmbed = (ImageView) itemView.findViewById(R.id.thread_embed);
            threadEmbedPlayButton = (ImageView) itemView.findViewById(R.id.thread_embed_play_button);
            threadMediaItemRecycler = (SnappyRecyclerView) itemView.findViewById(R.id.thread_media_recycler);
            mediaHolder = (FrameLayout) itemView.findViewById(R.id.thread_image_holder);

            threadObject = new ThreadObject();
            embedObject = new EmbedObject();

            threadReplies.setOnClickListener(this);
            threadReplyButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.thread_view_replies_button:{
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    CardDialogFragment cardDialogFragment = CardDialogFragment.showReplies(threadNo.getText().toString(), boardName);
                    fragmentTransaction.add(R.id.placeholder_card, cardDialogFragment)
                            .addToBackStack("threadDialog")
                            .commit();
                    break;
                }
                case R.id.thread_submit_reply_button:{
                    Log.d(LOG_TAG, "Reply button pressed " + threadObject.no);
                    Intent intent =  new Intent(context, ReplyCommentActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(CatalogAdapter.THREAD_NO, threadObject.resto);
                    intent.putExtra(CatalogAdapter.REPLY_NO, threadObject.no);
                    intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
                    context.startActivity(intent);
                }
            }
        }
    }

    //Cursor object ////////////////////////////////////////////////////////////////////////////////

    class ThreadObject {
        public String name;
        public String tripcode;
        public String no;
        public String sub;
        public String com;
        public String email;
        public long threadTime;
        public String embed;
        public String resto;
        public String id;
        public byte[] serializedMediaList;
        public String replyCount;
    }

    //Embed object /////////////////////////////////////////////////////////////////////////////////

    class  EmbedObject {
        public String imageUrl;
        public String dataUrl;
    }

}
