package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button btnToSignUp, btnLogin;
    EditText Email, Password;
    FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null)
            startActivity(new Intent(MainActivity.this, Welcome.class));
    } // בדיקה אם המשתמש כבר מחובר אז להעביר אותו לדף בית

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mAuth = FirebaseAuth.getInstance();
        InitViews();
        InitOnClicks();
    }

    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Exit");
        alertDialog.setMessage("Are You Sure You Want To Exit?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                        System.exit(0);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CLOSE",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    } // דיאלוג האם לצאת מהאפליקציה

    private void InitOnClicks() {
        btnToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUp.class));
            }
        }); // כפתור למעבר לדף הרשמה
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLogin.setEnabled(false);
                if (Email.getText().length() == 0 || Password.getText().length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(Email.getText().toString(), Password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            startActivity(new Intent(MainActivity.this, Welcome.class));
                            btnLogin.setEnabled(true);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            btnLogin.setEnabled(true);
                        }
                    }
                });
            }
        }); // כפתור לביצוע התחברות
    }

    private void InitViews() {
        btnToSignUp = findViewById(R.id.btnToSignUp);
        btnLogin = findViewById(R.id.btnLogin);
        Email = findViewById(R.id.LoginEmail);
        Password = findViewById(R.id.LoginPassword);
    }
}