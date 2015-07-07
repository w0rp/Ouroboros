package com.luorrak.ouroboros.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.luorrak.ouroboros.api.JsonParser;

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
public class NetworkHelper {
    public final String LOG_TAG = NetworkHelper.class.getSimpleName();

    public void postReply(final Context context, Reply reply, final SharedPreferences sharedPreferences,
                          final JsonParser jsonParser, final InfiniteDbHelper infiniteDbHelper){
        String postUrl = ChanUrls.getReplyUrl();
        String referalUrl = ChanUrls.getThreadHtmlUrl(reply.board, reply.resto);

        Log.d(LOG_TAG, "Resto " + reply.resto);
        if (!reply.resto.equals("0")){
            Ion.with(context)
                    .load(postUrl)
                    .setLogging(LOG_TAG, Log.DEBUG)
                    .setHeader("Referer", referalUrl)
                    .setMultipartParameter("name", reply.name)
                    .setMultipartParameter("email", reply.email)
                    .setMultipartParameter("subject", reply.subject)
                    .setMultipartParameter("post", "New Reply")
                    .setMultipartParameter("body", reply.comment)
                    .setMultipartParameter("board", reply.board)
                    .setMultipartParameter("thread", reply.resto) //only if new thread else nothing
                    .setMultipartParameter("password", reply.password)
                    .setMultipartParameter("json_response", "1")
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<JsonObject>>() {
                        @Override
                        public void onCompleted(Exception e, Response<JsonObject> jsonObjectResponse) {
                            String boardName;
                            String userPostNo;

                            if (e != null){
                                Toast toast = Toast.makeText(context, "An error has occured " + e.toString(), Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            }

                            //Temp measure to check for captcha before code is built
                            if (jsonObjectResponse.getHeaders().code() == 200 && !jsonObjectResponse.getResult().toString().contains("CAPTCHA")){
                                Toast toast = Toast.makeText(context, "Data posted successfully", Toast.LENGTH_SHORT);
                                toast.show();

                                boardName = jsonParser.getSubmittedBoardName(jsonObjectResponse.getResult());
                                userPostNo = jsonParser.getUserPostNo(jsonObjectResponse.getResult());

                                infiniteDbHelper.insertUserPostEntry(boardName, userPostNo);

                                sharedPreferences.edit().remove(SaveReplyText.nameEditTextKey)
                                        .remove(SaveReplyText.emailEditTextKey)
                                        .remove(SaveReplyText.subjectEditTextKey)
                                        .remove(SaveReplyText.commentEditTextKey)
                                        .apply();

                                ((Activity) context).finish();
                            } else {
                                Log.d(LOG_TAG, "Failed Post " + jsonObjectResponse.getResult().toString());
                                Toast toast = Toast.makeText(context, "Data did NOT post successfully", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
        } else {
            Ion.with(context)
                    .load(postUrl)
                    .setLogging(LOG_TAG, Log.DEBUG)
                    .setHeader("Referer", referalUrl)
                    .setMultipartParameter("board", reply.board)
                    .setMultipartParameter("page", "1")
                    .setMultipartParameter("name", reply.name)
                    .setMultipartParameter("email", reply.email)
                    .setMultipartParameter("subject", reply.subject)
                    .setMultipartParameter("post", "New Topic")
                    .setMultipartParameter("body", reply.comment)
                    .setMultipartParameter("password", reply.password)
                    .setMultipartParameter("json_response", "1")
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<JsonObject>>() {
                        @Override
                        public void onCompleted(Exception e, Response<JsonObject> jsonObjectResponse) {
                            String boardName;
                            String userPostNo;

                            if (e != null){
                                Toast toast = Toast.makeText(context, "An error has occured " + e.toString(), Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            }

                            //Temp measure to check for captcha before code is built
                            if (jsonObjectResponse.getHeaders().code() == 200 && !jsonObjectResponse.getResult().toString().contains("CAPTCHA")){
                                Toast toast = Toast.makeText(context, "Data posted successfully", Toast.LENGTH_SHORT);
                                toast.show();

                                boardName = jsonParser.getSubmittedBoardName(jsonObjectResponse.getResult());
                                userPostNo = jsonParser.getUserPostNo(jsonObjectResponse.getResult());

                                infiniteDbHelper.insertUserPostEntry(boardName, userPostNo);

                                sharedPreferences.edit().remove(SaveReplyText.nameEditTextKey)
                                        .remove(SaveReplyText.emailEditTextKey)
                                        .remove(SaveReplyText.subjectEditTextKey)
                                        .remove(SaveReplyText.commentEditTextKey)
                                        .apply();

                                ((Activity) context).finish();
                            } else {
                                Log.d(LOG_TAG, "Failed Post " + jsonObjectResponse.getResult().toString());
                                Toast toast = Toast.makeText(context, "Data did NOT post successfully", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
        }

    }

    public void getImageGenric(ImageView imageView, String imageUrl) {
        Ion.with(imageView)
                .smartSize(true)
                .crossfade(true)
                .load(imageUrl)
                .withBitmapInfo();
    }
    public void getImageNoCrossfade(ImageView imageView, String imageUrl) {
        Ion.with(imageView)
                .load(imageUrl)
                .withBitmapInfo();
    }
}
