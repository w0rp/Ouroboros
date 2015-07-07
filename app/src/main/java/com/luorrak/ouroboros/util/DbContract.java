package com.luorrak.ouroboros.util;

import android.provider.BaseColumns;

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
public class DbContract {

    public static final class BoardEntry implements BaseColumns{
        public static final String TABLE_NAME = "boards";

        //name of the board submitted by user
        public static final String COLUMN_BOARDS = "board_name";
    }

    public static final class CatalogEntry implements BaseColumns{
        public static final String TABLE_NAME = "catalog";

        //foreign key from table boardEntry
        public static final String COLUMN_BOARD_NAME = "board_name";

        /*
        Post Number CATALOG
        type: String
         */
        public static final String COLUMN_CATALOG_NO = "no";
        /*
        original file name
        type: String
         */
        public static final String COLUMN_CATALOG_FILENAME = "filename";
        /*
        Renamed filename
        type: String
         */
        public static final String COLUMN_CATALOG_TIM = "tim";
        /*
        filename extension
        type: String
         */
        public static final String COLUMN_CATALOG_EXT = "ext";
        /*
        Thread Subtitle
        type: String
         */
        public static final String COLUMN_CATALOG_SUB = "sub";
        /*
        comment text
        type: String
         */
        public static final String COLUMN_CATALOG_COM = "com";
        /*
        number of replies
        type: Integer
         */
        public static final String COLUMN_CATALOG_REPLIES = "replies";
        /*
        number of images
        type: Integer
         */
        public static final String COLUMN_CATALOG_IMAGES = "images";
        /*
        Is thread stickied
        type: Integer
         */
        public static final String COLUMN_CATALOG_STICKY = "sticky";
        /*
        Is thread locked
        type: Integer
         */
        public static final String COLUMN_CATALOG_LOCKED = "locked";

        public static final String COLUMN_CATALOG_EMBED = "embed";

    }

    public static final class ThreadEntry implements BaseColumns{

        public static final String TABLE_NAME = "thread";

        /*
        foreign key from table boards
        Type: String
         */
        public static final String COLUMN_BOARD_NAME = "board_name";
        /*
        OP post people are replying to
        Type: String
         */
        public static final String COLUMN_THREAD_RESTO = "resto";
        /*
        post number
        Type: String
         */
        public static final String COLUMN_THREAD_NO = "no";
        /*
        original filename
        WARNING: can be null
        Type: String
         */
        public static final String COLUMN_THREAD_FILENAME = "filename";
        /*
        renamed filename
        WARNING: can be null
        WARNING: WILL RETURN STRING ARRAY
        Type: String
         */
        public static final String COLUMN_THREAD_TIMS = "tims";
        /*
        filename extensions
        WARNING: can be null
        WARNING: WILL RETURN STRING ARRAY
        Type: String
         */
        public static final String COLUMN_THREAD_EXTS = "exts";

        public static final String COLUMN_THREAD_IMAGE_HEIGHT = "image_01_height";

        public static final String COLUMN_THREAD_IMAGE_WIDTH = "image_01_width";
        /*
        Post Subtitle
        WARNING: should only appear on OP else return null
        Type: String
         */
        public static final String COLUMN_THREAD_SUB = "sub";
        /*
        post comment
        WARNING: will be filled with html special characters
        Type: String
         */
        public static final String COLUMN_THREAD_COM = "com";
        /*
        post email
        WARNING: Can have any type of text
        Type: String
        */
        public static final String COLUMN_THREAD_EMAIL = "email";
        /*
        Name of poster
        WARNING: can be null
        Type: String
         */
        public static final String COLUMN_THREAD_NAME = "name";
        /*
        trip of poster
        WARNING: can be null
        Type: String
         */
        public static final String COLUMN_THREAD_TRIP = "trip";
        /*
        What time the post was submitted
        Type: String
         */
        public static final String COLUMN_THREAD_TIME = "time";
        /*
        last time the post was modified
        Type: String
         */
        public static final String COLUMN_THREAD_LAST_MODIFIED = "last_modified";
        /*
        poster id
        WARNING: may be null
        Type: String
         */
        public static final String COLUMN_THREAD_ID = "id";

        public static final String COLUMN_THREAD_EMBED = "embed";

    }

    public static final class UserPosts implements BaseColumns{
        public static final String TABLE_NAME = "userposts";

        //name of the board
        public static final String COLUMN_BOARDS = "board_name";

        public static final String COLUMN_NO = "user_post_no";
    }
}
