package com.example.raceme;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback
{
    // Variable Declaration
    ImageButton backButton;
    public Button startButton, pauseButton, finishButton, resumeButton, quitButton;
    public TextView stateTextBox, timerTextBox;
    public String runState = "Ready";

    // Firebase Variables
    private FirebaseAuth mAuth;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;

    // Build Map Route Variables.
    public String[] readPoints = {""};
    public String readPointsStr;
    boolean isReading = false;
    public ArrayList allPoints = new ArrayList();
    public ArrayList markerPoints = new ArrayList();
    public MarkerOptions options = new MarkerOptions();
    public LatLng startPoint, endPoint;

    // Ai Marker Variables.
    Double aiLat = 52.57917;
    Double aiLng = -0.25965;
    public int pointLocation = 1;
    Circle aiMarker;

    // Timer Variable
    CountDownTimer timer;
    long diff = 1000;
    long maxTime = 999999999;
    int minutes, seconds, timeElapsed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        // Instantiate each button & the state textbox.
        stateTextBox = findViewById(R.id.stateTextBox);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        finishButton = findViewById(R.id.finishButton);
        resumeButton = findViewById(R.id.resumeButton);
        quitButton = findViewById(R.id.quitButton);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Instantiate Button handlers.
        HandleStartButton();
        HandlePauseButton();
        HandleFinishButton();
        HandleResumeButton();
        HandleQuitButton();
        addListenerOnBackButton();

        // Build the Map & Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        final GoogleMap mMap = googleMap;

        GetRoute(mMap);  // This gets the route and displays both the route and ai position (if turned on.)

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {

            }
        });
    }

    public void GetRoute(final GoogleMap mMap)
    {
        // Get and display route from Firebase.
        // Get the data of the route which was selected in the plan activity from firebase.  (PlanRunActivty.selectedRoute)
        userId = mAuth.getUid();
        DocumentReference docRef = db.collection(userId).document(PlanRunActivity.selectedRoute);
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
                        readPointsStr = document.getData().toString(); // Get all of the route points LANG/LONG.
                        // Formatting
                        readPointsStr = readPointsStr.replace("{Points=[lat/lng: ", "");
                        readPointsStr = readPointsStr.replace("(", "");
                        readPointsStr = readPointsStr.replace(")", "");
                        readPointsStr = readPointsStr.replace(" lat/lng: ", "");
                        readPointsStr = readPointsStr.replace("]}", "");
                        // Convert to string list.
                        readPoints = readPointsStr.split(",");
                        // Convert string list to Lat/Lng list.
                        for(int i = 0; i < readPoints.length - 1; i++)
                        {
                            allPoints.add( new LatLng(Double.parseDouble(readPoints[i]), Double.parseDouble(readPoints[i + 1])));  // Lat/Lng of loaded route points.
                            i = i + 1;
                        }
                        // Map Setup Code.
                        {
                            // Move camera to peterborough.
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((LatLng) allPoints.get(0), 15.0f));
                            // Create AI Marker
                            DisplayRoute(mMap);
                            DisplayOpponent(mMap, aiMarker, allPoints);
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

    public static Circle DisplayOpponent(GoogleMap mMap, Circle aiMarker, ArrayList allPoints)
    {
        if(PlanRunActivity.aiSetting == "AI Opponent : On")
        {
             aiMarker = mMap.addCircle(new CircleOptions()
                .center((LatLng) allPoints.get(0))
                .radius(10)
                .strokeColor(Color.BLACK)
                .fillColor(Color.RED));

             return aiMarker;
        }
        return null;
    }

    public void DisplayRoute(GoogleMap mMap)
    {
        // Add/Display the route on the map.
        Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .width(15.0f)
                .addAll(allPoints)
        );

        // Add start and finish markers.
        for(int i = 0; i <= allPoints.size(); i++)
        {
            // Add start marker.
            if (i == 0)
            {
                // Adding new item to the ArrayList
                startPoint = (LatLng) allPoints.get(0);
                markerPoints.add(startPoint);
                // Setting the position and colour of the marker
                options.position(startPoint);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options.title("Start Position"));
            }

            if(i == allPoints.size() && allPoints.size() > 0)
            {
                // Adding new item to the ArrayList
                endPoint = (LatLng) allPoints.get(i - 1);
                markerPoints.add(endPoint);
                // Setting the position and colour of the marker
                options.position(endPoint);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options.title("Finish Position"));
            }
        }
    }

    public void UpdateOpponentPos(GoogleMap mMap)
    {
        // Replace marker with new one in updated pos.
        aiMarker.remove();
        aiMarker = mMap.addCircle(new CircleOptions()
            .center((LatLng) allPoints.get(pointLocation))  // First marker location needs to be changed.
            .radius(10)
            .strokeColor(Color.BLACK)
            .fillColor(Color.RED));

        pointLocation ++;
    }

    // Button Handlers
    public void HandleStartButton()
    {
        startButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Hide this button
                startButton.setVisibility(View.INVISIBLE);
                // Present the pause and finish button.
                pauseButton.setVisibility(View.VISIBLE);
                finishButton.setVisibility(View.VISIBLE);
                // Change the state text to running.
                runState = "State: Running";
                stateTextBox.setText(runState);

                // Start the timer in the time counter box.
                timerTextBox = findViewById(R.id.TimeTextBox);
                StartNewTimer();
            }
        });
    }

    private void StartNewTimer()
    {
        timer = new CountDownTimer(maxTime, diff)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                // Update Timer Text for a second.
                diff = maxTime - millisUntilFinished;
                minutes = (int) diff / 1000 / 60;
                seconds = (int) diff / 1000;

                // Add elapsed time from before pause to mins and seconds.
                seconds = seconds + (timeElapsed % 60);
                minutes = minutes + ((timeElapsed / 60) % 60);

                timerTextBox.setText("Time: " + minutes + ":" +  seconds);
            }
            @Override
            public void onFinish()
            {
            }
        }.start();
    }

    public void HandlePauseButton()
    {
        // Hide this button
        pauseButton.setVisibility(View.INVISIBLE);

        pauseButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Change the state text to pause.
                runState = "State: Paused";
                stateTextBox.setText(runState);

                // Pause the time counter
                timeElapsed = (minutes * 60) + seconds; // store the full seconds currently elapsed.
                timer.cancel(); ; // End/Delete the timer.

                // Present the user with the resume and quit button and hide the pause & finish button.
                resumeButton.setVisibility(View.VISIBLE);
                quitButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
                finishButton.setVisibility(View.INVISIBLE);

                // Stop the progress of the ai opponent.
            }
        });
    }

    public void HandleFinishButton()
    {
        // Hide this button
        finishButton.setVisibility(View.INVISIBLE);
        finishButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Get all info about the run (Run Name, Route Name, Time, Date, Average Run Pace*, Distance*.)
                //Toast.makeText(RunActivity.this, "Time = " + minutes + ":" + seconds , Toast.LENGTH_SHORT).show();
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                String formattedDate = df.format(c);

                // Form new document with a generated ID
                Map<String, Object> data = new HashMap<>();
                data.put("Route", PlanRunActivity.selectedRoute);
                data.put("Time", minutes + ":" + seconds);
                data.put("Date", formattedDate);

                // Send document to Filebase.
                userId = mAuth.getUid();
                db.collection("PastRuns" + userId)
                        .document(PlanRunActivity.runName)
                        .set(data);

                // Send result message to user.
                if(PlanRunActivity.aiSetting == "AI Opponent : On")
                {
                    // Check whether the users time was less than the opponents time.
                    // Convert the timers mins and seconds to seconds.
                    int runTime = ((minutes * 60) + seconds);
                    if(runTime < PlanRunActivity.opponentTime)
                    {
                        Toast.makeText(RunActivity.this, "Congratulations you won!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(RunActivity.this, "Run completed & Saved to FileBase", Toast.LENGTH_SHORT).show();
                    }
                    else if (runTime >= PlanRunActivity.opponentTime)
                    {
                        Toast.makeText(RunActivity.this, "You Lose, the opponent won!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(RunActivity.this, "Run complete & Saved to FileBase", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(RunActivity.this, "Run completed & Saved to FileBase", Toast.LENGTH_SHORT).show();
                }

                // Change the activity to the home activity.
                startActivity(new Intent(RunActivity.this, HomeActivity.class));
            }
        });
    }

    public void HandleResumeButton()
    {
        // Hide this button
        resumeButton.setVisibility(View.INVISIBLE);

        resumeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Change the state text from paused.
                stateTextBox.setText(runState);

                // Resume the timer.
                diff = 1000;
                maxTime = 999999999;
                StartNewTimer();

                // Hide this button & quit button. Then Present the pause and finish button.
                resumeButton.setVisibility(View.INVISIBLE);
                quitButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
                finishButton.setVisibility(View.VISIBLE);

                // Stop the progress of the ai opponent.
                // This can only be done in the race me app.
            }
        });
    }

    public void HandleQuitButton()
    {
        // Hide this button
        quitButton.setVisibility(View.INVISIBLE);

        quitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                // Take the user back to the plan activity page.
                Toast.makeText(RunActivity.this, "Run aborted", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RunActivity.this, PlanRunActivity.class));
            }
        });
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
                // Back to Plan Run Page.
                startActivity(new Intent(RunActivity.this, PlanRunActivity.class));
            }
        });
    }
}
