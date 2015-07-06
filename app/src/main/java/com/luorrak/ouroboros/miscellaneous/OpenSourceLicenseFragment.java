package com.luorrak.ouroboros.miscellaneous;


import android.app.Fragment;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luorrak.ouroboros.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

public class OpenSourceLicenseFragment extends Fragment {


    public OpenSourceLicenseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_open_source_license, container, false);
        Log.d("Trying", "License fragment loaded");
        AssetManager am = getActivity().getAssets();
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(am.open("license.txt")));
            String line;

            while ((line = br.readLine()) != null){
                text.append(line);
                text.append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextView textView = (TextView) view.findViewById(R.id.LicenseTextView);
        textView.setText(text);
        return view;
    }

}
