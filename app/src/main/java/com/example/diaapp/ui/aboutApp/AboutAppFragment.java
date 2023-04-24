package com.example.diaapp.ui.aboutApp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.diaapp.R;

public class AboutAppFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_about_app, container, false);
        //final TextView textView = root.findViewById(R.id.text_slideshow);

        return root;
    }
}