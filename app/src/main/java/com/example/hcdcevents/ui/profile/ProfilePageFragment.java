package com.example.hcdcevents.ui.profile;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hcdcevents.R;
import com.example.hcdcevents.data.model.StudentCache;
import com.example.hcdcevents.databinding.FragmentProfileBinding;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePageFragment extends Fragment {
    private ProfilePageViewModel galleryViewModel;
    private CircleImageView profileImage;
    private FragmentProfileBinding binding;
    private TextView profileName, profileEmail, profileProgram;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
         galleryViewModel =
                new ViewModelProvider(this).get(ProfilePageViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        profileImage = root.findViewById(R.id.profile_image);
        profileName = root.findViewById(R.id.textStudentName);
        profileEmail = root.findViewById(R.id.text_student_email);
        profileProgram = root.findViewById(R.id.text_student_program);


        return root;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        updateUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void updateUI() {
        profileImage.setImageDrawable(StudentCache.getcurrentProfileDrawable());
        profileName.setText(StudentCache.getCurrentName());
        profileEmail.setText(StudentCache.getCurrentEmail());
        profileProgram.setText(StudentCache.getCurrentCourse());
    }
}