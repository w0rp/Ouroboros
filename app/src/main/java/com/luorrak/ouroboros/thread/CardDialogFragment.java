package com.luorrak.ouroboros.thread;

import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.util.InfiniteDbHelper;

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
public class CardDialogFragment extends Fragment {
    private final String LOG_TAG = CardDialogFragment.class.getSimpleName();
    private Cursor cursor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        InfiniteDbHelper infiniteDbHelper = new InfiniteDbHelper(getActivity());

        String postNo;
        String repliesPostNo;
        String boardName;

        if(getArguments().getString("postNo") != null){
            postNo = getArguments().getString("postNo");
            boardName = getArguments().getString("boardName");
            cursor = infiniteDbHelper.getPost(postNo);
        } else {
            repliesPostNo = getArguments().getString("repliesPostNo");
            boardName = getArguments().getString("boardName");
            cursor = infiniteDbHelper.getReplies(repliesPostNo);
        }

        View view = inflater.inflate(R.layout.fragment_thread, container, false);
        view.setBackgroundColor(Color.argb(77, 00, 00, 00));

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.postList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        ThreadAdapter threadAdapter = new ThreadAdapter(cursor, getFragmentManager(), boardName, getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(threadAdapter);

        return view;
    }

    //Get thread number from link somehow
    public static CardDialogFragment showPost(String linkUrl, String boardName){
        CardDialogFragment cardDialogFragment = new CardDialogFragment();
        Bundle args = new Bundle();
        String[] url_split = linkUrl.split("#"); // Format /v/res/123456789.html#123456
        String postNo = url_split[1];
        args.putString("postNo", postNo);
        args.putString("boardName", boardName);
        cardDialogFragment.setArguments(args);
        return cardDialogFragment;
    }

    //Get list of replies to post
    public static CardDialogFragment showReplies(String repliesPostNo, String boardName){
        CardDialogFragment cardDialogFragment = new CardDialogFragment();
        Bundle args = new Bundle();
        args.putString("repliesPostNo", repliesPostNo);
        args.putString("boardName", boardName);
        cardDialogFragment.setArguments(args);
        return cardDialogFragment;
    }




}
