package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SavedRecepies extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ListView listViewRecepies;
    ArrayList<RecepieDetails> AllRecepies;
    ArrayList<String> savedRecepies;
    SavedRecepiesAdapter adapter;
    BottomNavigationView bottomNavigationView;
    ArrayList<RecepieDetails> availableRecepies;
    ArrayList<String> userIngredients;
    SharedPreferences sharedPreferences;
    TextView stateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_recepies);
        InitViews();
        InitBottomNavigation();
        GetSavedRecepies();
    }

    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(SavedRecepies.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        dialog.setTitle("Sign Out")
                .setMessage("Are You Sure You Want To Sign Out?")
                .setNegativeButton("CLOSE", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(SavedRecepies.this, "Bye Bye", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(SavedRecepies.this, MainActivity.class));
                        dialog.dismiss();
                    }
                }).show();
    } // דיאלוג לוודא אם המשתמש רוצה להתנתק או לא



    private void InitViews() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        listViewRecepies = findViewById(R.id.list_view_SavedRecepies);
        sharedPreferences = getSharedPreferences("MaxMissing",MODE_PRIVATE);
        stateText = findViewById(R.id.StateText);
    }
    private void InitBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackgroundColor(Color.WHITE);
        bottomNavigationView.setSelectedItemId(R.id.SavedPage);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.IngredientsPage:
                        startActivity(new Intent(SavedRecepies.this, Welcome.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.RecepiesPage:
                        startActivity(new Intent(SavedRecepies.this, Recepies.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.SavedPage:
                        startActivity(new Intent(SavedRecepies.this, SavedRecepies.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.SettingsPage:
                        startActivity(new Intent(SavedRecepies.this, Settings.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                }
                return true;
            }
        });

    }
    private void GetAllRecepies() {
        AllRecepies = new ArrayList<>();
        myRef.child("Recepies").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData md : currentData.getChildren())
                {
                    RecepieDetails recepie = md.getValue(RecepieDetails.class);
                    recepie.SetID(md.getKey());
                    if (savedRecepies.contains(recepie.GetID()))
                        AllRecepies.add(recepie);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (AllRecepies.isEmpty())
                {
                    stateText.setText("No Favorite Recepies");
                    return;
                }
                GetUserIngredients();
            }
        });
    }
    public void GetSavedRecepies() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        savedRecepies = new ArrayList<>();
        myRef.child("UsersInfo").child(mAuth.getUid()).child("SavedRecepies").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData md : currentData.getChildren())
                {
                    savedRecepies.add(md.getValue().toString());
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                GetAllRecepies();
            }
        });
    }
    public void GetAvailableRecepies() {
        availableRecepies = new ArrayList<>();
        for (int i=0; i<AllRecepies.size(); i++)
        {
            for (int j=0; j<AllRecepies.get(i).GetIngredients().size(); j++)
            {
                String recepieIngredient = AllRecepies.get(i).GetIngredients().get(j);
                if (!userIngredients.contains(recepieIngredient)){
                    StringBuilder builder = new StringBuilder();
                    builder.append("*Missing: ");
                    builder.append(recepieIngredient);
                    ArrayList<String> temp = AllRecepies.get(i).GetIngredients();
                    temp.set(j, builder.toString());
                    AllRecepies.get(i).SetIngredients(temp);
                }
            }
            availableRecepies.add(AllRecepies.get(i));
        }
    }
    public void GetUserIngredients()
    {
        userIngredients = new ArrayList<>();
        myRef = database.getReference("UsersInfo").child(mAuth.getUid()).child("UserIngredients");
        myRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData md : currentData.getChildren()) {
                    userIngredients.add(md.getValue().toString());
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                GetAvailableRecepies();
                adapter = new SavedRecepiesAdapter(SavedRecepies.this, R.layout.card_test, availableRecepies, savedRecepies);
                listViewRecepies.setAdapter(adapter);
                stateText.setVisibility(View.GONE);
                listViewRecepies.setVisibility(View.VISIBLE);
            }
        });
    }
}