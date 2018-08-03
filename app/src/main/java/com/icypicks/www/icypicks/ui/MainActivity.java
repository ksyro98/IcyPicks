package com.icypicks.www.icypicks.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
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

public class MainActivity extends AppCompatActivity {
    private ArrayList<IceCream> iceCreams;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private boolean isLoggedIn;
    private boolean hasAccount = false;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private User user;
    private AllIceCreamAdapter allIceCreamAdapter;
    private MustTryIceCreamAdapter mustTryIceCreamAdapter;
    private int numberOfUploads;

    public static final String INFO_FILE_NAME = "info.txt";
    private static final int REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String USER = "user";
    public static final String FILE_NAME = "image_file_name.txt";
    private Fragment fragment;

    @BindView(R.id.fragment_container)
    FrameLayout container;

    @BindView(R.id.share_fab)
    FloatingActionButton shareFab;

    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //TODO change this
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

        fragment = new AllFragment();
        ((AllFragment) fragment).setAllIceCreamAdapter(allIceCreamAdapter);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit();

        shareFab.setOnClickListener(view->{
            Intent intent = new Intent(this, ShareActivity.class);
            intent.putExtra(LogInSignUpActivity.USER_INTENT, user);
            Log.d(TAG, String.valueOf(user == null));
            intent.putExtra(ShareActivity.UPLOAD_NUMBER_INTENT, numberOfUploads);
            startActivity(intent);
        });

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

            Log.d(TAG, databaseReference.toString());
            Log.d(TAG, currentUser.getUid());

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    Log.d(TAG, String.valueOf(user == null));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, databaseError.toString());
                }
            });
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
            Log.d(TAG, "see_profile");
            if (!isLoggedIn){
                Log.d(TAG, "see_profile in if");
                Intent intent = new Intent(this, LogInSignUpActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
            else{
                Log.d(TAG, "see_profile in else");
                Intent intent = new Intent(this, ProfileActivity.class);
                final User[] user = new User[1];
                databaseReference = database.getReference().child(USER).child(currentUser.getUid());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "see_profile in onDataChange 1");
                        user[0] = dataSnapshot.getValue(User.class);
                        intent.putExtra(ProfileActivity.INTENT_USER_EXTRA, user[0]);
                        startActivity(intent);
                        Log.d(TAG, "see_profile in onDataChange 2");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.error_reading_database), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "see_profile in onCancelled");
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
//                                Bitmap bitmap = (Bitmap) data.getParcelableExtra(LogInSignUpActivity.PROFILE_IMAGE_INTENT);
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
                                //code was taken from stack overflow post: https://stackoverflow.com/questions/4989182/converting-java-bitmap-to-byte-array#4989543
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
                               Log.d(TAG, String.valueOf(isLoggedIn));
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
}

//TODO list:
//widget
//accessibility
//rotations
//signed api
//app crashes when user log ins and then tries to add something to the SQLite database
//progress bar
//delete from SQLite    (almost done, when something is deleted the fragment isn't refreshed)
//change image from button
//add list of posts

//TODO message Nick to tell him about Cant-communicate.txt
