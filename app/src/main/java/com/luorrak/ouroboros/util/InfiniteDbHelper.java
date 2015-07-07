package com.luorrak.ouroboros.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.luorrak.ouroboros.util.DbContract.BoardEntry;
import com.luorrak.ouroboros.util.DbContract.CatalogEntry;
import com.luorrak.ouroboros.util.DbContract.ThreadEntry;
import com.luorrak.ouroboros.util.DbContract.UserPosts;

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
public class InfiniteDbHelper extends SQLiteOpenHelper{

    private final String LOG_TAG = InfiniteDbHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "cache.db";
    SQLiteDatabase db = getWritableDatabase();

    // Constructor /////////////////////////////////////////////////////////////////////////////////

    public InfiniteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    // Catalog Helper Functions ////////////////////////////////////////////////////////////////////

    public boolean insertCatalogEntry (String board, String no, String filename, String tim, String ext, String sub, String comment,
                                       Integer replies, Integer images, Integer sticky, Integer locked, String embed){
        long newRowId;

        ContentValues values = new ContentValues();
        values.put(CatalogEntry.COLUMN_BOARD_NAME, board); //LOOK UP KEY FOR THIS
        values.put(CatalogEntry.COLUMN_CATALOG_NO, no);
        values.put(CatalogEntry.COLUMN_CATALOG_FILENAME, filename);
        values.put(CatalogEntry.COLUMN_CATALOG_TIM, tim);
        values.put(CatalogEntry.COLUMN_CATALOG_EXT, ext);
        values.put(CatalogEntry.COLUMN_CATALOG_SUB, sub);
        values.put(CatalogEntry.COLUMN_CATALOG_COM, comment);
        values.put(CatalogEntry.COLUMN_CATALOG_REPLIES, replies);
        values.put(CatalogEntry.COLUMN_CATALOG_IMAGES, images);
        values.put(CatalogEntry.COLUMN_CATALOG_STICKY, sticky);
        values.put(CatalogEntry.COLUMN_CATALOG_LOCKED, locked);
        values.put(CatalogEntry.COLUMN_CATALOG_EMBED, embed);

        try {
            db.insertOrThrow(
                    CatalogEntry.TABLE_NAME,
                    null,
                    values
            );
            return true;
        } catch (SQLException e){
            Log.e(LOG_TAG, "Error Inserting row into " + CatalogEntry.TABLE_NAME +
                    " NO: " + no);
            return false;
        }
    }

    public Cursor getCatalogCursor(){
        Cursor cursor = db.query(
                CatalogEntry.TABLE_NAME, //table name
                null, //columns to search
                null, //where clause
                null, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );

        cursor.moveToFirst();
        return cursor;
    }

