package com.luorrak.ouroboros.activities;

import android.content.SharedPreferences;
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

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.Reply;
import com.luorrak.ouroboros.util.SaveReplyText;
import com.koushikdutta.async.http.body.FilePart;

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
public class PostCommentActivityFragment extends Fragment {
    String resto;
    String boardName;
    String replyNo;
    SharedPreferences sharedPreferences;

    public PostCommentActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_comment_activity, container, false);
        setActionBarTitle("Post a comment");

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

        if (id == R.id.action_submit){
            EditText nameText = (EditText) getActivity().findViewById(R.id.post_comment_editText_name);
            EditText emailText = (EditText) getActivity().findViewById(R.id.post_comment_editText_email);
            EditText subjectText = (EditText) getActivity().findViewById(R.id.post_comment_editText_subject);
            EditText commentText = (EditText) getActivity().findViewById(R.id.post_comment_editText_comment);

            Random random = new Random();
            Reply reply = new Reply();

            reply.name = nameText.getText().toString();
            reply.email = emailText.getText().toString();
            reply.subject = subjectText.getText().toString();
            reply.comment = commentText.getText().toString();
            reply.resto = resto;
            reply.board = boardName;
            ArrayList<FilePart> fileParts = new ArrayList<FilePart>(); //for later

            reply.password = Long.toHexString(random.nextLong());

            //Add networking call to post data.
            NetworkHelper networkHelper = new NetworkHelper();
            networkHelper.postReply(getActivity(), reply, sharedPreferences);
        }
        return super.onOptionsItemSelected(item);
    }


}
