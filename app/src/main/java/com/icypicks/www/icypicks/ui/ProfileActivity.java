package com.icypicks.www.icypicks.ui;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.java_classes.User;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.widget.Toast.makeText;

public class ProfileActivity extends AppCompatActivity {
    public static final String INTENT_USER_EXTRA = "intent_user_extra_for_profile";

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

    @BindView(R.id.posts_recycler_view)
    RecyclerView postsRecyclerView;

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
}
