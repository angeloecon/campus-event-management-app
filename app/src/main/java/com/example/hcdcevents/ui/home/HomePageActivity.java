package com.example.hcdcevents.ui.home;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hcdcevents.R;
import com.example.hcdcevents.data.model.StudentCache;
import com.example.hcdcevents.feature.auth.SignInActivity;
import com.example.hcdcevents.feature.events.CreateEvent;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hcdcevents.databinding.ActivityHomePageBinding;
import com.google.firebase.auth.FirebaseAuth;


public class HomePageActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomePageBinding binding;
    private TextView nameTextView, emailTextView;
    private ImageView drawableTextView;

    @Override
    protected void onResume(){
        super.onResume();
        checkAdminStatusForFAB();
        updateUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarHomePage.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View headerView = navigationView.getHeaderView(0);

        nameTextView = headerView.findViewById(R.id.nameTextView);
        emailTextView = headerView.findViewById(R.id.emailTextView);
        drawableTextView = headerView.findViewById(R.id.imageView);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_about)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home_page);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_log_out) {
                logOut(drawer);
                return true;
            } else {
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (handled) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                return handled;
            }
        });
    }

    private void updateUI(){
        String headerName = StudentCache.getCurrentName();
        String headerEmail = StudentCache.getCurrentEmail();
        Drawable profileIcon = StudentCache.getcurrentProfileDrawable();

        if (nameTextView != null) {
            nameTextView.setText(headerName != null ? headerName : "Student");
        }
        if (emailTextView != null) {
            emailTextView.setText(headerEmail != null ? headerEmail : "Guest");
        }

        if (drawableTextView != null) {
            if (profileIcon != null) {
                drawableTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        drawableTextView.setImageDrawable(profileIcon);
                        drawableTextView.invalidate();
                    }
                });
            } else {
                drawableTextView.setImageResource(R.drawable.ic_default_profile_placeholder);
            }
        }
    }
    private void logOut(DrawerLayout drawer){
        StudentCache.clearCache();
        FirebaseAuth.getInstance().signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(HomePageActivity.this, gso);
        googleSignInClient.signOut()
                .addOnCompleteListener(task -> Log.d("SignOut", "Google client cleanup complete."));

        Toast.makeText(HomePageActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
        Intent logoutIntent = new Intent(HomePageActivity.this, SignInActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logoutIntent);
        finish();
        drawer.closeDrawer(GravityCompat.START);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home_page);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void checkAdminStatusForFAB(){
        boolean isAdmin = StudentCache.isCurrentIsAdmin();
        if(isAdmin){
            Toast.makeText(this, "You are an admin", Toast.LENGTH_SHORT).show();
            binding.appBarHomePage.fab.setVisibility(View.VISIBLE);
            binding.appBarHomePage.fab.setOnClickListener(view -> {
                Toast.makeText(HomePageActivity.this, "Preparing to add a new event...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomePageActivity.this, CreateEvent.class));
            });
        } else {
            binding.appBarHomePage.fab.setVisibility(View.GONE);
        }
    }

}