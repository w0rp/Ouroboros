package com.luorrak.ouroboros.util;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.async.http.body.StringPart;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.JsonParser;
import com.luorrak.ouroboros.reply.ReplyCommentFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private boolean needDNSBLCaptcha = false;
    private boolean genericCaptcha = false;
    private Reply reply;
    public void postReply(final Context context, Reply reply, final SharedPreferences sharedPreferences,
                          final JsonParser jsonParser, final InfiniteDbHelper infiniteDbHelper){
        this.reply = reply;
        String postUrl = ChanUrls.getReplyUrl();
        String referalUrl = ChanUrls.getThreadHtmlUrl(reply.board, reply.resto);

        if (needDNSBLCaptcha) {
            ArrayList<Part> dnsblparameters = new ArrayList<Part>();
            needDNSBLCaptcha = false;
            dnsblparameters.add(new StringPart("captcha_text", reply.captchaText));
            dnsblparameters.add(new StringPart("captcha_cookie", reply.captchaCookie));
            reply.captchaText = "";
            reply.captchaCookie = "";
            Ion.with(context)
                    .load(ChanUrls.getDnsblUrl())
                    .addMultipartParts(dnsblparameters)
                    .asString()
                    .tryGet();
            needDNSBLCaptcha = false;
        }
        ArrayList<Part> parameters = new ArrayList<Part>();
        parameters.add(new StringPart("board", reply.board));
        parameters.add(new StringPart("name", reply.name));
        parameters.add(new StringPart("email", reply.email));
        parameters.add(new StringPart("subject", reply.subject));
        parameters.add(new StringPart("body", reply.comment));
        parameters.add(new StringPart("password", reply.password));
        parameters.add(new StringPart("json_response", "1"));
        parameters.add(new StringPart("captcha_text", reply.captchaText));
        parameters.add(new StringPart("captcha_cookie", reply.captchaCookie));
        if (reply.resto.equals("0")) {
            parameters.add(new StringPart("page", "1"));
            parameters.add(new StringPart("post", "New Topic"));
        } else {
            parameters.add(new StringPart("post", "New Reply"));
            parameters.add(new StringPart("thread", reply.resto)); //only if new thread else nothing
        }

        if (reply.filePath != null){
            for (int i = 0; i < reply.filePath.size(); i++){
                parameters.add(new FilePart("file", new File(reply.filePath.get(i))));
            }
        }

        if (genericCaptcha) {
            genericCaptcha = false;
        }

        Ion.with(context)
                .load(postUrl)
                .setHeader("Referer", referalUrl)
                .addMultipartParts(parameters)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> jsonObjectResponse) {
                        ReplyCommentFragment.finishedPosting();
                        String boardName;
                        String userPostNo;
                        if (e != null){
                            Toast toast = Toast.makeText(context, "Data did NOT post successfully", Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
                        if (jsonObjectResponse.getHeaders().code() == 200 ){
                            captchaTest(jsonObjectResponse.getResult());
                            if (needDNSBLCaptcha){
                                Toast toast = Toast.makeText(context, "Please fill out DNSBL Captcha", Toast.LENGTH_SHORT);
                                toast.show();

                                EditText captchaText = (EditText) ((Activity) context).findViewById(R.id.post_comment_captcha_editText); //messy
                                captchaText.setVisibility(View.VISIBLE);

                                getDnsblCaptcha(context);
                                return;
                            } else if (genericCaptcha){
                                Toast toast = Toast.makeText(context, "Please fill out Captcha", Toast.LENGTH_SHORT);
                                toast.show();

                                EditText captchaText = (EditText) ((Activity) context).findViewById(R.id.post_comment_captcha_editText); //messy
                                captchaText.setVisibility(View.VISIBLE);

                                getCaptcha(context);
                                return;
                            } else if (jsonObjectResponse.getResult().has("error")){
                                //unknown error
                                Toast toast = Toast.makeText(context, jsonObjectResponse.getResult().get("error").getAsString(), Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            }

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
                            //There is a crash here sometimes.
                            Log.e(LOG_TAG, "Failed Post " + jsonObjectResponse.getHeaders().message() + " code " + jsonObjectResponse.getHeaders().code());
                            Toast toast = Toast.makeText(context, "Data did NOT post successfully", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    }
                });
    }

    private void getCaptcha(final Context context) {
        Ion.with(context)
                .load(ChanUrls.getCaptchaEntrypoint())
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String rawHTML) {
                        if (e != null){
                            Toast toast = Toast.makeText(context, "Error retrieving CAPTCHA", Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
                        ImageView captchaImage = (ImageView) ((Activity) context).findViewById(R.id.post_comment_captcha_image); //messy
                        captchaImage.setVisibility(View.VISIBLE);
                        Document captchaHtml = Jsoup.parse(rawHTML);

                        Pattern cookieIdPattern = Pattern.compile("CAPTCHA ID: (?:(?!<).)*");
                        Matcher matcher = cookieIdPattern.matcher(rawHTML);

                        captchaImage.setTag(matcher.find() ? matcher.group(0).substring(12) : "");
                        String strBase64Image = captchaHtml.select("body > img").attr("src");
                        String strBase64ImageBytes = strBase64Image.substring(strBase64Image.indexOf(",") + 1);
                        byte[] decodedbytes = Base64.decode(strBase64ImageBytes, Base64.DEFAULT);
                        Bitmap decodeImage = BitmapFactory.decodeByteArray(decodedbytes, 0, decodedbytes.length);
                        captchaImage.setImageBitmap(decodeImage);
                    }
                });
    }

    public String captchaTest(JsonObject jsonObject) {
        JsonElement error = jsonObject.get("error");

        if (error == null){
            return null;
        } else if (error.getAsString().contains("dnsbls_bypass.php")){
            needDNSBLCaptcha = true;
            return "needDNSBLCaptcha";
        } else if (error.getAsString().contains("entrypoint.php")){
            genericCaptcha = true;
            return "Captcha";
        } else {
            return null;
        }
    }

    public void getDnsblCaptcha(final Context context){
        Ion.with(context)
                .load(ChanUrls.getDnsblUrl())
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String rawHTML) {
                        if (e != null){
                            Toast toast = Toast.makeText(context, "Error retrieving DNSBL CAPTCHA", Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }

                        ImageView captchaImage = (ImageView) ((Activity) context).findViewById(R.id.post_comment_captcha_image); //messy
                        captchaImage.setVisibility(View.VISIBLE);

                        Document dnsblHtml = Jsoup.parse(rawHTML);
                        captchaImage.setTag(dnsblHtml.select("body > form > input.captcha_cookie").attr("value"));
                        String strBase64Image = dnsblHtml.select("body > form > img").attr("src");
                        String strBase64ImageBytes = strBase64Image.substring(strBase64Image.indexOf(",") + 1);
                        byte[] decodedbytes = Base64.decode(strBase64ImageBytes, Base64.DEFAULT);
                        Bitmap decodeImage = BitmapFactory.decodeByteArray(decodedbytes, 0, decodedbytes.length);
                        captchaImage.setImageBitmap(decodeImage);
                    }
                });
    }

    public void getImageNoCrossfade(ImageView imageView, String imageUrl) {
        Ion.with(imageView)
                .load(imageUrl)
                .withBitmapInfo();
    }

    public void downloadFile(String boardName, String tim, String ext, Context context){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(ChanUrls.getImageUrl(boardName, tim, ext)));
        request.setDescription(tim + ext);
        request.setTitle(tim + ext);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, tim + ext);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}
