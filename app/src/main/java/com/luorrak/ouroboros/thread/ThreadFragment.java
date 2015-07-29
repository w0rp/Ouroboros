package com.luorrak.ouroboros.thread;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.catalog.CatalogAdapter;
import com.luorrak.ouroboros.gallery.GalleryFragment;
import com.luorrak.ouroboros.reply.ReplyCommentActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.NetworkHelper;

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
public class ThreadFragment extends Fragment{
    // Construction ////////////////////////////////////////////////////////////////////////////////
    private final String LOG_TAG = ThreadFragment.class.getSimpleName();
    private InfiniteDbHelper infiniteDbHelper;
    private NetworkHelper networkHelper = new NetworkHelper();
    private RecyclerView recyclerView;
    private ThreadAdapter threadAdapter;
    private LinearLayoutManager layoutManager;
    private ThreadNetworkFragment networkFragment;
    String resto;
    String boardName;
    private Handler handler;

    //Get thread number from link somehow
    public static ThreadFragment newInstance(String resto, String boardName){
        ThreadFragment threadFragment = new ThreadFragment();
        Bundle args = new Bundle();
        args.putString("resto", resto);
        args.putString("boardName", boardName);
        threadFragment.setArguments(args);
        return threadFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutManager = new LinearLayoutManager(getActivity()){
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        if (savedInstanceState != null){
            Parcelable savedLayoutState = savedInstanceState.getParcelable("savedLayout");
            resto = savedInstanceState.getString("resto");
            boardName = getArguments().getString("boardName");
            layoutManager.onRestoreInstanceState(savedLayoutState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        infiniteDbHelper = new InfiniteDbHelper(getActivity());
        networkFragment = (ThreadNetworkFragment) getFragmentManager().findFragmentByTag("Thread_Task");
        View view = inflater.inflate(R.layout.fragment_thread, container, false);
        setHasOptionsMenu(true);
        recyclerView = (RecyclerView) view.findViewById(R.id.postList);

        if (getArguments() != null) {
            resto = getArguments().getString("resto");
            boardName = getArguments().getString("boardName");
        }

        if (networkFragment == null) {
            networkFragment = new ThreadNetworkFragment();
            getFragmentManager().beginTransaction().add(networkFragment, "Thread_Task").commit();
        }

        if (boardName != null){
            getThread(resto, boardName);
            recyclerView.setLayoutManager(layoutManager);
            threadAdapter = new ThreadAdapter(infiniteDbHelper.getThreadCursor(resto), getActivity().getFragmentManager(), boardName, getActivity());
            threadAdapter.setHasStableIds(true);
            threadAdapter.hasStableIds();
            recyclerView.setAdapter(threadAdapter);
        }

        handler = new Handler();
        startStatusCheck();
        return view;
    }

    // Life Cycle //////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onPause() {
        stopStatusCheck();
        super.onPause();
    }

    @Override
    public void onResume() {
        startStatusCheck();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("savedLayout", layoutManager.onSaveInstanceState());
        outState.putString("boardName", boardName);
        outState.putString("resto", resto);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if(savedInstanceState != null)
        {
            Parcelable savedLayoutState = savedInstanceState.getParcelable("savedLayout");
            recyclerView.getLayoutManager().onRestoreInstanceState(savedLayoutState);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    // Options Menu ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem refreshButton = menu.findItem(R.id.action_refresh);
        MenuItem scrollButton = menu.findItem(R.id.action_scroll_bottom);
        MenuItem replyButton = menu.findItem(R.id.action_reply);
        MenuItem galleryButton = menu.findItem(R.id.action_gallery);
        MenuItem saveAllImagesButton = menu.findItem(R.id.action_save_all_images);

        refreshButton.setVisible(true);
        scrollButton.setVisible(true);
        replyButton.setVisible(true);
        galleryButton.setVisible(true);
        saveAllImagesButton.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:{
                getThread(resto, boardName);
                break;
            }
            case R.id.action_scroll_bottom:{
                Log.d(LOG_TAG, "getItemCount " + threadAdapter.getItemCount());
                recyclerView.scrollToPosition(threadAdapter.getItemCount() - 1);
                break;
            }
            case R.id.action_reply:{
                Intent intent =  new Intent(getActivity(), ReplyCommentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(CatalogAdapter.THREAD_NO, resto);
                intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
                getActivity().startActivity(intent);
                break;
            }
            case R.id.action_gallery:{
                GalleryFragment galleryFragment = new GalleryFragment().newInstance(boardName, resto);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.placeholder_card, galleryFragment)
                        .addToBackStack("galleryfragment")
                        .commit();
                break;
            }
            case R.id.action_save_all_images: {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Download All Images")
                        .setMessage("Are you sure you want to download all images?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String tim;
                                String ext;
                                Cursor imageCursor = infiniteDbHelper.getGalleryCursor(resto);
                                do {
                                    tim = imageCursor.getString(imageCursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_TIMS));
                                    ext = imageCursor.getString(imageCursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_EXTS));
                                    networkHelper.downloadFile(boardName, tim, ext, getActivity());

                                } while (imageCursor.moveToNext());

                                imageCursor.close();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setActionBarTitle(String title){
        getActivity().setTitle(title);
    }

    // Loading Data ////////////////////////////////////////////////////////////////////////////////


    public void getThread(String threadNo, String boardName){
        //hacks to get this to work

        getThreadJson(getActivity(), boardName, threadNo);
    }

    public void getThreadJson(final Context context, final String boardName, final String threadNumber){
        ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        final String threadJsonUrl = ChanUrls.getThreadUrl(boardName, threadNumber);

        Log.d(LOG_TAG, "thread json url "+ threadJsonUrl);

        Ion.with(context)
                .load(threadJsonUrl)
                .setLogging(LOG_TAG, Log.DEBUG)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {

                    @Override
                    public void onCompleted(Exception e, JsonObject jsonObject) {

                        if (e == null) {
                            networkFragment.beginTask(jsonObject, infiniteDbHelper, boardName, resto, threadAdapter);
                            //new InsertThreadIntoDatabase().execute(jsonObject);
                        } else {
                            Log.d(LOG_TAG, "Error inserting thred into db");
                        }
                    }
                });
    }

    Runnable statusCheck = new Runnable() {
        @Override
        public void run() {
            getThread(resto, boardName);
            handler.postDelayed(statusCheck, 30000);
        }
    };

    private void startStatusCheck() {
        statusCheck.run();
    }

    private void stopStatusCheck() {
        handler.removeCallbacks(statusCheck);
    }
}
