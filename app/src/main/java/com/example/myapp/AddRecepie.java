package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionScene;

import android.os.Bundle;
import android.view.DragAndDropPermissions;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;

public class AddRecepie extends AppCompatActivity {
    ListView listViewIngredients;
    ArrayList<Ingredient> AllIngredients;
    ArrayList<String> CheckedIngredients;
    FirebaseDatabase database;
    DatabaseReference myRef;
    AddRecepieAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recepie);
        Button ADDrecepie = findViewById(R.id.AddRecepieBTN);
        CheckedIngredients = new ArrayList<>();
        InitIngredients();
//        ADDrecepie.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GetCheckedIngredients();
//                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                DatabaseReference myRef = database.getReference();
//                EditText image = findViewById(R.id.imageUrl);
//                EditText recepieName = findViewById(R.id.recepieName);
//                EditText recepieMakingTime = findViewById(R.id.recepieMakingTime);
//                EditText recepieServings = findViewById(R.id.recepieServings);
//                EditText url = findViewById(R.id.url);
//                RecepieDetails recepie = new RecepieDetails();
//                recepie.SetImage(image.getText().toString());
//                recepie.SetRecepieName(recepieName.getText().toString());
//                recepie.SetRecepieMakingTime(recepieMakingTime.getText().toString());
//                recepie.SetRecepieServings(recepieServings.getText().toString());
//                recepie.SetBtnUrl(url.getText().toString());
//                myRef.child("temp").runTransaction();
//                recepie.SetIngredients(CheckedIngredients);
//                myRef = database.getReference("Recepies").push();
//                myRef.setValue(recepie);
//            }
//        });
    }

    private void GetCheckedIngredients() {

    }

    public void onAddRecepie(View view) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        EditText image = findViewById(R.id.imageUrl);
        EditText recepieName = findViewById(R.id.recepieName);
        EditText recepieMakingTime = findViewById(R.id.recepieMakingTime);
        EditText recepieServings = findViewById(R.id.recepieServings);
        EditText url = findViewById(R.id.url);
        myRef.child("temp").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData md : currentData.getChildren())
                {
                    CheckedIngredients.add(md.getValue(String.class));
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();
                myRef.child("temp").setValue(null);
                RecepieDetails recepie = new RecepieDetails(
                        image.getText().toString(),
                        recepieName.getText().toString(),
                        recepieMakingTime.getText().toString(),
                        recepieServings.getText().toString(),
                        url.getText().toString(),
                        CheckedIngredients
                );
                myRef = database.getReference();
                myRef.child("Recepies").push().setValue(recepie);
            }
        });
    }
    private void InitIngredients() {
        listViewIngredients = findViewById(R.id.list_view_ingredients_add);
        AllIngredients = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
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
                adapter = new AddRecepieAdapter(AddRecepie.this, R.layout.ingredients_card, AllIngredients, CheckedIngredients);
                listViewIngredients.setAdapter(adapter);
            }
        });
    }// טעינת ה ListView
}