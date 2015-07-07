package com.luorrak.ouroboros.catalog;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.activities.PostCommentActivity;
import com.luorrak.ouroboros.api.JsonParser;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;


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

public class CatalogFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // Construction ////////////////////////////////////////////////////////////////////////////////

    private final String LOG_TAG = CatalogFragment.class.getSimpleName();
    private CatalogAdapter catalogAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    private String boardName = null;
    private InfiniteDbHelper infiniteDbHelper;


    public CatalogFragment() {
    }

    public CatalogFragment newInstance(String boardName) {
        CatalogFragment frag = new CatalogFragment();
        Bundle args = new Bundle();
        args.putString("boardName", boardName);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        infiniteDbHelper = new InfiniteDbHelper(getActivity());
        infiniteDbHelper.deleteCatalogCache();
        Bundle arguments = getArguments();
        if (savedInstanceState != null){
            boardName = savedInstanceState.getString("boardName");
        }

        if (arguments != null) {
            setHasOptionsMenu(true);
            boardName = getArguments().getString("boardName");
        }
        if (boardName == null){
            return inflater.inflate(R.layout.blank_catalog, container, false);
        }

        View view = inflater.inflate(R.layout.fragment_catalog, container, false);
        setActionBarTitle("/" + boardName + "/");

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.catalogList);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);

        catalogAdapter = new CatalogAdapter(
                infiniteDbHelper.getCatalogCursor(),
                getActivity().getFragmentManager(),
                boardName, infiniteDbHelper);

            recyclerView.setAdapter(catalogAdapter);


        getCatalogJson(getActivity(), boardName);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.catalog_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;

    }

    // Options Menu ////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem replyButton = menu.findItem(R.id.action_reply);
        replyButton.setVisible(true);

        MenuItem searchButton = menu.findItem(R.id.action_search);
        searchButton.setVisible(true);
        SearchView searchView = (SearchView) searchButton.getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(LOG_TAG, "query=" + newText);
                catalogAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        return infiniteDbHelper.searchCatalogForThread(constraint.toString());
                    }
                });
                catalogAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_reply:{
                String resto = "0";
                Intent intent =  new Intent(getActivity(), PostCommentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(CatalogAdapter.THREAD_NO, resto);
                intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
                getActivity().startActivity(intent);
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setActionBarTitle(String title){
        getActivity().setTitle(title);
    }

    // Life Cycle //////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("boardName", boardName);
        super.onSaveInstanceState(outState);
    }

    // Loading Data ////////////////////////////////////////////////////////////////////////////////
    public void getCatalogJson(final Context context, final String boardName) {

        String catalogJsonUrl = ChanUrls.getCatalogUrl(boardName);

        Ion.with(context)
                .load(catalogJsonUrl)
                .setLogging(LOG_TAG, Log.DEBUG)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray jsonElements) {
                        if (e == null) {
                            new InsertCatalogIntoDatabase().execute(jsonElements);
                        } else {
                            Log.d(LOG_TAG, "Error Retrieving Catalog From Server " + e);
                        }

                        catalogAdapter.changeCursor(infiniteDbHelper.getCatalogCursor());
                    }
                });
    }

    public class InsertCatalogIntoDatabase extends AsyncTask<JsonArray, Void, Void> {

        @Override
        protected Void doInBackground(JsonArray... params) {
            JsonParser jsonParser = new JsonParser();
            infiniteDbHelper.deleteCatalogCache();

            for (JsonElement page : params[0]) {
                JsonArray threads = page.getAsJsonObject().getAsJsonArray("threads");
                //loop through each post on the catalog and submit the results to the database for caching.
                for (JsonElement threadElement : threads) {
                    JsonObject thread = threadElement.getAsJsonObject();

                    infiniteDbHelper.insertCatalogEntry(
                            boardName,
                            jsonParser.getCatalogNo(thread),
                            jsonParser.getCatalogFilename(thread),
                            jsonParser.getCatalogTim(thread),
                            jsonParser.getCatalogExt(thread),
                            jsonParser.getCatalogSub(thread),
                            jsonParser.getCatalogCom(thread),
                            jsonParser.getCatalogReplies(thread),
                            jsonParser.getCatalogImageReplyCount(thread),
                            jsonParser.getCatalogSticky(thread),
                            jsonParser.getCatalogLocked(thread),
                            jsonParser.getCatalogEmbed(thread)
                    );
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            catalogAdapter.changeCursor(infiniteDbHelper.getCatalogCursor());
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        getCatalogJson(getActivity(), boardName);
    }

    public void show(FragmentManager fragmentManager, String s) {
    }
}

