package com.luorrak.ouroboros.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.luorrak.ouroboros.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
}
