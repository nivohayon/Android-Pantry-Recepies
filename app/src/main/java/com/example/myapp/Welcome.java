package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Welcome extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ListView listViewIngredients;
    IngredientsAdapter adapter;
    BottomNavigationView bottomNavigationView;
    ArrayList<Ingredient> AllIngredients;
    ArrayList<String> userIngredientsList;
    TextView loadingText;
    EditText searchBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        InitViews();
        InitBottomNavigation();
        InitIngredients();
    }

    private void InitViews() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        listViewIngredients = findViewById(R.id.list_view_ingredients);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        AllIngredients = new ArrayList<>();
        userIngredientsList = new ArrayList<>();
        loadingText = findViewById(R.id.LoadingText);
        searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Search();
            }
        });
    }

    private void Search() {
        String qurey = searchBar.getText().toString().toLowerCase();
        if (qurey.length() == 0)
        {
            adapter = new IngredientsAdapter(Welcome.this, R.layout.ingredients_card, AllIngredients, userIngredientsList);
            listViewIngredients.setAdapter(adapter);
            return;
        }
        ArrayList<Ingredient> filteredIngredients = new ArrayList<>();
        for(int i = 0; i < AllIngredients.size(); i++)
        {
            if (AllIngredients.get(i).GetName().toLowerCase().contains(qurey))
                filteredIngredients.add(AllIngredients.get(i));
        }
        adapter = new IngredientsAdapter(Welcome.this, R.layout.ingredients_card, filteredIngredients, userIngredientsList);
        listViewIngredients.setAdapter(adapter);
    }

    private void InitBottomNavigation() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackgroundColor(Color.WHITE);
        bottomNavigationView.setSelectedItemId(R.id.IngredientsPage);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.IngredientsPage:
                        startActivity(new Intent(Welcome.this, Welcome.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.RecepiesPage:
                        startActivity(new Intent(Welcome.this, Recepies.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.SavedPage:
                        startActivity(new Intent(Welcome.this, SavedRecepies.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.SettingsPage:
                        startActivity(new Intent(Welcome.this, Settings.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(Welcome.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        dialog.setTitle("Sign Out")
                .setMessage("Are You Sure You Want To Sign Out?")
                .setNegativeButton("CLOSE", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Welcome.this, "Bye Bye", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(Welcome.this, MainActivity.class));
                        dialog.dismiss();
                    }
                }).show();
    } // דיאלוג לוודא אם המשתמש רוצה להתנתק או לא

    private void InitIngredients() {
        listViewIngredients = findViewById(R.id.list_view_ingredients);
        AllIngredients = new ArrayList<>();
        myRef = database.getReference().child("Ingredients");
        myRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData md : currentData.getChildren()) {
                    Ingredient recepie = md.getValue(Ingredient.class);
                    AllIngredients.add(recepie);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                GetUserIngredients();
            }
        });
    }// טעינת ה ListView

    public void GetUserIngredients() {
        userIngredientsList = new ArrayList<>();
        myRef = database.getReference("UsersInfo").child(mAuth.getUid()).child("UserIngredients");
        myRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData md : currentData.getChildren()) {
                    userIngredientsList.add(md.getValue().toString());
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                adapter = new IngredientsAdapter(Welcome.this, R.layout.ingredients_card, AllIngredients, userIngredientsList);
                listViewIngredients.setAdapter(adapter);
                loadingText.setVisibility(View.GONE);
                listViewIngredients.setVisibility(View.VISIBLE);
            }
        });
    }// מחזירה את המרכיבים ששמורים למשתמש ב Firebase
}