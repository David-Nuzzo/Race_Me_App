package com.example.raceme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateRouteActivity extends AppCompatActivity implements OnMapReadyCallback
{
    // Variable Declaration
    private GoogleMap mMap;

    public ArrayList markerPoints = new ArrayList();
    public MarkerOptions options = new MarkerOptions();
    public ArrayList polyPoints = new ArrayList();
    public LatLng startPoint;
    int pointAmount = 0;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
     }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // Move camera to peterborough.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.57917, -0.25965), 17.0f));

        // Called when the map is click on.
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                // Clear all markers and lines.
                mMap.clear();

                // Add the polyLine (which will have a new point)
                polyPoints.add(latLng);
                Polyline newRoute = mMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .width(15.0f)
                        .addAll(polyPoints)
                );
                if(pointAmount == 0)
                {
                    startPoint = latLng;
                    pointAmount ++;
                }

                // Add start and finish markers.
                for(int i = 0; i <= polyPoints.size(); i++)
                {
                    // Add start marker.
                    if (i == 0)
                    {
                        // Adding new item to the ArrayList
                        markerPoints.add(startPoint);
                        // Setting the position and colour of the marker
                        options.position(startPoint);
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        // Add new marker to the Google Map Android API V2
                        mMap.addMarker(options.title("Start Position"));
                    }

                    if(i == polyPoints.size() && polyPoints.size() > 0)
                    {
                        // Adding new item to the ArrayList
                        markerPoints.add(latLng);
                        // Setting the position and colour of the marker
                        options.position(latLng);
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        // Add new marker to the Google Map Android API V2
                        mMap.addMarker(options.title("Finish Position"));
                    }
                }
            }
        });
    }

    public void back(View view)
    {
        // Reset all.
        polyPoints.clear();
        markerPoints.clear();
        mMap.clear();
        pointAmount = 0;

        // Go back to the other page.
        startActivity(new Intent(CreateRouteActivity.this, ManageRoutesActivity.class));
    }

    public void createRoute(View view)
    {
        // Store the route with the name from the nameBox.
        EditText nameBox = findViewById(R.id.RouteNameBox);
        String routeName = nameBox.getText().toString();
        Map<String, Object> route = new HashMap<>();

        // Get all the polyPoints as strings.
        String routePointsStr = polyPoints.toString();

        // Add the points to the document as a string (Arrays cause errors for unknown reasons.).
        route.put("Points", routePointsStr);

        // Add a new document with a generated ID
        userId = mAuth.getUid();
        db.collection(userId)
                .document(routeName)
                .set(route)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Log.d("TAG", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.w("TAG", "Error writing document", e);
                    }
                });


        // Clear everthing to show it has been completed.
        polyPoints.clear();
        markerPoints.clear();
        nameBox.setText("");
        pointAmount = 0;
        mMap.clear();
    }
}
