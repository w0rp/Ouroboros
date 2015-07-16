package com.luorrak.ouroboros.catalog;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.settings.SettingsActivity;
import com.luorrak.ouroboros.miscellaneous.OpenSourceLicenseActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

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

/*
   Adopted from
   https://github.com/slidenerd/materialtest
 */
/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationDrawerFragment extends Fragment implements View.OnClickListener {
    private final String LOG_TAG = NavigationDrawerFragment.class.getSimpleName();

    private static final String PREF_FILE_NAME = "navigationpref";
    private static final String KEY_USER_LEARED_DRAWER="user_learned_drawer";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;

    private View containerView;
    private NavigationBoardListAdapter boardListAdapter;


    public NavigationDrawerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        Button addBoard = (Button) view.findViewById(R.id.navigation_button_add_board);
        Button license = (Button) view.findViewById(R.id.navigation_button_licences);
        Button settings= (Button) view.findViewById(R.id.navigation_button_settings);
        addBoard.setOnClickListener(this);
        license.setOnClickListener(this);
        settings.setOnClickListener(this);



        InfiniteDbHelper infiniteDbHelper = new InfiniteDbHelper(getActivity());
        Cursor boardListCursor = infiniteDbHelper.getBoardCursor();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.navigation_boardlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        boardListAdapter = new NavigationBoardListAdapter(boardListCursor, getFragmentManager(), mDrawerLayout, containerView, getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(boardListAdapter);
        return view;
    }


    public void setUp(int fragmentID, DrawerLayout drawerLayout, Toolbar toolbar) {
        containerView=getActivity().findViewById(fragmentID);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(!mUserLearnedDrawer){
                    mUserLearnedDrawer = true;
                    saveToPreferences(getActivity(), KEY_USER_LEARED_DRAWER, "true");
                }
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };
        if(!mUserLearnedDrawer && !mFromSavedInstanceState){
            mDrawerLayout.openDrawer(containerView);
        }
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //make a button on the app bar
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readFromPreferences(getActivity(), KEY_USER_LEARED_DRAWER, "false"));
        if (savedInstanceState != null){
            mFromSavedInstanceState = true;
        }

    }

    public void saveToPreferences(Context context, String preferenceName, String preferenceValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public String readFromPreferences(Context context, String preferenceName, String defaultValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.navigation_button_add_board:{
                Log.d(LOG_TAG, "add board button clicked");
                final EditText edittext = new EditText(getActivity());
                edittext.setInputType(InputType.TYPE_CLASS_TEXT);
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setMessage("New Board")
                        .setTitle("Please enter a board name.")
                        .setView(edittext)
                        .setPositiveButton("Add Board",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //Add board to database, should consider doing some error checking as well.
                                        final String boardName = edittext.getText().toString();
                                        Ion.with(getActivity())
                                                .load(ChanUrls.getCatalogUrl(boardName))
                                                .asString()
                                                .withResponse()
                                                .setCallback(new FutureCallback<Response<String>>() {
                                                    @Override
                                                    public void onCompleted(Exception e, Response<String> stringResponse) {
                                                        if (e != null || stringResponse.getHeaders().code() == 404){
                                                            Toast.makeText(getActivity(), "Server Error! Does board exist?", Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        InfiniteDbHelper infiniteDbHelper = new InfiniteDbHelper(getActivity());
                                                        infiniteDbHelper.insertBoardEntry(boardName);
                                                        boardListAdapter.changeCursor(infiniteDbHelper.getBoardCursor());
                                                    }
                                                });
                                    }
                                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // what ever you want to do with No option.
                            }
                        })
                        .create();
                alertDialog.show();
                break;
            }
            case R.id.navigation_button_licences:{
                Intent intent = new Intent(getActivity(), OpenSourceLicenseActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.navigation_button_settings:{
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}
