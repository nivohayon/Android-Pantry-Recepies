package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity{

    EditText FirstName, Email, Password, RePassword;
    Button BtnSignUp, BtnToLogin;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        InitViews();
        InitOnClicks();
    }

    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(SignUp.this).create();
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
        BtnToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUp.this, MainActivity.class));
            }
        }); // כפתור לדף התחברות
        BtnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnSignUp.setEnabled(false);
                signUpUser();
            }
        }); // כפתור להרשמה
    }

    private void InitViews() {
        FirstName = findViewById(R.id.SignUpName);
        Email = findViewById(R.id.SignUpEmail);
        Password = findViewById(R.id.SignUpPassword);
        RePassword = findViewById(R.id.SignUpRePassword);
        BtnSignUp = findViewById(R.id.btnSignUp);
        BtnToLogin = findViewById(R.id.btnToLogin);
    }

    private void signUpUser() {
        if (!Password.getText().toString().equals(RePassword.getText().toString()))
        {
            Toast.makeText(getApplicationContext(), "Passwords Doesn't Match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (FirstName.getText().length() == 0 || Email.getText().length() == 0 || Password.getText().length() == 0 || RePassword.getText().length() == 0)
        {
            Toast.makeText(getApplicationContext(), "Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(Email.getText().toString(), Password.getText().toString())
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            database = FirebaseDatabase.getInstance();
                            myRef = database.getReference();
                            User user = new User(FirstName.getText().toString());
                            myRef.child("UsersInfo").child(mAuth.getUid()).setValue(user);
                            startActivity(new Intent(SignUp.this, Welcome.class));
                            BtnSignUp.setEnabled(true);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            BtnSignUp.setEnabled(true);
                        }
                    }
                });
    }
}