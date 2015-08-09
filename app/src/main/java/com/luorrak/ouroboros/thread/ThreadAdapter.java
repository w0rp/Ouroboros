package com.luorrak.ouroboros.thread;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.CommentParser;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.reply.ReplyCommentActivity;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.Media;

import java.util.ArrayList;


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

    CommentParser commentParser = new CommentParser();
    private FragmentManager fragmentManager;
    private String boardName;
    private Context context;
    private InfiniteDbHelper infiniteDbHelper;


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
        final String com;
        String email;
        long threadTime;
        final String embed;
        final String resto;
        //Should make this into an object to make it more obvious
        String[] youtubeData = {null, null}; //youtubeData[0] is video url youtubeData[1] is image url to load for thumbnail

        ThreadViewHolder threadViewHolder = (ThreadViewHolder)holder;

        //Prevent refresh flickering
        threadViewHolder.threadName.setVisibility(View.GONE);
        threadViewHolder.threadTripcode.setVisibility(View.GONE);
        threadViewHolder.threadNo.setVisibility(View.GONE);
        threadViewHolder.id.setVisibility(View.GONE);
        threadViewHolder.threadSub.setVisibility(View.GONE);
        threadViewHolder.threadCom.setVisibility(View.GONE);
        threadViewHolder.threadEmail.setVisibility(View.GONE);
        threadViewHolder.threadTime.setVisibility(View.GONE);
        threadViewHolder.threadReplies.setVisibility(View.GONE);

        name = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_NAME));
        tripcode = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_TRIP));
        no = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_NO));
        sub = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_SUB));
        com = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_COM));
        email = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_EMAIL));
        threadTime = cursor.getLong(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_TIME));
        embed = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_EMBED));
        resto = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_RESTO));
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


        // VIEW PAGER ///////////////////////////////////////////////////////////////////////////////
        //does image exist?


        // END VIEWPAGER ///////////////////////////////////////////////////////////////////////////


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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_thread, parent, false);
        return new ThreadViewHolder(view);
    }

    // TextHandling ////////////////////////////////////////////////////////////////////////////////



    // Utility /////////////////////////////////////////////////////////////////////////////////////



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
            }
        }
    }



    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        byte[] serializedMediaList;
        ArrayList<Media> mediaList;
        String embed;
        String resto;
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fm, byte[] serializedMediaList, ArrayList<Media> mediaList, String embed, String resto) {
            super(fm);
            this.serializedMediaList = serializedMediaList;
            this.mediaList = mediaList;
            this.embed = embed;
            this.resto = resto;
    }

        @Override
        public Fragment getItem(int position) {
            return new MediaViewFragment().newInstance(boardName, position, serializedMediaList, embed, resto);
        }

        @Override
        public int getCount() {
            return mediaList.size();
        }
    }
}
