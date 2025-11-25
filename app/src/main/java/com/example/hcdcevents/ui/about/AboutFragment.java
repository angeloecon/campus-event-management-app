package com.example.hcdcevents.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.hcdcevents.R;
import com.example.hcdcevents.databinding.FragmentAboutBinding;
import com.example.hcdcevents.utils.Helper;

public class AboutFragment extends Fragment {


    private FragmentAboutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        root.findViewById(R.id.facebook_icon).setOnClickListener(v -> {
            Helper.redirectSocialMedia(getContext(), "https://www.facebook.com/formenteraluis.12","com.facebook.katana");
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}