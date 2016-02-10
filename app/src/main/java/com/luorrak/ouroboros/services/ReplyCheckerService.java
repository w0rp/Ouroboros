package com.luorrak.ouroboros.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.JsonParser;
import com.luorrak.ouroboros.catalog.CatalogActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.Util;

import java.util.concurrent.ExecutionException;

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

public class ReplyCheckerService extends IntentService {
    NotificationManager notificationManager;

    public ReplyCheckerService() {
        super(ReplyCheckerService.class.getName());

    }

    @Override
    public void onCreate() {
        super.onCreate();
       notificationManager = (NotificationManager) getSystemService(getApplicationContext()
                .NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        InfiniteDbHelper infiniteDbHelper = new InfiniteDbHelper(getApplicationContext());
        Cursor userPostsCursor = infiniteDbHelper.getUserPostsCursor();
        Log.d("ReplyService", DatabaseUtils.dumpCursorToString(userPostsCursor));
        Cursor repliesCursor;
        String userPostBoardName;
        String userPostResto;
        String userPostNo;
        int userReplyCount;
        int userPostErrorCount;
        int replyCount = 0;

        String oldResto = "";
        if((userPostsCursor != null) && (userPostsCursor.getCount() > 0)){
            do {
                String userPostRowId = String.valueOf(userPostsCursor.getInt(userPostsCursor.getColumnIndex(DbContract.UserPosts._ID)));
                userPostBoardName = userPostsCursor.getString(userPostsCursor.getColumnIndex(DbContract.UserPosts.COLUMN_BOARDS));
                userPostResto = userPostsCursor.getString(userPostsCursor.getColumnIndex(DbContract.UserPosts.COLUMN_RESTO));
                userPostNo = userPostsCursor.getString(userPostsCursor.getColumnIndex(DbContract.UserPosts.COLUMN_NO));
                userReplyCount = userPostsCursor.getInt(userPostsCursor.getColumnIndex(DbContract.UserPosts.COLUMN_NUMBER_OF_REPLIES));
                userPostErrorCount = userPostsCursor.getInt(userPostsCursor.getColumnIndex(DbContract.UserPosts.COLUMN_ERROR_COUNT));

                //No change in thread.
                // TODO: 2/7/16 resto 0 new thread
                if (!userPostResto.equals(oldResto)){
                    getThreadJson(userPostBoardName, userPostResto, infiniteDbHelper, userPostRowId, userPostErrorCount);
                    oldResto = userPostResto;
                }
                repliesCursor = infiniteDbHelper.getRCReplies(userPostNo);
                int threadReplyCount = repliesCursor.getCount();
                repliesCursor.close();

                if (threadReplyCount > userReplyCount) {
                    replyCount++;
                    infiniteDbHelper.updateUserPostReplyCount(threadReplyCount, userPostRowId);
                    infiniteDbHelper.addUserPostFlag(userPostRowId);
                }
            }while (userPostsCursor.moveToNext());
            userPostsCursor.close();
            infiniteDbHelper.deleteRCCache();

            if (replyCount > 0){
                createNotification(String.valueOf(replyCount));
            }
        }
    }

    private void getThreadJson(final String userPostBoardName, String userPostResto, final InfiniteDbHelper infiniteDbHelper, String userPostRowId, int userPostErrorCount) {
        JsonObject jsonObject = null;
        try {
            jsonObject = Ion.with(getApplicationContext())
                    .load(ChanUrls.getThreadUrl(userPostBoardName, userPostResto))
                    .setLogging("ReplyService", Log.DEBUG)
                    .asJsonObject().get();
        } catch (InterruptedException e) {
            userPostPrune(infiniteDbHelper, userPostRowId, userPostErrorCount);
        } catch (ExecutionException e) {
            userPostPrune(infiniteDbHelper, userPostRowId, userPostErrorCount);
        }
        if (jsonObject != null){
            insertRCIntoDatabase(jsonObject, userPostBoardName, infiniteDbHelper);
            infiniteDbHelper.updateUserPostErrorCount(userPostRowId, 0);
        } else {
            userPostPrune(infiniteDbHelper, userPostRowId, userPostErrorCount);
        }
    }

    private void userPostPrune(InfiniteDbHelper infiniteDbHelper, String userPostRowId, int userPostErrorCount){
        if(userPostErrorCount == 2){
            infiniteDbHelper.deleteUserPostsEntry(userPostRowId);
        } else {
            infiniteDbHelper.updateUserPostErrorCount(userPostRowId, userPostErrorCount + 1);
        }
    }

    private void insertRCIntoDatabase(JsonObject jsonObject, String userPostBoardName, InfiniteDbHelper infiniteDbHelper) {
        JsonParser jsonParser = new JsonParser();
        JsonArray posts = jsonObject.getAsJsonArray("posts");
        for (JsonElement postElement : posts) {
            JsonObject post = postElement.getAsJsonObject();
            infiniteDbHelper.insertRCEntry(
                    userPostBoardName,
                    jsonParser.getThreadResto(post),
                    jsonParser.getThreadNo(post),
                    jsonParser.getThreadSub(post),
                    jsonParser.getThreadCom(post),
                    jsonParser.getThreadEmail(post),
                    jsonParser.getThreadName(post),
                    jsonParser.getThreadTrip(post),
                    jsonParser.getThreadTime(post),
                    jsonParser.getThreadLastModified(post),
                    jsonParser.getThreadId(post),
                    jsonParser.getThreadEmbed(post),
                    jsonParser.getMediaFiles(post)
            );
        }
    }

    private void createNotification(String replyCount) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.white_ouroboros)
                .setColor(getApplicationContext().getResources().getColor(R.color.md_green_500))
                .setContentTitle(replyCount + " New Post Replied To")
                .setContentText("Click here to go see");

        Intent resultIntent = new Intent(this, CatalogActivity.class);
        resultIntent.putExtra(Util.INTENT_REPLY_CHECKER, true);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notificationBuilder.setContentIntent(resultPendingIntent);
        notificationManager.notify(1438, notificationBuilder.build());
    }
}
