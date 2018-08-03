package com.icypicks.www.icypicks.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.icypicks.www.icypicks.helpers.BitmapUtils;
import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.java_classes.User;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Parts of the code related with the use of camera and the image display were taken from the emojifier app
 */
public class ShareActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PLACE_PICKER = 2;
    private static final String FILE_PROVIDER_AUTHORITY = "com.icypicks.www.icypicks.fileprovider";
    public static final String UPLOAD_NUMBER_INTENT = "upload_number_intent_name";
    private static final String TAG = ShareActivity.class.getSimpleName();
    private static final String BITMAP_KEY = "saved_instance_state_bitmap_key";
    private static final String PLACE_KEY = "saved_instance_state_place_key";
    private static final String BITMAP_FILE_KEY = "saved_instance_state_bitmap_file_key";
    private static final int CAMERA_REQUEST_CODE = 314;

    private String tempPhotoPath;
    private Bitmap resultsBitmap;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth firebaseAuth;
    private String resultPlace;
    private boolean permissionGranted;

    @BindView(R.id.ice_cream_image_view)
    ImageView iceCreamImageView;

    @BindView(R.id.flavor_edit_text)
    EditText flavorEditText;

    @BindView(R.id.description_edit_text)
    EditText descriptionEditText;

    @BindView(R.id.final_share_button)
    Button shareButton;

    @BindView(R.id.location_button)
    Button locationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        ButterKnife.bind(this);

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(BITMAP_KEY)){
                resultsBitmap = savedInstanceState.getParcelable(BITMAP_KEY);
                if(resultsBitmap != null) {
                    iceCreamImageView.setImageBitmap(resultsBitmap);
                }
            }
            if(savedInstanceState.containsKey(PLACE_KEY)){
                resultPlace = savedInstanceState.getString(PLACE_KEY);
            }
            if(savedInstanceState.containsKey(BITMAP_FILE_KEY)){
                tempPhotoPath = savedInstanceState.getString(BITMAP_FILE_KEY);
            }
        }

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        Intent startIntent = getIntent();
        User user = null;
        int uploadNumber = 0;
        if(startIntent != null) {
            user = startIntent.getParcelableExtra(LogInSignUpActivity.USER_INTENT);
            uploadNumber = startIntent.getIntExtra(UPLOAD_NUMBER_INTENT, 0);
        }

        iceCreamImageView.setOnClickListener(view ->{
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

                    getCameraPermission(takePictureIntent);
                }
            }
        });

        User finalUser = user;
        int finalUploadNumber = uploadNumber;

        shareButton.setOnClickListener(view -> {
            String flavor = flavorEditText.getText().toString();
            String description = descriptionEditText.getText().toString();

            if(!"".equals(flavor) && !"".equals(description) && resultsBitmap != null && resultPlace != null) {
                if (finalUser != null && currentUser != null) {
                    StorageReference storageReference = firebaseStorage.getReference().child(String.valueOf(finalUploadNumber+1));
                    StorageReference imageStorageReference = storageReference.child("image.jpg");
                    StorageReference flavorStorageReference = storageReference.child("flavor.txt");
                    StorageReference descriptionStorageReference = storageReference.child("description.txt");
                    StorageReference placeStorageReference = storageReference.child("place.txt");

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    resultsBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] data = byteArrayOutputStream.toByteArray();
                    UploadTask imageUploadTask = imageStorageReference.putBytes(data);
                    imageUploadTask
                            .addOnFailureListener((exception) -> Toast.makeText(this, R.string.image_upload_failure, Toast.LENGTH_SHORT).show())
                            .addOnSuccessListener((taskSnapshot -> {
                                Toast.makeText(this, R.string.image_upload_success, Toast.LENGTH_SHORT).show();
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = firebaseDatabase.getReference("numberOfUploads");
                                databaseReference.setValue(String.valueOf(finalUploadNumber+1));
                            }));

                    try {
                        createFileAndSend(flavor, flavorStorageReference, "flavor");
                        createFileAndSend(description, descriptionStorageReference, "description");
                        createFileAndSend(resultPlace, placeStorageReference, "place");
                    }
                    catch (IOException e){
                        e.printStackTrace();
                        Log.d(TAG, e.toString());
                        Toast.makeText(this, R.string.text_upload_failure, Toast.LENGTH_SHORT).show();
                    }

                    finalUser.addUpload(finalUploadNumber+1);
                    DatabaseReference uploadNumberDatabaseReference = FirebaseDatabase.getInstance().getReference("user").child(currentUser.getUid()).child("uploads");
                    uploadNumberDatabaseReference.setValue(finalUser.getUploads());

                    onBackPressed();
                }
                else{
                    Toast.makeText(this, R.string.not_logged_in_error, Toast.LENGTH_SHORT).show();
                }

                if(tempPhotoPath != null) {
                    BitmapUtils.deleteImageFile(this, tempPhotoPath);
                }
            }
            else{
                Toast.makeText(this, R.string.empty_fields_in_sharing_error, Toast.LENGTH_SHORT).show();
            }
        });


        locationButton.setOnClickListener(view -> {
            locationButton.setEnabled(false);
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            try {
                startActivityForResult(builder.build(this), REQUEST_PLACE_PICKER);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        });

        GoogleApiClient client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if((requestCode == REQUEST_IMAGE_CAPTURE) && (resultCode == RESULT_OK)){
            resultsBitmap = BitmapUtils.resamplePic(this, tempPhotoPath);
            iceCreamImageView.setImageBitmap(resultsBitmap);
        }
        else if((requestCode == REQUEST_PLACE_PICKER) && (resultCode == RESULT_OK)){
            locationButton.setEnabled(true);
            if(data != null) {
                Place place = PlacePicker.getPlace(this, data);
                if(place != null) {
                    resultPlace = String.valueOf(place.getLatLng().latitude) + "_" + String.valueOf(place.getLatLng().longitude);
                }

            }
        }
        else{
            if(tempPhotoPath != null) {
                BitmapUtils.deleteImageFile(this, tempPhotoPath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createFileAndSend(String dataToSend, StorageReference storageReference, String name) throws IOException {
        File file = new File(this.getCacheDir(), name + "_tempIcyPickFile.txt");

        FileWriter fileWriter = new FileWriter(file, false);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(dataToSend);
        bufferedWriter.close();
        fileWriter.close();

        InputStream inputStream = new FileInputStream(file);
        UploadTask uploadTask = storageReference.putStream(inputStream);

        uploadTask
                .addOnFailureListener((exception) -> Toast.makeText(this, R.string.text_upload_failure, Toast.LENGTH_SHORT).show())
                .addOnSuccessListener((taskSnapshot -> {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    file.delete();
                }));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Client Connection Successful.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Client Connection Suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Client Connection Failed.");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PLACE_KEY, resultPlace);
        outState.putParcelable(BITMAP_KEY, resultsBitmap);
        outState.putString(BITMAP_FILE_KEY, tempPhotoPath);
    }

    private void getCameraPermission(Intent takePictureIntent) {
        String[] permissions = {Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
        else {
            ActivityCompat.requestPermissions(this, permissions, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionGranted = false;
        if(requestCode == CAMERA_REQUEST_CODE){
            if(grantResults.length > 0){
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
            }
            permissionGranted = true;
        }
    }
}
