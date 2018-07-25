package com.icypicks.www.icypicks.ui;

import android.content.Intent;
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
import com.icypicks.www.icypicks.java_classes.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LogInSignUpActivity extends AppCompatActivity {
    public static final String ACCOUNT_INFO = "account_info_first_time";
    public static final String EMAIL_INTENT = "email_name_for_intent";
    public static final String PASSWORD_INTENT = "password_name_for_intent";
    public static final String NAME_INTENT = "name_name_for_intent";
    public static final String FLAVOR_INTENT = "flavor_name_for_intent";
    public static final String USER_INTENT = "user_name_for_intent";

    private boolean hasAccount;
    EditText nameEditText;
    EditText flavorEditText;
    EditText emailEditText;
    EditText passwordEditText;
    EditText repeatPasswordEditText;

    @BindView(R.id.container_frame_layout)
    FrameLayout containerFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_sign_up);
        ButterKnife.bind(this);


        Intent intent = getIntent();

        View.OnClickListener onClick = view ->{
            if(allEditTextValuesAreValid()){
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EMAIL_INTENT, emailEditText.getText().toString());
                resultIntent.putExtra(PASSWORD_INTENT, passwordEditText.getText().toString());
                if(!hasAccount) {
                    User user = new User(nameEditText.getText().toString(), flavorEditText.getText().toString(), emailEditText.getText().toString(), null, new ArrayList<>());
                    resultIntent.putExtra(USER_INTENT, user);
//                    Toast.makeText(this, ((User) intent.getParcelableExtra(USER_INTENT)).getName(), Toast.LENGTH_SHORT).show();
//                    resultIntent.putExtra(NAME_INTENT, nameEditText.getText().toString());
//                    resultIntent.putExtra(FLAVOR_INTENT, flavorEditText.getText().toString());
                }
//                Toast.makeText(this, String.valueOf(user != null), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        };

        if(intent != null){
            hasAccount = intent.getBooleanExtra(ACCOUNT_INFO, false);
            if(!hasAccount){
//                Toast.makeText(this, String.valueOf(hasAccount), Toast.LENGTH_SHORT).show();
                FrameLayout signUpLayout = (FrameLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.sign_up_screen, containerFrameLayout, true);
                ImageView profileImageView = (ImageView) signUpLayout.findViewById(R.id.profile_image_view);
                nameEditText = (EditText) signUpLayout.findViewById(R.id.name_edi_text);
                flavorEditText = (EditText) signUpLayout.findViewById(R.id.favorite_flavor_edit_text);
                emailEditText = (EditText) signUpLayout.findViewById(R.id.email_edit_text);
                passwordEditText = (EditText) signUpLayout.findViewById(R.id.password_edit_text);
                repeatPasswordEditText = (EditText) signUpLayout.findViewById(R.id.repeat_password_edit_text);
                TextView haveAccountTextView = (TextView) signUpLayout.findViewById(R.id.already_have_an_account_text_view);
                Button signUpButton = (Button) signUpLayout.findViewById(R.id.sign_up_button);

                signUpButton.setOnClickListener(onClick);
            }
            else{
                FrameLayout signUpLayout = (FrameLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.log_in_screen, containerFrameLayout, true);

                emailEditText = (EditText) signUpLayout.findViewById(R.id.email_edit_text);
                passwordEditText = (EditText) signUpLayout.findViewById(R.id.password_edit_text);
                TextView forgotPasswordTextView = (TextView) signUpLayout.findViewById(R.id.forgot_password_text_view);
                Button logInButton = (Button) signUpLayout.findViewById(R.id.log_in_button);

                logInButton.setOnClickListener(onClick);
            }
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
}
