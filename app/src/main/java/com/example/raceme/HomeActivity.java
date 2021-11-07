package com.example.raceme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity
{
    // Variable Declaration
    ImageButton backButton;
    Button pastRunsButton, startRunButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        addListenerOnBackButton();
        addListenerOnPastRunsButton();
        addListenerOnStartRunButton();
    }

    public void manageRoutes(View view)
    {
        startActivity(new Intent(HomeActivity.this, ManageRoutesActivity.class));
    }

    // Button Listeners
    public void addListenerOnBackButton()
    {
        backButton = (ImageButton) findViewById(R.id.navBackButton);

        backButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Back to Login Page.
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            }
        });
    }
    public void addListenerOnPastRunsButton()
    {
        pastRunsButton =  findViewById(R.id.pastRunsButton);

        pastRunsButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Back to Login Page.
                startActivity(new Intent(HomeActivity.this,PastRunsActivity.class));
            }
        });
    }
    public void addListenerOnStartRunButton()
    {
        startRunButton = findViewById(R.id.StartRunButton);

        startRunButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Back to Login Page.
                startActivity(new Intent(HomeActivity.this, PlanRunActivity.class));
            }
        });
    }

}
