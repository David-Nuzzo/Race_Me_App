package com.example.raceme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;

public class StartActivity extends AppCompatActivity
{
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        addListenerOnButton();
    }

    public void addListenerOnButton()
    {
        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // StartActivity to LoginActivity
                startActivity(new Intent(StartActivity.this,LoginActivity.class));
            }
        });
    }
}