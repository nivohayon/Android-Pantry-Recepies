package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Settings extends AppCompatActivity {
    MaterialButton btnLogOut;
    FirebaseAuth mAuth;
    Slider maxMissing;
    SharedPreferences sharedPreferences;
    MaterialButton btnIngredientsListUp;
    MaterialButton btnIngredientsListDown;
    ListView listViewIngredients;
    ArrayList<Ingredient> AllIngredients;
    ArrayList<String> savedIngredients;
    FirebaseDatabase database;
    DatabaseReference myRef;
    MustIngredientsAdapter adapter;
    TextView stateText;
    ImageView btnWhatsApp;
    ImageView btnGmail;
    ImageView btnSms;
    TextView signedInByText;
    TextView signedInByTextEmail;
    EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        InitViews();
        InitOnClicks();
        InitBottomNavigation();
        InitIngredients();
    }

    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(Settings.this, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        dialog.setTitle("Sign Out")
                .setMessage("Are You Sure You Want To Sign Out?")
                .setNegativeButton("CLOSE", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Settings.this, "Bye Bye", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(Settings.this, MainActivity.class));
                        dialog.dismiss();
                    }
                }).show();
    } // דיאלוג לוודא אם המשתמש רוצה להתנתק או לא


    private void InitBottomNavigation() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackgroundColor(Color.WHITE);
        bottomNavigationView.setSelectedItemId(R.id.SettingsPage);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                sharedPreferences.edit().putInt("max", (int)maxMissing.getValue()).apply();
                switch (item.getItemId()) {
                    case R.id.IngredientsPage:
                        startActivity(new Intent(Settings.this, Welcome.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.RecepiesPage:
                        startActivity(new Intent(Settings.this, Recepies.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.SavedPage:
                        startActivity(new Intent(Settings.this, SavedRecepies.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                    case R.id.SettingsPage:
                        startActivity(new Intent(Settings.this, Settings.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        break;
                }
                return true;
            }
        });

    }

    private void InitIngredients() {
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
                GetSavedMustIngredients();
            }
        });
    }
    public void GetSavedMustIngredients() {
        savedIngredients = new ArrayList<>();
        myRef = database.getReference("UsersInfo").child(mAuth.getUid()).child("UserMustIngredients");
        myRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData md : currentData.getChildren()) {
                    savedIngredients.add(md.getValue().toString());
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                adapter = new MustIngredientsAdapter(Settings.this, R.layout.ingredients_card, AllIngredients, savedIngredients);
                listViewIngredients.setAdapter(adapter);
            }
        });
    }

    private void InitOnClicks() {
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(Settings.this).create();
                alertDialog.setTitle("Sign Out");
                alertDialog.setMessage("Are You Sure You Want To Sign Out?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mAuth.signOut();
                                Toast.makeText(getApplicationContext(), "Signed Out", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(Settings.this, MainActivity.class));
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
            }
        });
        btnIngredientsListDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewIngredients.setVisibility(View.VISIBLE);
                btnIngredientsListDown.setVisibility(View.GONE);
                btnIngredientsListUp.setVisibility(View.VISIBLE);
                searchBar.setVisibility(View.VISIBLE);
            }
        });
        btnIngredientsListUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewIngredients.setVisibility(View.GONE);
                btnIngredientsListUp.setVisibility(View.GONE);
                btnIngredientsListDown.setVisibility(View.VISIBLE);
                searchBar.setVisibility(View.GONE);
            }
        });
        btnWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Settings.this, "Send Me Recepies Ideas", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://wa.me/9720526327537?text=Hey, I Have A Recepie Suggestion..."));
                intent.setPackage("com.whatsapp");
                startActivity(intent);
            }
        });
        btnGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String[] strTo = { "nivohayon1582@gmail.com" };
                intent.putExtra(Intent.EXTRA_EMAIL, strTo);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Pantry Recepies");
                intent.putExtra(Intent.EXTRA_TEXT, "Say Something");
                intent.setType("message/rfc822");
                intent.setPackage("com.google.android.gm");
                startActivity(intent);
            }
        });
        btnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:"+" 0526327537")); // This ensures only SMS apps respond
                intent.putExtra("sms_body", "say something");
                startActivity(intent);
            }
        });
    }

    private void Search() {
        String qurey = searchBar.getText().toString().toLowerCase();
        if (qurey.length() == 0)
        {
            adapter = new MustIngredientsAdapter(Settings.this, R.layout.ingredients_card, AllIngredients, savedIngredients);
            listViewIngredients.setAdapter(adapter);
            return;
        }
        ArrayList<Ingredient> filteredIngredients = new ArrayList<>();
        for(int i = 0; i < AllIngredients.size(); i++)
        {
            if (AllIngredients.get(i).GetName().toLowerCase().contains(qurey))
                filteredIngredients.add(AllIngredients.get(i));
        }
        adapter = new MustIngredientsAdapter(Settings.this, R.layout.ingredients_card, filteredIngredients, savedIngredients);
        listViewIngredients.setAdapter(adapter);
    }

    private void InitViews() {
        btnLogOut = findViewById(R.id.btnLogOut);
        listViewIngredients = findViewById(R.id.list_view_ingredientsMust);
        mAuth = FirebaseAuth.getInstance();
        maxMissing = findViewById(R.id.maxMissing);
        btnIngredientsListUp = findViewById(R.id.btnIngredientsListUp);
        btnIngredientsListDown = findViewById(R.id.btnIngredientsListDown);
        savedIngredients = new ArrayList<>();
        sharedPreferences = getSharedPreferences("MaxMissing",MODE_PRIVATE);
        maxMissing.setValue(sharedPreferences.getInt("max",0));
        database = FirebaseDatabase.getInstance();
        stateText = findViewById(R.id.StateText);
        btnWhatsApp = findViewById(R.id.btnWhatsApp);
        btnGmail = findViewById(R.id.btnGmail);
        btnSms = findViewById(R.id.btnSms);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        signedInByText = findViewById(R.id.SignedInByText);
        signedInByTextEmail = findViewById(R.id.SignedInByTextEmail);
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
        myRef.child("UsersInfo").child(mAuth.getUid()).child("FirstName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                signedInByText.setText(String.format("Signed In As: %s", snapshot.getValue().toString()));
                signedInByTextEmail.setText(mAuth.getCurrentUser().getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}