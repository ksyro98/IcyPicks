package com.icypicks.www.icypicks.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.helpers.BitmapUtils;
import com.icypicks.www.icypicks.java_classes.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.widget.Toast.makeText;

public class ProfileActivity extends AppCompatActivity {
    public static final String INTENT_USER_EXTRA = "intent_user_extra_for_profile";
    private static final String TAG = ProfileActivity.class.getSimpleName();
    private static final String FILE_PROVIDER_AUTHORITY = "com.icypicks.www.icypicks.fileprovider";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private String tempPhotoPath;
    private Toast toast;
//    private String value;


    @BindView(R.id.profile_screen_image_view)
    ImageView profileScreenImageView;

    @BindView(R.id.change_profile_image_button)
    Button changeProfileImageButton;

    @BindView(R.id.name_text_view)
    TextView nameTextView;

    @BindView(R.id.favorite_flavor_text_view)
    TextView favoriteFlavorTextView;

    @BindView(R.id.info_text_view)
    TextView infoTextView;

    @BindView(R.id.user_posts_recycler_view)
    RecyclerView userPostsRecyclerView;

    @BindView(R.id.sign_out_text_view)
    TextView signOutTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        toast = makeText(this, R.string.change_value_tip, Toast.LENGTH_SHORT);
        toast.show();

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(INTENT_USER_EXTRA)){
            User user = intent.getParcelableExtra(INTENT_USER_EXTRA);
            if(user != null) {
                nameTextView.setText(user.getName());
                favoriteFlavorTextView.setText(user.getFavoriteFlavor());
                if(user.getInfo() != null) {
                    infoTextView.setText(user.getInfo());
                }
                UserPostAdapter userPostAdapter = new UserPostAdapter(this, user.getUploads());
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                userPostsRecyclerView.setLayoutManager(layoutManager);
                userPostsRecyclerView.setHasFixedSize(false);
                userPostsRecyclerView.setAdapter(userPostAdapter);
            }
            if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                Log.d(TAG, "Not null");
                StorageReference userStorageReference = FirebaseStorage.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile_image.jpg");
                userStorageReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Glide.with(getApplicationContext()).load(uri).into(profileScreenImageView);
                            Log.d(TAG, "Success");
                        })
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            Log.d(TAG, "Failure");
                        });
            }
        }
        else{
            nameTextView.setText(R.string.error_reading_database);
            favoriteFlavorTextView.setText(R.string.error_reading_database);
            infoTextView.setText(R.string.error_reading_database);
        }

        nameTextView.setOnClickListener(view -> getAlertDialog("name", "Name").show());

        favoriteFlavorTextView.setOnClickListener(view -> getAlertDialog("favoriteFlavor", "Favorite Flavor").show());
        //TODO fill fields with user's data

        infoTextView.setOnClickListener(view -> getAlertDialog("info", "About Me").show());

        signOutTextView.setOnClickListener(view -> signOut());

        changeProfileImageButton.setOnClickListener(view -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(takePictureIntent.resolveActivity(getPackageManager()) != null){
                File photoFile = null;
                try{
                    photoFile = BitmapUtils.createTempImageFile(this);
                }
                catch (IOException e){
                    e.printStackTrace();
                }

                if(photoFile != null) {
                    tempPhotoPath = photoFile.getAbsolutePath();

                    Uri photoUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            toast.cancel();
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private AlertDialog.Builder getAlertDialog(String key, String field){
        EditText editText = new EditText(this);
        editText.setHint(field);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.change_dialog_title) + field);
        builder.setView(editText);

        builder.setPositiveButton(getString(R.string.dialog_positive), ((dialog, which) ->{
            if(!"".equals(editText.getText().toString())){
                writeToFirebaseDatabase(key, editText.getText().toString());
                NavUtils.navigateUpFromSameTask(this);
            }
            else{
                Toast.makeText(this, R.string.empty_field_dialog_error, Toast.LENGTH_SHORT).show();
            }
        }));

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            //do nothing
        });

        return builder;
    }

    private void writeToFirebaseDatabase(String key, String value){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            DatabaseReference reference = firebaseDatabase.getReference(MainActivity.FIREBASE_DATABASE_REFERENCE).child(currentUser.getUid()).child(key);
            reference.setValue(value);
        }
    }

    private void signOut(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        Toast.makeText(this, R.string.signed_out_toast, Toast.LENGTH_SHORT).show();
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if((requestCode == REQUEST_IMAGE_CAPTURE) && (resultCode == RESULT_OK)){
            Bitmap resultsBitmap = BitmapUtils.resamplePic(this, tempPhotoPath);
            profileScreenImageView.setImageBitmap(resultsBitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resultsBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            if(FirebaseAuth.getInstance() != null) {
                StorageReference userStorageReference = FirebaseStorage.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid()).child("profile_image.jpg");
                userStorageReference.putBytes(stream.toByteArray());
                Toast.makeText(this, R.string.new_image_info_message, Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            BitmapUtils.deleteImageFile(this, tempPhotoPath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
