package com.icypicks.www.icypicks.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.icypicks.www.icypicks.java_classes.IceCream;
import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.java_classes.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * This is the MainActivity of the app.
 * It contains 2 fragments one for all the ice crams and one for the ice creams in the must-try list
 * and the user can navigate from one to another using a bottom navigation bar.
 * In addition in this activity the user authentication is being done.
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private boolean isLoggedIn;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private User user;
    private AllIceCreamAdapter allIceCreamAdapter;
    private int numberOfUploads = -1;

    public static final String INFO_FILE_NAME = "info.txt";
    private static final int REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String USER = "user";
    public static final String FILE_NAME = "image_file_name.txt";
    private static final String FRAGMENT_SELECTION_KEY = "saved_instance_fragment_selection_key";

    private Fragment fragment;
    private Toast loadingToast;

    @BindView(R.id.fragment_container)
    FrameLayout container;

    @BindView(R.id.share_fab)
    FloatingActionButton shareFab;

    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigationView;

    @BindView(R.id.layout_container)
    ConstraintLayout layoutContainer;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        allIceCreamAdapter = new AllIceCreamAdapter(this, numberOfUploads);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem->{

            switch (menuItem.getItemId()){
                case R.id.all:
                    fragment = new AllFragment();
                   ((AllFragment) fragment).setAllIceCreamAdapter(allIceCreamAdapter);
                    break;
                case R.id.must_try:
                    fragment = new MustTryFragment();
                    break;
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            return true;
        });

        if(savedInstanceState != null && savedInstanceState.containsKey(FRAGMENT_SELECTION_KEY)){
            if(savedInstanceState.getBoolean(FRAGMENT_SELECTION_KEY)){
                fragment = new AllFragment();
                ((AllFragment) fragment).setAllIceCreamAdapter(allIceCreamAdapter);
            }
            else{
                fragment = new MustTryFragment();
            }
        }
        else {
            fragment = new AllFragment();
            ((AllFragment) fragment).setAllIceCreamAdapter(allIceCreamAdapter);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit();

        shareFab.setOnClickListener(view->{
            Intent intent = new Intent(this, ShareActivity.class);
            intent.putExtra(LogInSignUpActivity.USER_INTENT, user);
            intent.putExtra(ShareActivity.UPLOAD_NUMBER_INTENT, numberOfUploads);
            startActivity(intent);
        });

        hideUI();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child("numberOfUploads");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numberOfUploads = Integer.parseInt(String.valueOf(dataSnapshot.getValue()));
                if(allIceCreamAdapter != null) {
                    allIceCreamAdapter.setNumberOfImages(numberOfUploads);
                }
                showUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = auth.getCurrentUser();
        isLoggedIn = (currentUser != null);
        if(isLoggedIn) {
            databaseReference = database.getReference(USER).child(currentUser.getUid());

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, databaseError.toString());
                }
            });

            if(fragment instanceof MustTryFragment){
                ((MustTryFragment) fragment).loadData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.see_profile) {
            if (!isLoggedIn){
                Intent intent = new Intent(this, LogInSignUpActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
            else{
                Intent intent = new Intent(this, ProfileActivity.class);
                final User[] user = new User[1];
                databaseReference = database.getReference().child(USER).child(currentUser.getUid());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user[0] = dataSnapshot.getValue(User.class);
                        intent.putExtra(ProfileActivity.INTENT_USER_EXTRA, user[0]);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideUI();
                    }
                });
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            String email = data.getStringExtra(LogInSignUpActivity.EMAIL_INTENT);
            String password = data.getStringExtra(LogInSignUpActivity.PASSWORD_INTENT);
            File infoFile = new File(this.getCacheDir(), INFO_FILE_NAME);
            boolean hasAccount = false;
            try {
                FileReader fileReader = new FileReader(infoFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                hasAccount = Boolean.parseBoolean(bufferedReader.readLine());
                bufferedReader.close();
                fileReader.close();
            }
            catch (IOException e){
                e.printStackTrace();
                hasAccount = false;
            }
            if(!hasAccount) {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                currentUser = auth.getCurrentUser();
                                isLoggedIn = currentUser != null;

                                try {
                                    FileWriter fileWriter = new FileWriter(infoFile, false);
                                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                                    bufferedWriter.write("true");
                                    bufferedWriter.newLine();
                                    bufferedWriter.close();
                                    fileWriter.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                User user = data.getParcelableExtra(LogInSignUpActivity.USER_INTENT);
                                databaseReference = database.getReference("user").child(currentUser.getUid());
                                databaseReference.setValue(user);

                                StorageReference userStorageReference = FirebaseStorage.getInstance().getReference().child("users").child(currentUser.getUid()).child("profile_image.jpg");
                                Bitmap bitmap = null;
                                try {
                                    bitmap = BitmapFactory.decodeStream(this.openFileInput(FILE_NAME));
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    userStorageReference.putBytes(stream.toByteArray());
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, R.string.error_uploading_image, Toast.LENGTH_SHORT).show();
                                }
                                File file = new File(this.getCacheDir(), FILE_NAME);
                                file.delete();
                                loadUiOnLogIn();
                            }
                            else {
                                Log.d(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(this, R.string.error_signing_up, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else{
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                           if(task.isSuccessful()){
                               Log.d(TAG, "signInWithEmail:success");
                               currentUser = auth.getCurrentUser();
                               isLoggedIn = currentUser != null;
                               loadUiOnLogIn();
                           }
                           else{
                               currentUser = null;
                               isLoggedIn = false;
                               Log.d(TAG, "signInWithEmail:failure", task.getException());
                               Toast.makeText(this, R.string.log_in_error, Toast.LENGTH_SHORT).show();
                           }
                        });
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(fragment instanceof AllFragment) {
            outState.putBoolean(FRAGMENT_SELECTION_KEY, true);
        }
        else{
            outState.putBoolean(FRAGMENT_SELECTION_KEY, false);
        }
    }

    private void loadUiOnLogIn(){
        currentUser = auth.getCurrentUser();
        databaseReference = database.getReference("numberOfUploads");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numberOfUploads = Integer.parseInt(dataSnapshot.getValue(String.class));
                allIceCreamAdapter.setNumberOfImages(numberOfUploads);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.toString());
            }
        });
        showUI();
    }

    private void showUI(){
        layoutContainer.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        loadingToast = null;
    }

    private void hideUI(){
        layoutContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        loadingToast = Toast.makeText(this, R.string.loading_toast, Toast.LENGTH_LONG);
        //if the ui is hidden for 3 seconds, show a warning message.
        new Handler().postDelayed(() -> runOnUiThread(() -> {
            if(loadingToast != null) {
                loadingToast.show();
            }
        }), 30000);
    }
}