package com.example.raceme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlanRunActivity extends AppCompatActivity
{
    // Variable Declaration
    ImageButton backButton;
    Button aiButton, confirmButton;
    EditText runNameBox;
    public static String selectedRoute, aiSetting, runName;
    public static int opponentTime;
    public Spinner spinner;

    // Variables for ai switch on
    EditText aiTimeTextBox, minsTextBox, secondsTextBox, minsInputBox, secondsInputBox;

    // Firebase Variables
    private FirebaseAuth mAuth;
    // Initialize Firebase Store
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private ArrayList<String> arrayList = new ArrayList();
    public String[] listElements = {"route 1", "route 2", "route 3", "route 4"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_run);

        // Initialize Spinner, Buttons and text boxes
        spinner       = findViewById(R.id.routeNamesSpinner);
        runNameBox    = findViewById(R.id.runNameInputBox);
        confirmButton = findViewById(R.id.pauseButton);
        aiButton = findViewById(R.id.aiButton);
        aiButton.setText("AI Opponent : Off");     // Added this as button would not activate if statement on first pass.
        minsInputBox       = findViewById(R.id.minsInputBox);
        secondsInputBox    = findViewById(R.id.secondsInputBox);
        aiTimeTextBox = findViewById(R.id.aiTimeBox);
        minsTextBox = findViewById(R.id.minsBox);
        secondsTextBox = findViewById(R.id.secondsBox);
        HideAiOptions();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        addListenerOnBackButton();
        addListenerOnAiButton();
        addListenerOnConfirmButton();
        FillList();
    }

    // Fill spinner options.
    public void FillList()
    {
        // Get the routes from the correct users saved routes.
        userId = mAuth.getUid();
        // Get all the route markers and names of each document in the users account files.
        db.collection(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                arrayList.add(document.getId() + "#");  // Just get the route names.
                            }
                            String allStuff = arrayList.toString();
                            // Formatting
                            allStuff = allStuff.replace("[", "");
                            allStuff = allStuff.replace("]", "");
                            allStuff = allStuff.replace(", ", "");
                            // Splitting into each record.
                            listElements = allStuff.split("#");

                            // Make the list on the activity.
                            List<String> ListElementsArrayList = new ArrayList<>(Arrays.asList(listElements));
                            ArrayAdapter<String> adapter = new ArrayAdapter<>
                                    (PlanRunActivity.this, android.R.layout.simple_list_item_1, ListElementsArrayList);
                            spinner.setAdapter(adapter);

                        }
                        else
                        {
                            Log.d("TAG", "Error getting routes: ", task.getException());
                        }
                    }
                });
    }
    public void HideAiOptions()
    {
        minsInputBox.setVisibility(View.INVISIBLE);
        secondsInputBox.setVisibility(View.INVISIBLE);
        aiTimeTextBox.setVisibility(View.INVISIBLE);
        minsTextBox.setVisibility(View.INVISIBLE);
        secondsTextBox.setVisibility(View.INVISIBLE);
    }
    public void ShowAiOptions()
    {
        minsInputBox.setVisibility(View.VISIBLE);
        secondsInputBox.setVisibility(View.VISIBLE);
        aiTimeTextBox.setVisibility(View.VISIBLE);
        minsTextBox.setVisibility(View.VISIBLE);
        secondsTextBox.setVisibility(View.VISIBLE);
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
                // Back to Home Page.
                startActivity(new Intent(PlanRunActivity.this, HomeActivity.class));
            }
        });
    }
    public void addListenerOnAiButton()
    {
        aiButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
               aiSetting = aiButton.getText().toString();
               if(aiSetting == "AI Opponent : Off")
               {
                   aiButton.setText("AI Opponent : On");
                   aiSetting=aiButton.getText().toString();
                   ShowAiOptions();
               }
               else
               {
                   aiButton.setText("AI Opponent : Off");
                   aiSetting=aiButton.getText().toString();
                   HideAiOptions();
               }
            }
        });
    }
    public void addListenerOnConfirmButton()
    {
        confirmButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Get the route name.
                selectedRoute = spinner.getSelectedItem().toString();
                // Set runName
                runName = runNameBox.getText().toString();
                // Set Opponent run time.
                if(aiSetting == "AI Opponent : Off")
                {
                    opponentTime = 0;
                }
                else // Opponent On
                {
                    if(minsInputBox.getText().toString().trim().length() == 0 || secondsInputBox.getText().toString().trim().length() == 0)  // If empty
                    {
                        opponentTime = 0;
                    }
                    else
                    {
                        opponentTime = ((Integer.parseInt(minsInputBox.getText().toString()) * 60 ) + Integer.parseInt(secondsInputBox.getText().toString()));
                    }
                }
                // Go to the Run Activity.
                startActivity(new Intent(PlanRunActivity.this, RunActivity.class));
            }
        });
    }
}
