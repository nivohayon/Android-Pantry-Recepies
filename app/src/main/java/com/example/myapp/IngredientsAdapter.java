package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;

public class IngredientsAdapter extends ArrayAdapter<Ingredient> {
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private  Context mContext;
    private ArrayList<Ingredient> ingredientsList;
    ArrayList<String> savedIngredients;
    public IngredientsAdapter(@NonNull Context context, int resource, ArrayList<Ingredient> arrayList, ArrayList<String> savedIngredients) {
        super(context, resource, arrayList);
        mContext = context;
        ingredientsList = arrayList;
        this.savedIngredients = savedIngredients;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // convertView which is recyclable view
        View currentItemView = convertView;

        // of the recyclable view is null then inflate the custom layout for the same
        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.ingredients_card, parent, false);
        }

        // get the position of the view from the ArrayAdapter
        Ingredient currentIngredientPosition = getItem(position);

        // then according to the position of the view assign the desired image for the same
        ImageView ingredientImage = currentItemView.findViewById(R.id.IngredientImage);
        assert currentIngredientPosition != null;
        Picasso.get().load(currentIngredientPosition.GetPicUrl()).into(ingredientImage);

        CheckBox ingredientName = currentItemView.findViewById(R.id.IngredientName);
        ingredientName.setText(currentIngredientPosition.GetName());
        if (savedIngredients.contains(currentIngredientPosition.GetName()))
            ingredientName.setChecked(true);
        else
            ingredientName.setChecked(false);

        ingredientName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = v.findViewById(R.id.IngredientName);
                mAuth = FirebaseAuth.getInstance();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference();
                if (cb.isChecked()) {
                    savedIngredients.add(cb.getText().toString());
                    myRef.child("UsersInfo").child(mAuth.getUid()).child("UserIngredients").push().setValue(cb.getText().toString());
                } else {
                    savedIngredients.remove(cb.getText().toString());
                    Query applesQuery = myRef.child("UsersInfo").child(mAuth.getUid()).child("UserIngredients").orderByValue().equalTo(cb.getText().toString());
                    applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                appleSnapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
        ingredientName.setTextColor(Color.BLACK);

        // then return the recyclable view
        return currentItemView;
    }
}
