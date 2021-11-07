package com.example.raceme;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity
{
    // All Variable Declaration
    ImageButton backButton;
    private FirebaseAuth mAuth;
    private EditText emailBox, passwordBox;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        addListenerOnBackButton();

        // Initialization
        mAuth = FirebaseAuth.getInstance();
        emailBox = findViewById(R.id.emailBox);
        passwordBox = findViewById(R.id.passwordBox);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    public void createAccount(View view)
    {
        GetEmailPassword();
        String checkResult = CheckCredentials(email,password);

        if(checkResult == "Passed")
        {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Sign in success, notify the user that it has worked.
                            Toast.makeText(LoginActivity.this,"Account Created", Toast.LENGTH_SHORT).show();
                            Log.d("TAG", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        }
                        else
                        {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Account Creation Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
        else
        {
           // Don't attempt the create account but display what the result was.
            Toast.makeText(LoginActivity.this, checkResult, Toast.LENGTH_LONG).show();
        }
    }

    public void login(View view)
    {
        GetEmailPassword();
        String checkResult = CheckCredentials(email,password);

        if(checkResult == "Passed")
        {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(LoginActivity.this, "Signed in as." + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                                // To home Page.
                                startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                            }
                            else
                            {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else
        {
            // Don't attempt login.
            // State result of check
            Toast.makeText(LoginActivity.this, checkResult, Toast.LENGTH_LONG).show();
        }


    }



    private void GetEmailPassword()
    {
        // Sets the email and password variables from the corresponding text in the boxes.
        email    = emailBox.getText().toString().trim();
        password = passwordBox.getText().toString().trim();
    }

    public static String CheckCredentials(String email, String password)
    {
        String result = "Passed";

        // Check
        if (email.length() == 0 || password.length() == 0)  // Email or password contains nothing.
        {
            result = "No email or password inputted in above boxes.";
        }
        if (!email.contains("@"))
        {
            result = "Email does not contain '@' .";
        }
        return result;
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
                // Back to Start Page.
                startActivity(new Intent(LoginActivity.this,StartActivity.class));
            }
        });
    }
}