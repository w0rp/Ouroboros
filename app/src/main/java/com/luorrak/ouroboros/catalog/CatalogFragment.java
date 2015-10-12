package com.luorrak.ouroboros.catalog;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.ProgressBar;

import com.google.gson.JsonArray;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.reply.ReplyCommentActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
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

public class CatalogFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // Construction ////////////////////////////////////////////////////////////////////////////////

    private final String LOG_TAG = CatalogFragment.class.getSimpleName();
    private CatalogAdapter catalogAdapter;
    private GridLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String boardName = null;
    private InfiniteDbHelper infiniteDbHelper;
    private CatalogNetworkFragment networkFragment;
    private ActionProvider shareActionProvider;

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
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.catalogList);
        layoutManager = new GridLayoutManager(getActivity(), Util.getCatalogColumns(getActivity()));
        recyclerView.setLayoutManager(layoutManager);

        //if not first load
        if (savedInstanceState != null){
            Parcelable savedLayoutState = savedInstanceState.getParcelable("SavedLayout");
            recyclerView.getLayoutManager().onRestoreInstanceState(savedLayoutState);
            boardName = savedInstanceState.getString("boardName");
            setHasOptionsMenu(true);
        } else {
            infiniteDbHelper.deleteCatalogCache();
            if (getArguments() != null){
                boardName = getArguments().getString("boardName");
                setHasOptionsMenu(true);
            }
            getCatalogJson(getActivity(), boardName);
        }

        if (boardName != null) {
            setActionBarTitle("/" + boardName + "/");
        }
        networkFragment = (CatalogNetworkFragment) getFragmentManager().findFragmentByTag("Catalog_Task");
        if (networkFragment == null) {
            networkFragment = new CatalogNetworkFragment();
            getFragmentManager().beginTransaction().add(networkFragment, "Catalog_Task").commit();
        }

        catalogAdapter = new CatalogAdapter(
                infiniteDbHelper.getCatalogCursor(),
                getFragmentManager(),
                boardName, infiniteDbHelper);
        recyclerView.setAdapter(catalogAdapter);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.catalog_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    // Options Menu ////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem replyButton = menu.findItem(R.id.action_reply);
        MenuItem openExternalButton = menu.findItem(R.id.action_external_browser);
        MenuItem shareButton = menu.findItem(R.id.menu_item_share);

        replyButton.setVisible(true);
        openExternalButton.setVisible(true);
        shareButton.setVisible(true);
        shareActionProvider = MenuItemCompat.getActionProvider(shareButton);

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
                Intent intent =  new Intent(getActivity(), ReplyCommentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(CatalogAdapter.THREAD_NO, resto);
                intent.putExtra(CatalogAdapter.BOARD_NAME, boardName);
                getActivity().startActivity(intent);
                break;
            }
            case R.id.action_external_browser: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ChanUrls.getCatalogUrlExternal(boardName)));
                startActivity(browserIntent);
                break;
            }
            case R.id.menu_item_share: {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = ChanUrls.getCatalogUrlExternal(boardName);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setActionBarTitle(String title){
        getActivity().setTitle(title);
    }

    // Life Cycle //////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("SavedLayout", layoutManager.onSaveInstanceState());
        outState.putString("boardName", boardName);
        super.onSaveInstanceState(outState);
    }

    //https://stackoverflow.com/questions/27057449/when-switch-fragment-with-swiperefreshlayout-during-refreshing-fragment-freezes
    @Override
    public void onPause() {
        super.onPause();

        if (swipeRefreshLayout!=null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.destroyDrawingCache();
            swipeRefreshLayout.clearAnimation();
        }
    }

    @Override
    public void onDestroy() {
        if (networkFragment != null){
            if (networkFragment.getStatus() == AsyncTask.Status.FINISHED){
                networkFragment.cancelTask();
            }
        }
        super.onDestroy();
    }

    // Loading Data ////////////////////////////////////////////////////////////////////////////////
    private void getCatalogJson(final Context context, final String boardName) {
        String catalogJsonUrl = ChanUrls.getCatalogUrl(boardName);
        final ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        Ion.with(context)
                .load(catalogJsonUrl)
                .setLogging(LOG_TAG, Log.DEBUG)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray jsonArray) {
                        if (e == null) {
                            networkFragment.beginTask(jsonArray, infiniteDbHelper, boardName, catalogAdapter);
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            swipeRefreshLayout.setRefreshing(false);
                            Snackbar.make(getView(), "Error retrieving catalog", Snackbar.LENGTH_LONG).show();
                        }

                        catalogAdapter.changeCursor(infiniteDbHelper.getCatalogCursor());
                    }
                });
    }

    @Override
    public void onRefresh() {
        if (boardName != null){
            getCatalogJson(getActivity(), boardName);
        }
    }
}

