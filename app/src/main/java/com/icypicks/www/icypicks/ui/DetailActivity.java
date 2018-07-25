package com.icypicks.www.icypicks.ui;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.database.IceCreamContentProvider;
import com.icypicks.www.icypicks.database.IceCreamContract;
import com.icypicks.www.icypicks.java_classes.IceCream;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String INTENT_IMAGE_EXTRA = "intent_image_extra_for_details";
    public static final String INTENT_POSITION_EXTRA = "intent_position_extra_for_details";
    private static final String TAG = DetailActivity.class.getSimpleName();

    private IceCream iceCream = new IceCream();

    @BindView(R.id.detail_ice_cream_image_view)
    ImageView detailIceCreamImageView;

    @BindView(R.id.description_text_view)
    TextView descriptionTextView;

    @BindView(R.id.map_view)
    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle(getString(R.string.app_name));
//        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        Intent intent = getIntent();
        if(intent != null){
            Bitmap bitmap = intent.getParcelableExtra(INTENT_IMAGE_EXTRA);
            detailIceCreamImageView.setImageBitmap(bitmap);
            int iceCreamNumber = intent.getIntExtra(INTENT_POSITION_EXTRA, 0);
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference().child(String.valueOf(iceCreamNumber));

            iceCream.setUploadNumber(iceCreamNumber);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
            iceCream.setImageBytes(stream.toByteArray());

            StorageReference descriptionStorageReference = storageReference.child("description.txt");
            File descriptionFile = new File(this.getCacheDir(), "description.txt");
            descriptionStorageReference.getFile(descriptionFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        try {
                            FileReader fileReader = new FileReader(descriptionFile);
                            BufferedReader bufferedReader = new BufferedReader(fileReader);
                            String description = bufferedReader.readLine();
                            descriptionTextView.setText(description);
                            iceCream.setDescription(description);
                            bufferedReader.close();
                            fileReader.close(); } catch (IOException e){
                            e.printStackTrace();
                            descriptionTextView.setText(R.string.error_loading_description);
                        }
                        descriptionFile.delete();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        descriptionTextView.setText(R.string.error_loading_description);
                        descriptionFile.delete();
                    });

            StorageReference flavorStorageReference = storageReference.child("flavor.txt");
            File flavorFile = new File(this.getCacheDir(), "flavor.txt");
            flavorStorageReference.getFile(flavorFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        try {
                            FileReader fileReader = new FileReader(flavorFile);
                            BufferedReader bufferedReader = new BufferedReader(fileReader);
                            String flavor = bufferedReader.readLine();
                            toolbar.setTitle(flavor);
                            iceCream.setFlavor(flavor);
                            bufferedReader.close();
                            fileReader.close();
//                            Toast.makeText(this, "Will I see this?", Toast.LENGTH_SHORT).show();
                            Toast.makeText(this, flavor, Toast.LENGTH_SHORT).show();
                        }
                        catch (IOException exception){
                            exception.printStackTrace();
                            toolbar.setTitle(R.string.app_name);
                        }
                        flavorFile.delete();
                        setSupportActionBar(toolbar);
                    })
                    .addOnFailureListener(exception -> {
                        exception.printStackTrace();
                        toolbar.setTitle(R.string.app_name);
                        flavorFile.delete();
                        setSupportActionBar(toolbar);
                    });

            StorageReference placeStorageReference = storageReference.child("place.txt");
            File placeFile = new File(this.getCacheDir(), "place.txt");
            placeStorageReference.getFile(placeFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        try {
                            FileReader fileReader = new FileReader(placeFile);
                            BufferedReader bufferedReader = new BufferedReader(fileReader);
                            String place = bufferedReader.readLine();
                            iceCream.setPlace(place);
                            bufferedReader.close();
                            fileReader.close();
                            mapView.getMapAsync(this);
                            Log.d(TAG, "Map Async should start here.");
//                            Toast.makeText(this, "Will I see this?", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(this, flavor, Toast.LENGTH_SHORT).show();
                        }
                        catch (IOException exception){
                            exception.printStackTrace();
                            toolbar.setTitle(R.string.app_name);
                        }
                        flavorFile.delete();
                        setSupportActionBar(toolbar);
                    })
                    .addOnFailureListener(exception -> {
                        exception.printStackTrace();
                        toolbar.setTitle(R.string.app_name);
                        flavorFile.delete();
                        setSupportActionBar(toolbar);
                    });
        }

        mapView.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_to_must_try, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.add_to_must_try){
            ContentValues contentValues = new ContentValues();
            contentValues.put(IceCreamContract.IceCreamEntry.ICE_CREAM_FLAVOR, iceCream.getFlavor());
            contentValues.put(IceCreamContract.IceCreamEntry.ICE_CREAM_PLACE, iceCream.getPlace());
            contentValues.put(IceCreamContract.IceCreamEntry.ICE_CREAM_DESCRIPTION, iceCream.getDescription());
            contentValues.put(IceCreamContract.IceCreamEntry.ICE_CREAM_IMAGE, iceCream.getImageBytes());
            contentValues.put(IceCreamContract.IceCreamEntry.ICE_CREAM_UPLOAD_NUMBER, iceCream.getUploadNumber());

            getContentResolver().insert(IceCreamContract.IceCreamEntry.CONTENT_URI, contentValues);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "Map Ready!");
        map.getUiSettings().setMyLocationButtonEnabled(false);
        MarkerOptions markerOptions = new MarkerOptions();
        String[] latLng = iceCream.getPlace().split("_");
        markerOptions.position(new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1])));
            map.addMarker(markerOptions);
    }
}