    public Cursor searchCatalogForThread(String searchString) {
        Cursor cursor;
        if (searchString == null || searchString.length() == 0){
            cursor = getCatalogCursor();
        } else {
            cursor = db.query(
                    CatalogEntry.TABLE_NAME, //table name
                    null, //columns to search
                    CatalogEntry.COLUMN_CATALOG_COM + " LIKE ? OR " + CatalogEntry.COLUMN_CATALOG_SUB + " LIKE ?", //where clause
                    new String[] {"%" + searchString + "%", "%" + searchString + "%"}, //where arguements
                    null, //Group by
                    null, //having
                    null,
                    null//orderby
            );
        }
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void deleteCatalogCache(){
        //Delete all rows in table
        db.delete(CatalogEntry.TABLE_NAME, null, null);
    }

    // Thread Helper Functions /////////////////////////////////////////////////////////////////////

    public boolean insertThreadEntry(String board, String resto, String no, String filename, String tims, String exts,
                                     String sub, String com, String email, String name, String trip, String time, String last_modified,
                                     String id, String embed, String imageHeight, String imageWidth){
        long newRowId;

        ContentValues values = new ContentValues();
        values.put(ThreadEntry.COLUMN_BOARD_NAME, board);
        values.put(ThreadEntry.COLUMN_THREAD_RESTO, resto);
        values.put(ThreadEntry.COLUMN_THREAD_NO, no);
        values.put(ThreadEntry.COLUMN_THREAD_FILENAME, filename);
        values.put(ThreadEntry.COLUMN_THREAD_TIMS, tims);
        values.put(ThreadEntry.COLUMN_THREAD_EXTS, exts);
        values.put(ThreadEntry.COLUMN_THREAD_SUB, sub);
        values.put(ThreadEntry.COLUMN_THREAD_COM, com);
        values.put(ThreadEntry.COLUMN_THREAD_EMAIL, email);
        values.put(ThreadEntry.COLUMN_THREAD_NAME, name);
        values.put(ThreadEntry.COLUMN_THREAD_TRIP, trip);
        values.put(ThreadEntry.COLUMN_THREAD_TIME, time);
        values.put(ThreadEntry.COLUMN_THREAD_LAST_MODIFIED, last_modified);
        values.put(ThreadEntry.COLUMN_THREAD_ID, id);
        values.put(ThreadEntry.COLUMN_THREAD_EMBED, embed);
        values.put(ThreadEntry.COLUMN_THREAD_IMAGE_HEIGHT, imageHeight);
        values.put(ThreadEntry.COLUMN_THREAD_IMAGE_WIDTH, imageWidth);

        try {
            db.insertOrThrow(
                    ThreadEntry.TABLE_NAME,
                    null,
                    values
            );
            return true;
        } catch (SQLException e){
            Log.e(LOG_TAG, "Error Inserting row into " + ThreadEntry.TABLE_NAME +
                    " NO: " + no);
            return false;
        }
    }

    public Cursor getThreadCursor(String resto){
        Cursor cursor = db.query(
                ThreadEntry.TABLE_NAME, //table name
                null, //columns to search
                ThreadEntry.COLUMN_THREAD_RESTO + "=?", //where clause
                new String[] {resto}, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );

        cursor.moveToFirst();
        return cursor;
    }

    public void deleteThreadCache(){
        //Delete all rows in table
        db.delete(ThreadEntry.TABLE_NAME, null, null);
    }

    public Cursor getPost(String postNo){
        Cursor cursor = db.query(
                ThreadEntry.TABLE_NAME, //table name
                null, //columns to search
                "no=?", //where clause
                new String[] {postNo}, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );
        return cursor;
    }

    public Cursor getReplies(String postNo) {
        Cursor cursor = db.query(
                ThreadEntry.TABLE_NAME, //table name
                null, //columns to search
                ThreadEntry.COLUMN_THREAD_COM + " LIKE ?", //where clause
                new String[] {"%onclick=\"highlightReply('" + postNo + "%"}, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );
        cursor.moveToFirst();
        return cursor;
    }

    // Board Helper Functions //////////////////////////////////////////////////////////////////////

    public void insertBoardEntry(String board){
        long newRowId;

        ContentValues values = new ContentValues();
        values.put(BoardEntry.COLUMN_BOARDS, board);

        try {
            db.insertOrThrow(
                    BoardEntry.TABLE_NAME,
                    null,
                    values
            );
        } catch (SQLException e){
            Log.e(LOG_TAG, "Error Inserting row into " + BoardEntry.TABLE_NAME);
        }
    }

    public void deleteBoardEntry(String board){
        db.delete(BoardEntry.TABLE_NAME,
                BoardEntry.COLUMN_BOARDS + "=?",
                new String[]{board}
        );
    }

    public Cursor getBoardCursor(){

        Cursor cursor = db.query(
                BoardEntry.TABLE_NAME,
                null,
                null, //selection
                null, //selection args
                null, //group by
                null, //having
                null  //orderby
        );

        cursor.moveToFirst();

        return cursor;
    }

    public String findBoardKey(String board){

        String[] columns = {BoardEntry._ID};

        Cursor cursor = db.query(
                BoardEntry.TABLE_NAME, //table name
                columns, //columns to search
                BoardEntry.COLUMN_BOARDS + " = '" + board + "'", //where clause "Where board in db == board provided"
                null, //Filter if multiple boards
                null, //Group by
                null, //having
                null //orderby
        );

        cursor.moveToFirst();
        String board_key = cursor.getString(
                cursor.getColumnIndex(BoardEntry._ID)
        );
        cursor.close();
        return board_key;
    }

    // User Posts Functions ////////////////////////////////////////////////////////////////////////

    public void insertUserPostEntry(String board, String no){

        ContentValues values = new ContentValues();
        values.put(UserPosts.COLUMN_BOARDS, board);
        values.put(UserPosts.COLUMN_NO, no);

        try {
            db.insertOrThrow(
                    UserPosts.TABLE_NAME,
                    null,
                    values
            );
        } catch (SQLException e){
            Log.e(LOG_TAG, "Error Inserting row into " + UserPosts.TABLE_NAME);
        }
    }

    public boolean isNoUserPost(String boardName, String no) {
       return DatabaseUtils.queryNumEntries(
                db, //Database
                UserPosts.TABLE_NAME, //Table name
                UserPosts.COLUMN_BOARDS + "=? AND " + UserPosts.COLUMN_NO + "=?", //where clause
                new String[] {boardName, no}) > 0; // selection
    }

    // Database Lifecycle Functions ////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_BOARD_TABLE = "CREATE TABLE IF NOT EXISTS " + BoardEntry.TABLE_NAME + " (" +
                BoardEntry._ID + " INTEGER PRIMARY KEY, " +
                BoardEntry.COLUMN_BOARDS + " TEXT UNIQUE NOT NULL);";

        final String SQL_CREATE_CATALOG_TABLE = " CREATE TABLE " + CatalogEntry.TABLE_NAME + " (" +

                CatalogEntry._ID + " INTEGER PRIMARY KEY, " +

                //Name of board and foreign key
                CatalogEntry.COLUMN_BOARD_NAME + " TEXT NOT NULL, " +
                CatalogEntry.COLUMN_CATALOG_NO + " TEXT NOT NULL, " +

                //Clean up null data before it enters the database and enter a string to signify it
                CatalogEntry.COLUMN_CATALOG_FILENAME + " TEXT, " +
                CatalogEntry.COLUMN_CATALOG_TIM + " TEXT, " +
                CatalogEntry.COLUMN_CATALOG_EXT + " TEXT, " +
                CatalogEntry.COLUMN_CATALOG_SUB + " TEXT, " +
                CatalogEntry.COLUMN_CATALOG_COM + " TEXT, " +
                CatalogEntry.COLUMN_CATALOG_REPLIES + " INTEGER NOT NULL, " +
                CatalogEntry.COLUMN_CATALOG_IMAGES + " INTEGER NOT NULL, " +
                CatalogEntry.COLUMN_CATALOG_STICKY + " INTEGER, " +
                CatalogEntry.COLUMN_CATALOG_LOCKED + " INTEGER, " +
                CatalogEntry.COLUMN_CATALOG_EMBED + " TEXT, " +

                //One post per board. No board should have two post 1234567 for example
                " UNIQUE (" + CatalogEntry.COLUMN_CATALOG_NO + ", " + CatalogEntry.COLUMN_BOARD_NAME +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_THREAD_TABLE = "CREATE TABLE " + ThreadEntry.TABLE_NAME + " (" +
                ThreadEntry._ID + " INTEGER PRIMARY KEY, " +

                //Foreign Key for board name
                ThreadEntry.COLUMN_BOARD_NAME + " INTEGER NOT NULL, " +
                ThreadEntry.COLUMN_THREAD_RESTO + " TEXT NOT NULL, " +
                ThreadEntry.COLUMN_THREAD_NO + " TEXT NOT NULL, " +
                ThreadEntry.COLUMN_THREAD_FILENAME + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_TIMS + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_EXTS + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_SUB + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_COM + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_EMAIL + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_NAME + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_TRIP + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_TIME + " TEXT NOT NULL, " +
                ThreadEntry.COLUMN_THREAD_LAST_MODIFIED + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_ID + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_EMBED + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_IMAGE_HEIGHT + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_IMAGE_WIDTH + " TEXT, " +

                //I think this should auto overwrite dup posts if they ever come up
                " UNIQUE (" + ThreadEntry.COLUMN_THREAD_NO + ", " + ThreadEntry.COLUMN_BOARD_NAME +
                ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_USER_POSTS_TABLE = "CREATE TABLE IF NOT EXISTS " + UserPosts.TABLE_NAME + " (" +
                UserPosts._ID + " INTEGER PRIMARY KEY, " +

                UserPosts.COLUMN_BOARDS + " TEXT NOT NULL, " +
                UserPosts.COLUMN_NO + " TEXT NOT NULL);";

        Log.d(LOG_TAG, "SQL STRINGS");
        Log.d(LOG_TAG, SQL_CREATE_BOARD_TABLE);
        Log.d(LOG_TAG, SQL_CREATE_CATALOG_TABLE);
        Log.d(LOG_TAG, SQL_CREATE_THREAD_TABLE);
        db.execSQL(SQL_CREATE_BOARD_TABLE);
        db.execSQL(SQL_CREATE_CATALOG_TABLE);
        db.execSQL(SQL_CREATE_THREAD_TABLE);
        db.execSQL(SQL_CREATE_USER_POSTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CatalogEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ThreadEntry.TABLE_NAME);

        onCreate(db);
    }

}
