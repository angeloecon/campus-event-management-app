package com.example.hcdcevents.data.model;
import android.graphics.drawable.Drawable;
public class StudentCache {
    private static String currentName = null;
    private static String currentEmail = null;
    private static String currentProgram= null;
    private static String currentKey = null;
    private static boolean currentIsAdmin;
    private static Drawable currentProfileDrawable;

    public static void setStudentData(String name, String email, String program, boolean isAdmin,String key, Drawable profileDrawable){
        currentName = name;
        currentEmail = email;
        currentProgram = program;
        currentIsAdmin = isAdmin;
        currentKey = key;
        currentProfileDrawable = profileDrawable;
    }

    public static void clearCache(){
        currentProgram = null;
        currentName = null;
        currentEmail = null;
        currentIsAdmin = false;
        currentProfileDrawable = null;
    }

    public static String getCurrentName() {
        return currentName;
    }

    public static String getCurrentEmail() {
        return currentEmail;
    }

    public static String getCurrentCourse() {
        return currentProgram;
    }

    public static boolean isCurrentIsAdmin() {
        return currentIsAdmin;
    }
    public static void setCurrentCourse(String currentCourse) {
        StudentCache.currentProgram = currentCourse;
    }
    public static Drawable getcurrentProfileDrawable() {
        return currentProfileDrawable;
    }

    public static void setCurrentProfileDrawable(Drawable currentProfileDrawable) {
        StudentCache.currentProfileDrawable = currentProfileDrawable;
    }

    public static String getCurrentKey() {
        return currentKey;
    }
}
