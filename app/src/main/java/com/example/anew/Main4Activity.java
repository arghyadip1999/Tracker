package com.example.anew;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Main4Activity extends AppCompatActivity {

    private EditText phoneNumberSignUp;


    ConstraintLayout constraintLayout;
    AnimationDrawable animationDrawable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        constraintLayout = findViewById(R.id.signupverify);
        animationDrawable = (AnimationDrawable)constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(100);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();


        // OTP Verification Part Begins

        phoneNumberSignUp = findViewById(R.id.phoneNumberSignUp);

        findViewById(R.id.verifySignUpButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String number = phoneNumberSignUp.getText().toString().trim();
                if (number.isEmpty() || number.length() < 10) {
                    phoneNumberSignUp.setError("Valid Number is required");
                    phoneNumberSignUp.requestFocus();
                    return;
                }

                String phoneNumber = "+91" + number;
                Intent intent = new Intent(Main4Activity.this, VerifyPhoneActivity.class);
                intent.putExtra("phonenumber", phoneNumber);
                startActivity(intent);

            }

        });
    }
}
