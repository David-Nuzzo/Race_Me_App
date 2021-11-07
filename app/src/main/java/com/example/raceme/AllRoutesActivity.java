package com.example.raceme;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.Api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AllRoutesActivity extends AppCompatActivity implements OnMapReadyCallback
{
    // Variable Declaration
    private String userId;
    private ArrayList<String> arrayList = new ArrayList();
    private String[] listElements = {"1","2","3"};
    private FirebaseAuth mAuth;
    public FirebaseFirestore db;
    private Spinner routeDropdown;
    private ImageButton deleteButton;

    // Vars for the map fragment
    public String[] readPoints = {""};
    public String readPointsStr;
    boolean isReading = false;
    private String selectedRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_routes);

        // Initialize all.
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        routeDropdown = findViewById(R.id.selectRouteSpinner);
        deleteButton = findViewById(R.id.DeleteButton);

        FillDropdown();
        AddListenerOnDelete();

        // Build the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        GoogleMap mMap = googleMap;
        AddListenerOnSpinner(mMap);
    }

    public void GetRoute(final GoogleMap mMap)
    {
        if(selectedRoute == "")  // No saved routes.
        {
            Toast.makeText(AllRoutesActivity.this,"No routes saved yet.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // Created internally so it is made fresh for each selection.
            final ArrayList[] allPoints = {new ArrayList()};

            // Get and display route from Firebase.
            // Get the data of the route which was selected in the dropdown list from firebase.  (PlanRunActivty.selectedRoute)
            userId = mAuth.getUid();
            DocumentReference docRef = db.collection(userId).document(selectedRoute);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
            {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task)
                {
                    if (task.isSuccessful())
                    {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists())
                        {
                            // Get all of the route points LANG/LONG from firebase into a str.
                            readPointsStr = document.getData().toString();
                            // Convert the full document str into a string array of just Lat/Lng values.
                            readPoints = FormatThenSplit(readPointsStr);
                            // Convert string array to Lat/Lng list.
                            for(int i = 0; i < readPoints.length - 1; i++)
                            {
                                allPoints[0].add( new LatLng(Double.parseDouble(readPoints[i]), Double.parseDouble(readPoints[i + 1])));  // Lat/Lng of loaded route points.
                                i = i + 1;
                            }
                            // Map Setup Code.
                            {
                                // Move camera to peterborough.
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((LatLng) allPoints[0].get(0), 15.0f));
                                // Create AI Marker
                                DisplayRoute(mMap, allPoints[0]);
                            }
                        }
                        else
                        {
                            Log.d("TAG", "Error getting routes: ", task.getException());
                        }
                    }
                    else
                    {
                        Log.d("TAG", "Error getting route data: ", task.getException());
                    }
                }
            });
        }
    }

    public void DisplayRoute(GoogleMap mMap, ArrayList allPoints)
    {
        // Reset Polyline and clear map.
        Polyline polyline1 = null;
        mMap.clear();
        // Add/Display the route on the map.
        polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .width(15.0f)
                .addAll(allPoints)
        );
    }

     // Add button listener
     public void AddListenerOnDelete()
     {
         deleteButton.setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View arg0)
             {
                 DeleteSelectedRoute();
             }
         });
     }

    public void AddListenerOnSpinner(final GoogleMap mMap)
    {
        routeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selectedRoute = routeDropdown.getSelectedItem().toString();
                GetRoute(mMap);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
    }

     // All other methods.
     public void FillDropdown()
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
                             ArrayAdapter<String> adapter = new ArrayAdapter<>(AllRoutesActivity.this, android.R.layout.simple_list_item_1, ListElementsArrayList);
                             routeDropdown.setAdapter(adapter);
                         }
                         else
                         {
                             Log.d("TAG", "Error getting the docs of past runs: ", task.getException());
                         }
                     }
                 });
     }

    public void DeleteSelectedRoute()
    {
        // Delete the file.
        db.collection(userId).document(routeDropdown.getSelectedItem().toString()).delete();

        // Back to Home Page.  (This is to temporarily solve the refreshing of the spinner.)
        startActivity(new Intent(AllRoutesActivity.this, ManageRoutesActivity.class));
    }

    public void back(View view)
    {
        // Go back to the other page.
        startActivity(new Intent(AllRoutesActivity.this, ManageRoutesActivity.class));
    }

    public static String[] FormatThenSplit(String FullDocStr)
    {
        // Formatting and Split into StringArray
        FullDocStr = FullDocStr.replace("{Points=[lat/lng: ", "");
        FullDocStr = FullDocStr.replace("(", "");
        FullDocStr = FullDocStr.replace(")", "");
        FullDocStr = FullDocStr.replace(" lat/lng: ", "");
        FullDocStr = FullDocStr.replace("]}", "");
        // Convert to string array.
        return FullDocStr.split(",");

    }
}
