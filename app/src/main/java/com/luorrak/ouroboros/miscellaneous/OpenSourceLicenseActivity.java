package com.luorrak.ouroboros.miscellaneous;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.webkit.WebView;

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
public class OpenSourceLicenseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.onActivityCreateSetTheme(this, Util.getTheme(this));
        super.onCreate(savedInstanceState);

        WebView webView = new WebView(this);
        AssetManager am = getAssets();
        webView.loadUrl("file:///android_asset/license.html");

        setContentView(webView);
    }
}
