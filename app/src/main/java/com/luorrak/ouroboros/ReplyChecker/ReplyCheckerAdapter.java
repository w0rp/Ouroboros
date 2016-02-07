package com.luorrak.ouroboros.ReplyChecker;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;

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
public class ReplyCheckerAdapter extends CursorRecyclerAdapter{

    public ReplyCheckerAdapter(Cursor cursor) {
        super(cursor);
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        ReplyCheckerViewHolder replyCheckerViewHolder = (ReplyCheckerViewHolder) holder;
        createReplyCheckerObject(replyCheckerViewHolder, cursor);
        replyCheckerViewHolder.rcSubjectText.setText(replyCheckerViewHolder.replyCheckerObject.subject);
        replyCheckerViewHolder.rcCommentText.setText(replyCheckerViewHolder.replyCheckerObject.comment);
        replyCheckerViewHolder.rcReplyCountText.setText(replyCheckerViewHolder.replyCheckerObject.replyCount);
    }


    private void createReplyCheckerObject(ReplyCheckerViewHolder replyCheckerViewHolder, Cursor cursor){
        replyCheckerViewHolder.replyCheckerObject.subject = cursor.getString(cursor.getColumnIndex(DbContract.UserPosts.COLUMN_SUBJECT));
        replyCheckerViewHolder.replyCheckerObject.comment = cursor.getString(cursor.getColumnIndex(DbContract.UserPosts.COLUMN_COMMENT));
        replyCheckerViewHolder.replyCheckerObject.replyCount = cursor.getString(cursor.getColumnIndex(DbContract.UserPosts.COLUMN_NUMBER_OF_REPLIES));
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReplyCheckerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_checker_list_item, parent, false));
    }

    class ReplyCheckerObject {
        public String subject;
        public String comment;
        public String replyCount;
    }

    private class ReplyCheckerViewHolder extends RecyclerView.ViewHolder {
        public TextView rcSubjectText;
        public TextView rcCommentText;
        public TextView rcReplyCountText;
        public ReplyCheckerObject replyCheckerObject;
        public ReplyCheckerViewHolder(View itemView) {
            super(itemView);
            rcSubjectText = (TextView) itemView.findViewById(R.id.reply_checker_sub_text);
            rcCommentText = (TextView) itemView.findViewById(R.id.reply_checker_com_text);
            rcReplyCountText = (TextView) itemView.findViewById(R.id.reply_checker_reply_count);
            replyCheckerObject = new ReplyCheckerObject();
        }
    }
}
