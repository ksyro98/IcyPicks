package com.icypicks.www.icypicks.ui;

import android.content.Intent;
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
import java.io.File;
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
    private static int numberOfUploads;

    private static final String infoFileName = "info.txt";
    private static final int REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String FIREBASE_DATABASE_REFERENCE = "user";

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
            Fragment iceCreamFragment = null;

            switch (menuItem.getItemId()){
                case R.id.all:
                    iceCreamFragment = new AllFragment();
                   ((AllFragment) iceCreamFragment).setAllIceCreamAdapter(allIceCreamAdapter);
                    break;
                case R.id.must_try:
                    iceCreamFragment = new MustTryFragment();
//                    mustTryIceCreamAdapter = new MustTryIceCreamAdapter();
//                    ((MustTryFragment) iceCreamFragment).setIceCreamAdapter(mustTryIceCreamAdapter);
                    break;
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, iceCreamFragment)
                    .commit();

            return true;
        });

        AllFragment initialFragment = new AllFragment();
        initialFragment.setAllIceCreamAdapter(allIceCreamAdapter);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, initialFragment)
                .commit();

        shareFab.setOnClickListener(view->{
            Intent intent = new Intent(this, ShareActivity.class);
            intent.putExtra(LogInSignUpActivity.USER_INTENT, user);
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
                    allIceCreamAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        Intent intent = new Intent(this, DetailActivity.class);
//        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = auth.getCurrentUser();
        isLoggedIn = (currentUser != null);
        if(currentUser != null) {
            databaseReference = database.getReference(FIREBASE_DATABASE_REFERENCE).child(currentUser.getUid());

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.see_profile) {
            if (!isLoggedIn){// || !hasAccount) {
                try {
                    File file = new File(this.getCacheDir(), infoFileName);
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    hasAccount = Boolean.parseBoolean(bufferedReader.readLine());
                    bufferedReader.close();
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(this, LogInSignUpActivity.class);
                intent.putExtra(LogInSignUpActivity.ACCOUNT_INFO, hasAccount);
                startActivityForResult(intent, REQUEST_CODE);
            }
            else{
                Intent intent = new Intent(this, ProfileActivity.class);
                //TODO put some Extras
                final User[] user = new User[1];
                databaseReference = database.getReference(FIREBASE_DATABASE_REFERENCE).child(currentUser.getUid());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user[0] = dataSnapshot.getValue(User.class);
                        intent.putExtra(ProfileActivity.INTENT_USER_EXTRA, user[0]);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.error_reading_database), Toast.LENGTH_SHORT).show();
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
            if(!hasAccount) {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                currentUser = auth.getCurrentUser();
                                isLoggedIn = currentUser != null;

                                try {
                                    File file = new File(this.getCacheDir(), infoFileName);
                                    FileWriter fileWriter = new FileWriter(file, false);
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
//                               Toast.makeText(this, String.valueOf(currentUser == null), Toast.LENGTH_SHORT).show();
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
