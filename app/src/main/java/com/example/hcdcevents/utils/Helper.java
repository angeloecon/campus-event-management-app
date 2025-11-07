package com.example.hcdcevents.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
    private static final String TAG = "SocialMediaUtils";
    private static final String EMAIL_REGEX = "^[\\w.-]+@hcdc\\.edu\\.ph$";
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

    private Helper() {}
    public static boolean isValidHCDCEmail(String emailInput){
        if(emailInput == null || emailInput.isEmpty()){
            return true;
        }
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailInput);
        return !matcher.matches();
    }

    public static void programDropdown(AutoCompleteTextView courseSelect, Context context, int layoutID){
        List<String> courses = Arrays.asList(
                "COLLEGE OF CRIMINAL JUSTICE EDUCATION",
                "COLLEGE OF ENGINEERING AND TECHNOLOGY",
                "COLLEGE OF HOSPITALITY AND TOURISM",
                "COLLEGE OF HUMANITIES, SOCIAL SCIENCES AND COMMUNICATION",
                "COLLEGE OF MARITIME EDUCATION",
                "SCHOOL OF BUSINESS AND MANAGEMENT EDUCATION",
                "SCHOOL OF TEACHER EDUCATION"
        );

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(context, layoutID, courses);
        courseSelect.setAdapter(courseAdapter);
    }

    public static Drawable iconGenerator(String userName){
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(userName);

        String firstLetter = "?";
        if (userName != null && !userName.isEmpty()) {
            firstLetter = String.valueOf(userName.charAt(0)).toUpperCase();
        }

        return TextDrawable.builder()
                .beginConfig()
                .width(90)
                .height(90)
                .endConfig()
                .buildRound(firstLetter, color);
    }

    public static void redirectSocialMedia(Context context, String socialMediaUrl, String appPackageName){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(socialMediaUrl));
            intent.setPackage(appPackageName);

            if(intent.resolveActivity(context.getPackageManager()) != null){
                context.startActivity(intent);
                return;
            }
        } catch (Exception e){
            Log.e(TAG, "App not found, falling back to browser", e);
        }


        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(socialMediaUrl));

        try {
            context.startActivity(webIntent);
        } catch (Exception e){
            Log.e(TAG, "Could not open or found browser",e);
            Toast.makeText(context, "Cannot find suitable application", Toast.LENGTH_SHORT).show();
        }
    }
}
