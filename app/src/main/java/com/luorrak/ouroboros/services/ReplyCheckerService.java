package com.luorrak.ouroboros.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.JsonParser;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;

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
        Cursor repliesCursor;
        String userPostBoardName;
        String userPostResto;
        String userPostNo;
        int userReplyCount;
        int replyCount = 0;

        String oldResto = "";
        while (userPostsCursor.moveToNext()) {
            userPostBoardName = userPostsCursor.getString(userPostsCursor.getColumnIndex(DbContract.UserPosts.COLUMN_BOARDS));
            userPostResto = userPostsCursor.getString(userPostsCursor.getColumnIndex(DbContract.UserPosts.COLUMN_RESTO));
            userPostNo = userPostsCursor.getString(userPostsCursor.getColumnIndex(DbContract.UserPosts.COLUMN_NO));
            userReplyCount = userPostsCursor.getInt(userPostsCursor.getColumnIndex(DbContract.UserPosts.COLUMN_NUMBER_OF_REPLIES));

            //No change in thread.
            // TODO: 2/7/16 resto 0 new thread
            if (!userPostResto.equals(oldResto)){
                getThreadJson(userPostBoardName, userPostResto, infiniteDbHelper);
            }

            repliesCursor = infiniteDbHelper.getReplies(userPostNo);
            int threadReplyCount = repliesCursor.getCount();
            repliesCursor.close();

            if (threadReplyCount > userReplyCount) {
                replyCount++;
                String userPostRowId = String.valueOf(userPostsCursor.getInt(userPostsCursor.getColumnIndex(DbContract.UserPosts._ID)));
                //// TODO: 2/6/16 remove flag in review activity
                infiniteDbHelper.updateUserPostReplyCount(threadReplyCount, InfiniteDbHelper.trueFlag, userPostRowId);
            }
        }
        userPostsCursor.close();
        infiniteDbHelper.deleteThreadCache();

        if (replyCount > 0){
            createNotification(String.valueOf(replyCount));
        }
    }

    private void getThreadJson(final String userPostBoardName, String userPostResto, final InfiniteDbHelper infiniteDbHelper) {
        JsonObject jsonObject = null;
        try {
            jsonObject = Ion.with(getApplicationContext())
                    .load(ChanUrls.getThreadUrl(userPostBoardName, userPostResto))
                    .setLogging("ReplyService", Log.DEBUG)
                    .asJsonObject().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (jsonObject != null){
            insertThreadIntoDatabase(jsonObject, userPostBoardName, infiniteDbHelper);
        }
    }

    private void insertThreadIntoDatabase(JsonObject jsonObject, String userPostBoardName, InfiniteDbHelper infiniteDbHelper) {
        JsonParser jsonParser = new JsonParser();
        JsonArray posts = jsonObject.getAsJsonArray("posts");
        for (JsonElement postElement : posts) {
            JsonObject post = postElement.getAsJsonObject();
            infiniteDbHelper.insertThreadEntry(
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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(replyCount + " Simple Notification")
                .setContentText("This is a normal notification.");

        //Intent resultIntent = new Intent(this, )
        notificationManager.notify(1438, notificationBuilder.build());
    }
}
