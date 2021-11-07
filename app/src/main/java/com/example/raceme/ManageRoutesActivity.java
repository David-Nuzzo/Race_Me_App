package com.example.raceme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ManageRoutesActivity extends AppCompatActivity
{
    // Variable Declaration
    ImageButton backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_routes);


        addListenerOnBackButton();
    }


    public void viewAllRoutes(View view)
    {
        startActivity(new Intent(ManageRoutesActivity.this,AllRoutesActivity.class));
    }

    public void createRoute(View view)
    {
         startActivity(new Intent(ManageRoutesActivity.this, CreateRouteActivity.class));
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
                startActivity(new Intent(ManageRoutesActivity.this, HomeActivity.class));
            }
        });
    }
}
