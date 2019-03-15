package com.example.anew;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class Main3Activity extends AppCompatActivity {

    ConstraintLayout constraintLayout2;
    AnimationDrawable animationDrawable2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        constraintLayout2 = findViewById(R.id.constraintLayout2);
        animationDrawable2 = (AnimationDrawable)constraintLayout2.getBackground();
        animationDrawable2.setEnterFadeDuration(100);
        animationDrawable2.setExitFadeDuration(4000);
        animationDrawable2.start();
    }

    public void openActivity4(View view){

        Intent proceed = new Intent(this, Main4Activity.class);
        startActivity(proceed);
    }
}
