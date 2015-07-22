package com.luorrak.ouroboros.reply;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.async.http.body.FilePart;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.JsonParser;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.Reply;
import com.luorrak.ouroboros.util.SaveReplyText;

import java.util.ArrayList;
import java.util.Random;

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
public class ReplyCommentFragment extends Fragment {
    private static boolean isPosting;
    String resto;
    String boardName;
    String replyNo;
    ArrayList<String> attachments;
    SharedPreferences sharedPreferences;
    NetworkHelper networkHelper;
    final int FILE_SELECT_CODE = 1;
    Reply reply;

    public ReplyCommentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        attachments = new ArrayList<String>();
        isPosting = false;
        View view = inflater.inflate(R.layout.fragment_post_comment_activity, container, false);
        setActionBarTitle("Post a comment");

        networkHelper = new NetworkHelper();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        resto = getActivity().getIntent().getStringExtra(CatalogAdapter.THREAD_NO);
        boardName = getActivity().getIntent().getStringExtra(CatalogAdapter.BOARD_NAME);
        replyNo = getActivity().getIntent().getStringExtra(CatalogAdapter.REPLY_NO);

        EditText nameText = (EditText) view.findViewById(R.id.post_comment_editText_name);
        EditText emailText = (EditText) view.findViewById(R.id.post_comment_editText_email);
        EditText subjetText = (EditText) view.findViewById(R.id.post_comment_editText_subject);
        EditText commentText = (EditText) view.findViewById(R.id.post_comment_editText_comment);

        nameText.setText(sharedPreferences.getString(SaveReplyText.nameEditTextKey, ""));
        emailText.setText(sharedPreferences.getString(SaveReplyText.emailEditTextKey, ""));
        subjetText.setText(sharedPreferences.getString(SaveReplyText.subjectEditTextKey, ""));
        commentText.setText(sharedPreferences.getString(SaveReplyText.commentEditTextKey, ""));

        nameText.addTextChangedListener(new SaveReplyText(sharedPreferences, SaveReplyText.nameEditTextKey));
        emailText.addTextChangedListener(new SaveReplyText(sharedPreferences, SaveReplyText.emailEditTextKey));
        subjetText.addTextChangedListener(new SaveReplyText(sharedPreferences, SaveReplyText.subjectEditTextKey));
        commentText.addTextChangedListener(new SaveReplyText(sharedPreferences, SaveReplyText.commentEditTextKey));

        if (replyNo != null){
            if (commentText.getText().toString().equals("")){
                commentText.append(">>" + replyNo + "\n");
            } else {
                commentText.append("\n>>" + replyNo + "\n");
            }
        }
        
        commentText.requestFocus();

        setHasOptionsMenu(true);
        return view;
    }

    public void setActionBarTitle(String title){
        getActivity().setTitle(title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_post_comment_activity, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_attach_file){
            boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            if (isKitKat) {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,FILE_SELECT_CODE);

            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent,FILE_SELECT_CODE);
            }
        }
        if (id == R.id.action_submit && !isPosting){
            isPosting = true;
            EditText nameText = (EditText) getActivity().findViewById(R.id.post_comment_editText_name);
            EditText emailText = (EditText) getActivity().findViewById(R.id.post_comment_editText_email);
            EditText subjectText = (EditText) getActivity().findViewById(R.id.post_comment_editText_subject);
            EditText commentText = (EditText) getActivity().findViewById(R.id.post_comment_editText_comment);
            EditText captchaText = (EditText) getActivity().findViewById(R.id.post_comment_captcha_editText);
            ImageView captchaImage = (ImageView) getActivity().findViewById(R.id.post_comment_captcha_image);

            Random random = new Random();
            reply = new Reply();

            reply.name = nameText.getText().toString();
            reply.email = emailText.getText().toString();
            reply.subject = subjectText.getText().toString();
            reply.comment = commentText.getText().toString();
            reply.captchaText = captchaText.getText().toString();

            if (captchaImage.getTag() != null){
                reply.captchaCookie = captchaImage.getTag().toString();
            }
            reply.resto = resto;
            reply.board = boardName;
            ArrayList<FilePart> fileParts = new ArrayList<FilePart>(); //for later

            reply.password = Long.toHexString(random.nextLong());

            //Add networking call to post data.
            networkHelper.postReply(getActivity(), reply, sharedPreferences, new JsonParser(), new InfiniteDbHelper(getActivity()));
        }
        return super.onOptionsItemSelected(item);
    }

    public static void finishedPosting(){
        isPosting = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (attachments.size() < 1){
                    reply.filePath.add(data.getData().getPath());
                    reply.fileName.add(data.getData().getLastPathSegment());
                } else {
                    Toast.makeText(getActivity(), "Only one file can be uploaded right now.", Toast.LENGTH_SHORT);
                }
            }
        }
    }
}
