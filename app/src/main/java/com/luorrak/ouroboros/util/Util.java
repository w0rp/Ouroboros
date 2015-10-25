package com.luorrak.ouroboros.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.luorrak.ouroboros.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

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
public class Util {
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_DARK = 1;

    public static final int THREAD_LAYOUT_VERTICAL = 0;
    public static final int THREAD_LAYOUT_HORIZONTAL = 1;

    public static final int CATALOG_LAYOUT_GRID = 0;
    public static final int CATALOG_LAYOUT_LIST = 1;


    public static String[] parseYoutube(String embed) {
        String[] youtubeData = new String[2];

        Document doc = Jsoup.parse(embed);
        if (doc.select("a.file").size() > 0){
            youtubeData[0] = doc.select("a.file").first().attr("href");
            youtubeData[1] = doc.select("img.post-image").first().attr("src").substring(2);
        }
        return youtubeData;
    }

    /*Set the theme */
    public static void onActivityCreateSetTheme(Context context, int themeValue)
    {
        switch (themeValue)
        {
            default:
            case THEME_DEFAULT:
                context.setTheme(R.style.AppTheme);
                break;
            case THEME_DARK:
                context.setTheme(R.style.AppThemeDark);
                break;
        }
    }

    public static int getTheme(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String themeValue = sharedPreferences.getString("theme_preference", "0");
        return Integer.valueOf(themeValue);
    }

    public static int getThreadView(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String themeValue = sharedPreferences.getString("thread_view", "0");
        return Integer.valueOf(themeValue);
    }

    public static int getCatalogView(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String themeValue = sharedPreferences.getString("catalog_view", "1");
        return Integer.valueOf(themeValue);
    }

    public static void setCatalogView(Context context, int layoutValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("catalog_view", String.valueOf(layoutValue));
        editor.apply();
    }

    public static int getCatalogColumns(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String themeValue = sharedPreferences.getString("catalog_grid_columns", "3");
        return Integer.valueOf(themeValue);
    }

    public static Media createMediaItem(String height, String width, String tim, String ext){
        Media mediaItem = new Media();
        mediaItem.height = height;
        mediaItem.width = width;
        mediaItem.fileName = tim;
        mediaItem.ext = ext;
        return mediaItem;
    }

    public static byte[] serializeObject (Object object) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            ObjectOutput objectOutput = new ObjectOutputStream(outputStream);
            objectOutput.writeObject(object);
            objectOutput.close();

            byte[] serializedObject = outputStream.toByteArray();
            return serializedObject;
        } catch (IOException e) {
            return null;
        }
    }

    public static Object deserializeObject(byte[] bytes) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object object = inputStream.readObject();
            inputStream.close();

            return object;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static void copyToClipboard(Context context, String text){
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("ComText", text);
        clipboardManager.setPrimaryClip(clipData);
    }
}
