package com.example.raceme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PastRunsActivity extends AppCompatActivity
{
    // Nav Vars
    ImageButton backButton, deleteButton, shareButton;
    // Spinner Vars
    private Spinner spinner;
    private String selectedItem;
    private FirebaseAuth mAuth;
    private String userId;
    private ArrayList<String> arrayList = new ArrayList();
    private String[] listElements = {"route 1", "route 2", "route 3", "route 4"};
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayAdapter<String> adapter;

    // Run info Vars
    private String routeName, runTime, runDate;
    EditText routeNameBox, runTimeBox, runDateBox;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_runs);

        // Initialize Buttons
        deleteButton = findViewById(R.id.DeleteButton);
        shareButton = findViewById(R.id.ShareButton);
        backButton = findViewById(R.id.navBackButton);

        // Retrieval of past run names for the dropdown list (spinner.)
        spinner = findViewById(R.id.selectRouteSpinner);
        mAuth = FirebaseAuth.getInstance();
        FillSpinner();

        // Add Listeners
        addListenerOnBackButton();
        addListenerOnSpinner();
        addListenerOnDelete();
        addListenerOnShare();
    }

    // Add Listeners
    public void addListenerOnBackButton()
    {
        backButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Back to Home Page.
                startActivity(new Intent(PastRunsActivity.this, HomeActivity.class));
            }
        });
    }

    public void addListenerOnSpinner()
    {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selectedItem = spinner.getSelectedItem().toString();
                GetAllRouteData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
    }

    public void addListenerOnDelete()
    {
        deleteButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                DeleteSelectedRun();
            }
        });
    }

    public void addListenerOnShare()
    {
        shareButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                ShareRun();
            }
        });
    }

    // Fill spinner options with the doc names from filebase.
    public void FillSpinner()
    {
        // Get the routes from the correct users saved routes.
        userId = mAuth.getUid();
        // Get all the route markers and names of each document in the users account files.
        db.collection("PastRuns" + userId)
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
                                arrayList.add(document.getId());  // Just get the route names.
                            }
                            String allStuff = arrayList.toString();
                            // Formatting
                            allStuff = allStuff.replace("[", "");
                            allStuff = allStuff.replace("]", "");
                            allStuff = allStuff.replace(", ", ",");
                            // Splitting into each record.
                            listElements = allStuff.split(",");
                            // Make the list on the activity.
                            List<String> ListElementsArrayList = new ArrayList<>(Arrays.asList(listElements));
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(PastRunsActivity.this, android.R.layout.simple_list_item_1, ListElementsArrayList);
                            spinner.setAdapter(adapter);
                        }
                        else
                        {
                            Log.d("TAG", "Error getting the docs of past runs: ", task.getException());
                        }
                    }
                });
    }

    // Get all route data from filebase.
    public void GetAllRouteData()
    {
        if(spinner.getSelectedItem().toString().trim().length() == 0)  // There are no runs.
        {
            Toast.makeText(PastRunsActivity.this,"No completed runs saved.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            db.collection("PastRuns" + userId).document(spinner.getSelectedItem().toString())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
            {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task)
                {
                    if (task.isSuccessful())
                    {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists())
                        {
                            // Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            String fullDocStr = document.getData().toString();
                            fullDocStr = fullDocStr.replace("{","");
                            fullDocStr = fullDocStr.replace("}","");
                            String[] fullDocArr = fullDocStr.split(",");
                            runTime = fullDocArr[0].replace  ("Time=","");
                            runDate = fullDocArr[1].replace  (" Date=", "");
                            routeName = fullDocArr[2].replace(" Route=", "");

                            FillRouteData();
                        }
                        else
                        {
                            Log.d("TAG", "No such document to get data from.");
                        }
                    }
                    else
                    {
                        Log.d("TAG", "document get data failed with ", task.getException());
                    }
                }
            });
        }

    }

    // Internal to GetAllRouteData
    public void FillRouteData()
    {
        routeNameBox = findViewById(R.id.RouteNameBox);
        runTimeBox = findViewById(R.id.TimeBox);
        runDateBox = findViewById(R.id.DateBox);

        routeNameBox.setText("Route: " + routeName);
        runTimeBox.setText  ("Time: "  + runTime  );
        runDateBox.setText  ("Date: "  + runDate  );
    }

    public void DeleteSelectedRun()
    {
        // Delete the file.
        db.collection("PastRuns" + userId).document(spinner.getSelectedItem().toString())
                .delete();

        // Back to Home Page.  (This is to temporarily solve the refreshing of the spinner.)
        startActivity(new Intent(PastRunsActivity.this, HomeActivity.class));
    }

    public void ShareRun()
    {
        Intent myIntent = new Intent(Intent.ACTION_SEND);
        myIntent.setType("text/plain");
        String shareBody = "I completed the following run! \nRun Name: " + spinner.getSelectedItem().toString() + "\nRoute: "+ routeName + "\nTime: " + runTime + "\nDate: " + runDate + "\n(with help from the RaceMe app.)";
        String shareSub = "Your subject!";
        myIntent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
        myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(myIntent, "Share using"));
    }
}
