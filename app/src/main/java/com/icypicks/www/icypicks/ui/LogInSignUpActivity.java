package com.icypicks.www.icypicks.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.helpers.BitmapUtils;
import com.icypicks.www.icypicks.java_classes.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LogInSignUpActivity extends AppCompatActivity {
    public static final String EMAIL_INTENT = "email_name_for_intent";
    public static final String PASSWORD_INTENT = "password_name_for_intent";
    public static final String USER_INTENT = "user_name_for_intent";
    private static final String FILE_PROVIDER_AUTHORITY = "com.icypicks.www.icypicks.fileprovider";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private boolean hasAccount;
    private String tempPhotoPath;
    private Bitmap resultsBitmap;

    private ImageView profileImageView;
    private EditText nameEditText;
    private EditText flavorEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;

    @BindView(R.id.container_frame_layout)
    FrameLayout containerFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_sign_up);
        ButterKnife.bind(this);

        View.OnClickListener onClick = view ->{
            if(allEditTextValuesAreValid()){
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EMAIL_INTENT, emailEditText.getText().toString());
                resultIntent.putExtra(PASSWORD_INTENT, passwordEditText.getText().toString());

                //code was taken from Stack Overflow post: https://stackoverflow.com/questions/4352172/how-do-you-pass-images-bitmaps-between-android-activities-using-bundles#4352194
                try{
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    if(resultsBitmap != null) {
                        resultsBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                        FileOutputStream fileOutputStream = openFileOutput(MainActivity.FILE_NAME, Context.MODE_PRIVATE);
                        fileOutputStream.write(bytes.toByteArray());
                        fileOutputStream.close();
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                if(!hasAccount) {
                    User user = new User(nameEditText.getText().toString(), flavorEditText.getText().toString(), emailEditText.getText().toString(), null, new ArrayList<>());
                    resultIntent.putExtra(USER_INTENT, user);
                }
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        };

        File file = new File(this.getCacheDir(), MainActivity.INFO_FILE_NAME);
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            hasAccount = Boolean.parseBoolean(bufferedReader.readLine());
            bufferedReader.close();
            fileReader.close();
        }
        catch (IOException e){
            e.printStackTrace();
            hasAccount = false;
        }
        if(!hasAccount){
            FrameLayout signUpLayout = (FrameLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.sign_up_screen, containerFrameLayout, true);
            profileImageView = (ImageView) signUpLayout.findViewById(R.id.profile_image_view);
            nameEditText = (EditText) signUpLayout.findViewById(R.id.name_edi_text);
            flavorEditText = (EditText) signUpLayout.findViewById(R.id.favorite_flavor_edit_text);
            emailEditText = (EditText) signUpLayout.findViewById(R.id.email_edit_text);
            passwordEditText = (EditText) signUpLayout.findViewById(R.id.password_edit_text);
            repeatPasswordEditText = (EditText) signUpLayout.findViewById(R.id.repeat_password_edit_text);
            TextView haveAccountTextView = (TextView) signUpLayout.findViewById(R.id.already_have_an_account_text_view);
            Button signUpButton = (Button) signUpLayout.findViewById(R.id.sign_up_button);

            profileImageView.setImageDrawable(getResources().getDrawable(R.drawable.avatar));
            profileImageView.setBackgroundColor(getResources().getColor(R.color.darker_grey));
            profileImageView.setOnClickListener(view ->{
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

            signUpButton.setOnClickListener(onClick);

            haveAccountTextView.setOnClickListener(view -> {
                hasAccount = true;
                try {
                    FileWriter fileWriter = new FileWriter(file, false);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write("true");
                    bufferedWriter.newLine();
                    bufferedWriter.close();
                    fileWriter.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                recreate();
            });
        }
        else{
            FrameLayout signUpLayout = (FrameLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.log_in_screen, containerFrameLayout, true);

            emailEditText = (EditText) signUpLayout.findViewById(R.id.email_edit_text);
            passwordEditText = (EditText) signUpLayout.findViewById(R.id.password_edit_text);
            TextView createNewAccountTextView = (TextView) signUpLayout.findViewById(R.id.cretete_new_account_text_view);
            Button logInButton = (Button) signUpLayout.findViewById(R.id.log_in_button);

            logInButton.setOnClickListener(onClick);
            createNewAccountTextView.setOnClickListener(view -> {
                hasAccount = false;
                try {
                    FileWriter fileWriter = new FileWriter(file, false);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write("false");
                    bufferedWriter.newLine();
                    bufferedWriter.close();
                    fileWriter.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                recreate();
            });
        }
    }

    private boolean allEditTextValuesAreValid(){
        if(!hasAccount){
            if(!"".equals(nameEditText.getText().toString()) && !"".equals(flavorEditText.getText().toString()) && !"".equals(emailEditText.getText().toString()) && !"".equals(passwordEditText.getText().toString()) && !"".equals(repeatPasswordEditText.getText().toString())){
                if(passwordEditText.length() < 8){
                    Toast.makeText(this, R.string.password_length_error, Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(passwordEditText.getText().toString().equals(repeatPasswordEditText.getText().toString())){
                    return true;
                }
                else{
                    Toast.makeText(this, R.string.passwords_dont_match, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            else{
                Toast.makeText(this, R.string.empty_fields, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else{
            if(!"".equals(emailEditText.getText().toString()) && !"".equals(passwordEditText.getText().toString())){
                return true;
            }
            else{
                Toast.makeText(this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if((requestCode == REQUEST_IMAGE_CAPTURE) && (resultCode == RESULT_OK)){
            resultsBitmap = BitmapUtils.resamplePic(this, tempPhotoPath);
            profileImageView.setImageBitmap(resultsBitmap);
        }
        else{
            BitmapUtils.deleteImageFile(this, tempPhotoPath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
