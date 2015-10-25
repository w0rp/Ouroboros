package com.luorrak.ouroboros.catalog;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.CommentParser;
import com.luorrak.ouroboros.thread.ThreadActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.Util;

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
public class CatalogAdapter extends CursorRecyclerAdapter implements Filterable {
    private final String LOG_TAG = CatalogAdapter.class.getSimpleName();
    public final static String THREAD_NO = "com.luorrak.ouroboros.THREADNO";
    public final static String BOARD_NAME = "com.luorrak.ouroboros.BOARDNAME";
    public final static String REPLY_NO = "com.luorrak.ouroboros.REPLYNO";
    public final static String TIM = "com.luorrak.ouroboros.TIM";
    public final static String EXT = "com.luorrak.ouroboros.EXT";

    private NetworkHelper networkHelper = new NetworkHelper();
    private CommentParser commentParser = new CommentParser();
    private String boardName;
    private InfiniteDbHelper infiniteDbHelper;
    private Context context;
    public CatalogAdapter(Cursor cursor, String boardName, InfiniteDbHelper infiniteDbHelper, Context context) {
        super(cursor);
        this.boardName = boardName;
        this.infiniteDbHelper = infiniteDbHelper;
        this.context = context;
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        String imageUrl;
        String[] youtubeData = {null, null};

        CatalogViewHolder catalogViewHolder = (CatalogViewHolder)holder;

        String sub = cursor.getString(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_SUB));
        String com = cursor.getString(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_COM));
        String tim = cursor.getString(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_TIM));
        String replyCount = String.valueOf(cursor.getInt(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_REPLIES)));
        String imageReplyCount = String.valueOf(cursor.getInt(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_IMAGES)));
        String embed = cursor.getString(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_EMBED));

        if (sub != null){
            catalogViewHolder.catalogSubText.setVisibility(View.VISIBLE);
            catalogViewHolder.catalogSubText.setText(sub);
        } else {
            catalogViewHolder.catalogSubText.setVisibility(View.GONE);
        }

        if (com != null){
            catalogViewHolder.catalogComText.setVisibility(View.VISIBLE);
            catalogViewHolder.catalogComText.setText(commentParser.parseCom(
                    com,
                    "v",
                    "-1", //dummy data as link is unclickable
                    null,
                    infiniteDbHelper
            ));
        } else {
            catalogViewHolder.catalogComText.setVisibility(View.GONE);
        }

        if (getItemViewType(cursor.getPosition()) == Util.CATALOG_LAYOUT_LIST){
            replyCount = replyCount + " Replies";
            imageReplyCount = imageReplyCount + " Images";
        }

        catalogViewHolder.replyCount.setText(replyCount);
        catalogViewHolder.imageReplyCount.setText(imageReplyCount);

        //Prevent's bad requests to the server
        if (tim != null){
                imageUrl = ChanUrls.getThumbnailUrl(boardName, tim);
                networkHelper.getImageNoCrossfade(catalogViewHolder.catalog_picture, imageUrl);
        } else if (embed != null){
            youtubeData = Util.parseYoutube(embed);
            imageUrl = "https://" + youtubeData[1];
            networkHelper.getImageNoCrossfade(catalogViewHolder.catalog_picture, imageUrl);
        } else {
            catalogViewHolder.catalog_picture.setImageDrawable(null);
        }

        //HIDDEN TAG ON COM TEXT TO HACK THREAD NUMBER INTO VIEW
        catalogViewHolder.catalogComText.setTag(cursor.getString(cursor.getColumnIndex(DbContract.CatalogEntry.COLUMN_CATALOG_NO)));
    }

    @Override
    public int getItemViewType(int position) {
        return Util.getCatalogView(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            default:
                case Util.CATALOG_LAYOUT_GRID: {
                    return new CatalogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_grid_item, parent, false));
                }
                case Util.CATALOG_LAYOUT_LIST: {
                    return new CatalogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_list_item, parent, false));
                }
        }
    }


    class CatalogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView catalogSubText;
        public TextView catalogComText;
        public TextView replyCount;
        public TextView imageReplyCount;
        public ImageView catalog_picture;

        public CatalogViewHolder(View itemView) {
            super(itemView);
            catalogSubText = (TextView) itemView.findViewById(R.id.catalog_sub_text);
            catalogComText = (TextView) itemView.findViewById(R.id.catalog_com_text);
            replyCount = (TextView) itemView.findViewById(R.id.catalog_reply_count);
            imageReplyCount = (TextView) itemView.findViewById(R.id.catalog_image_reply_count);
            catalog_picture = (ImageView) itemView.findViewById(R.id.catalog_picture);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = new Intent(context, ThreadActivity.class);
            String threadNo = (String) catalogComText.getTag();
            intent.putExtra(THREAD_NO, threadNo);
            intent.putExtra(BOARD_NAME, boardName);
            context.startActivity(intent);
        }
    }
}
