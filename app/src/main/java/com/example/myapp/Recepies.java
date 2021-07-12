package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
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
import java.util.HashMap;
import java.util.Iterator;

public class Recepies extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ListView listViewRecepies;
    ListView listViewAll;
    ArrayList<RecepieDetails> AllRecepies;
    ArrayList<String> savedRecepies;
    RecepiesAdapter adapter;
    RecepiesAdapter adapterNoFilter;
    BottomNavigationView bottomNavigationView;
    ArrayList<String> userIngredients;
    ArrayList<RecepieDetails> availableRecepies;
    SharedPreferences sharedPreferences;
    TextView stateText;
    MaterialButton btnShowAll;
    ArrayList<RecepieDetails> ShowAllRecepies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recepies);
        InitViews();
        InitBottomNavigation();
        GetRecepies();
        GetAllRecepiesNoFilter();
    }

    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(Recepies.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        dialog.setTitle("Sign Out")
                .setMessage("Are You Sure You Want To Sign Out?")
                .setNegativeButton("CLOSE", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Recepies.this, "Bye Bye", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(Recepies.this, MainActivity.class));
                        dialog.dismiss();
                    }
                }).show();
    } // דיאלוג לוודא אם המשתמש רוצה להתנתק או לא

    private void GetRecepies() {
        //Getting User Must Contain Ingredients
        savedRecepies = new ArrayList<>();
        myRef.child("UsersInfo").child(mAuth.getUid()).child("UserMustIngredients").runTransaction(new Transaction.Handler() {
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

    private void InitViews() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        listViewRecepies = findViewById(R.id.list_view_recepies);
        sharedPreferences = getSharedPreferences("MaxMissing",MODE_PRIVATE);
        stateText = findViewById(R.id.StateText);
        btnShowAll = findViewById(R.id.btnShowAll);
        ShowAllRecepies();
        listViewAll = findViewById(R.id.list_view_all);
    }

    private void InitBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackgroundColor(Color.WHITE);
        bottomNavigationView.setSelectedItemId(R.id.RecepiesPage);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.IngredientsPage:
                        startActivity(new Intent(Recepies.this, Welcome.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.RecepiesPage:
                        startActivity(new Intent(Recepies.this, Recepies.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.SavedPage:
                        startActivity(new Intent(Recepies.this, SavedRecepies.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.SettingsPage:
                        startActivity(new Intent(Recepies.this, Settings.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
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
                    if (recepie.GetIngredients().containsAll(savedRecepies))
                    {
                        recepie.SetID(md.getKey());
                        AllRecepies.add(recepie);
                    }
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                GetSavedRecepies();
            }
        });
    }

    private void GetAllRecepiesNoFilter() {
        ShowAllRecepies = new ArrayList<>();
        myRef.child("Recepies").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData md : currentData.getChildren())
                {
                    RecepieDetails recepie = md.getValue(RecepieDetails.class);
                    recepie.SetID(md.getKey());
                    ShowAllRecepies.add(recepie);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                adapterNoFilter = new RecepiesAdapter(Recepies.this, R.layout.card_test, ShowAllRecepies, savedRecepies, userIngredients);
                listViewAll.setAdapter(adapterNoFilter);
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
                GetUserIngredients();
            }
        });
    }

    public void GetUserIngredients() {
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
                GetAvailableRecepies(sharedPreferences.getInt("max",0));
                if(availableRecepies.size() == 0)
                {
                    stateText.setText("No Matching Recepies");
                    return;
                }
                adapter = new RecepiesAdapter(Recepies.this, R.layout.card_test, availableRecepies, savedRecepies, userIngredients);
                listViewRecepies.setAdapter(adapter);
                stateText.setVisibility(View.GONE);
                listViewRecepies.setBackgroundColor(Color.WHITE);
                listViewRecepies.setVisibility(View.VISIBLE);
                adapterNoFilter = new RecepiesAdapter(Recepies.this, R.layout.card_test, ShowAllRecepies, savedRecepies, userIngredients);
                listViewAll.setAdapter(adapterNoFilter);
            }
        });
    }

    public void GetAvailableRecepies(int maxMissing) {
        availableRecepies = new ArrayList<>();
        for (int i=0; i<AllRecepies.size(); i++)
        {
            int missingIngredientsCounter = 0;
            for (int j=0; j<AllRecepies.get(i).GetIngredients().size(); j++)
            {
                String recepieIngredient = AllRecepies.get(i).GetIngredients().get(j);
                if (!userIngredients.contains(recepieIngredient)){
                    missingIngredientsCounter++;
                    StringBuilder builder = new StringBuilder();
                    builder.append("*Missing: ");
                    builder.append(AllRecepies.get(i).GetIngredients().get(j));
                    ArrayList<String> temp = AllRecepies.get(i).GetIngredients();
                    temp.set(j, builder.toString());
                    AllRecepies.get(i).SetIngredients(temp);
                    if (missingIngredientsCounter > maxMissing)
                    {
                        break;
                    }
                }
            }
            if(missingIngredientsCounter <= maxMissing)
            {
                availableRecepies.add(AllRecepies.get(i));
            }
        }
    }

    public void ShowAllRecepies(){
        btnShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnShowAll.getText().toString().equals("Show All"))
                {
                    stateText.setVisibility(View.GONE);
                    listViewRecepies.setVisibility(View.GONE);
                    listViewAll.setBackgroundColor(Color.WHITE);
                    listViewAll.setVisibility(View.VISIBLE);
                    btnShowAll.setText("Show Matches");
                }
                else
                {
                    stateText.setVisibility(View.GONE);
                    listViewAll.setVisibility(View.GONE);
                    listViewRecepies.setVisibility(View.VISIBLE);
                    listViewRecepies.setBackgroundColor(Color.WHITE);
                    btnShowAll.setText("Show All");
                }
            }
        });
    }
}