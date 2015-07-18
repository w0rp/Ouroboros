package com.luorrak.ouroboros.catalog;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;
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
public class NavigationBoardListAdapter extends CursorRecyclerAdapter {
    InfiniteDbHelper infiniteDbHelper;
    private FragmentManager fragmentManager;
    Context context;
    public NavigationBoardListAdapter(Cursor cursor, FragmentManager fragmentManager, Context context) {
        super(cursor);
        this.fragmentManager = fragmentManager;
        this.context = context;
        infiniteDbHelper = new InfiniteDbHelper(context);
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        NavigationBoardListViewHolder navigationBoardListViewHolder = (NavigationBoardListViewHolder) holder;
        String boardName = cursor.getString(cursor.getColumnIndex(DbContract.BoardEntry.COLUMN_BOARDS));
        navigationBoardListViewHolder.boardNameBtn.setText("/" + boardName + "/");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_list_item, parent, false);
        return new NavigationBoardListViewHolder(view);
    }

    class NavigationBoardListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public Button boardNameBtn;
        public Button deleteItemBtn;

        public NavigationBoardListViewHolder(View itemView) {
            super(itemView);
            boardNameBtn = (Button) itemView.findViewById(R.id.boardlist_boardname_button);
            deleteItemBtn = (Button) itemView.findViewById(R.id.boardlist_delete_button);
            boardNameBtn.setAllCaps(false);
            boardNameBtn.setOnClickListener(this);
            deleteItemBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.boardlist_boardname_button:{
                    String buttonText = boardNameBtn.getText().toString();
                    CatalogFragment catalogFragment = new CatalogFragment().newInstance(buttonText.substring(1, buttonText.length()-1));
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.activity_catalog_fragment_container, catalogFragment).commit();
                    break;
                }
                case R.id.boardlist_delete_button:{
                    final String buttonText = boardNameBtn.getText().toString();
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("Remove Board")
                            .setMessage("Are you sure you want to remove board: " + buttonText.substring(1, buttonText.length() - 1) + "?")
                            .setPositiveButton("Delete Board",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            //Add board to database, should consider doing some error checking as well.
                                            InfiniteDbHelper infiniteDbHelper = new InfiniteDbHelper(context);
                                            infiniteDbHelper.deleteBoardEntry(buttonText.substring(1, buttonText.length() - 1));
                                            changeCursor(infiniteDbHelper.getBoardCursor());
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
            }
        }
    }
}
