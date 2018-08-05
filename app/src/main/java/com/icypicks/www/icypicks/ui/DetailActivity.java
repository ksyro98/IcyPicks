package com.icypicks.www.icypicks.ui;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.database.IceCreamContract;
import com.icypicks.www.icypicks.java_classes.IceCream;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This activity contains more information about an ice cream.
 * In the onCreate method the information are retrieved from the firebase storage
 * and they are being shown in activity's UI (except the image that is passed as an intent extra).
 */
public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String INTENT_IMAGE_EXTRA = "intent_image_extra_for_details";
    public static final String INTENT_POSITION_EXTRA = "intent_position_extra_for_details";
    private static final String TAG = DetailActivity.class.getSimpleName();
    private static final int DETAILS_LOADER = 1;

    private IceCream iceCream = new IceCream();
    private boolean isMustTree = false;
    private Menu menu;

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

        Intent intent = getIntent();
        if(intent != null){
            String fileName = intent.getStringExtra(INTENT_IMAGE_EXTRA);
            Bitmap bitmap = BitmapFactory.decodeFile(fileName);
//            File file = new File(fileName);
//            try {
//                FileOutputStream fileOutputStream = new FileOutputStream(file);
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            Bitmap bitmap = intent.getParcelableExtra(INTENT_IMAGE_EXTRA);
            detailIceCreamImageView.setImageBitmap(bitmap);
            int iceCreamNumber = intent.getIntExtra(INTENT_POSITION_EXTRA, 0);
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference().child(String.valueOf(iceCreamNumber));

            iceCream.setUploadNumber(iceCreamNumber);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            iceCream.setImageBytes(stream.toByteArray());

            StorageReference imageStorageReference = storageReference.child("image.jpg");
            imageStorageReference.getDownloadUrl()
                    .addOnSuccessListener(uri -> Glide.with(getApplicationContext()).load(uri).into(detailIceCreamImageView))
                    .addOnFailureListener(Throwable::printStackTrace);

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
                        }
                        catch (IOException exception){
                            exception.printStackTrace();
                            Log.d(TAG, exception.toString());
                            toolbar.setTitle(R.string.app_name);
                        }
                        flavorFile.delete();
                        setSupportActionBar(toolbar);
                    })
                    .addOnFailureListener(exception -> {
                        exception.printStackTrace();
                        Log.d(TAG, exception.toString());
                        toolbar.setTitle(R.string.app_name);
                        flavorFile.delete();
                        setSupportActionBar(toolbar);
                    });

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
                            fileReader.close();
                        }
                        catch (IOException e){
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

    /**
     * If the item isn't in the database the icon of the menu button is a "+"
     * and if it is the icon of the menu button a "-".
     * This is why the SQLite database is queried in the onCreateOptionMenu using a loader.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.add_to_must_try, menu);

        menu.getItem(0).setEnabled(false);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Cursor> iceCreamLoader = loaderManager.getLoader(DETAILS_LOADER);
        if(iceCreamLoader == null){
            loaderManager.initLoader(DETAILS_LOADER, null, this);
        }
        else{
            loaderManager.restartLoader(DETAILS_LOADER, null, this);
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * When the user clicks the button in the option menu the ice cream is saved in the SQLite database
     * or it is removed from it if it was already there.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.add_to_must_try){
            if(!isMustTree) {
                if(iceCream.getFlavor() == null || iceCream.getPlace() == null || iceCream.getDescription() == null || iceCream.getImageBytes() == null){
                    Toast.makeText(this, R.string.write_to_database_error, Toast.LENGTH_SHORT).show();
                }
                else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(IceCreamContract.IceCreamEntry.ICE_CREAM_FLAVOR, iceCream.getFlavor());
                    contentValues.put(IceCreamContract.IceCreamEntry.ICE_CREAM_PLACE, iceCream.getPlace());
                    contentValues.put(IceCreamContract.IceCreamEntry.ICE_CREAM_DESCRIPTION, iceCream.getDescription());
                    contentValues.put(IceCreamContract.IceCreamEntry.ICE_CREAM_IMAGE, iceCream.getImageBytes());
                    contentValues.put(IceCreamContract.IceCreamEntry.ICE_CREAM_UPLOAD_NUMBER, iceCream.getUploadNumber());

                    getContentResolver().insert(IceCreamContract.IceCreamEntry.CONTENT_URI, contentValues);

                    item.setIcon(R.drawable.minus_sign);
                    isMustTree = true;
                }
            }
            else{
                int deleted = getContentResolver().delete(IceCreamContract.IceCreamEntry.CONTENT_URI, IceCreamContract.IceCreamEntry.ICE_CREAM_UPLOAD_NUMBER + " = ?", new String[]{String.valueOf(iceCream.getUploadNumber())});
                if(deleted>0){
                    item.setIcon(R.drawable.plus_sign);
                    isMustTree = false;
                }
                else{
                    Toast.makeText(this, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
                }
            }
            Intent intent = new Intent(this, MustTryWidgetProvider.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            sendBroadcast(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.getUiSettings().setMyLocationButtonEnabled(false);
        MarkerOptions markerOptions = new MarkerOptions();
        String[] latLng = iceCream.getPlace().split("_");
        LatLng latLng1 = new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
        markerOptions.position(latLng1);
        map.addMarker(markerOptions);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 10));
    }


    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor detailCursor = null;

            @Override
            protected void onStartLoading() {
                forceLoad();
                super.onStartLoading();
            }

            @Nullable
            @Override
            public Cursor loadInBackground() {
                return getContentResolver().query(IceCreamContract.IceCreamEntry.CONTENT_URI, null, IceCreamContract.IceCreamEntry.ICE_CREAM_UPLOAD_NUMBER + " = ?", new String[]{String.valueOf(iceCream.getUploadNumber())}, null);
            }

            @Override
            public void deliverResult(@Nullable Cursor data) {
                detailCursor = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        menu.getItem(0).setEnabled(true);

        if(data == null || !data.moveToFirst()){
            return;
        }

        menu.getItem(0).setIcon(R.drawable.minus_sign);

        isMustTree = true;

        data.close();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
