package com.example.myapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.transition.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;
import java.security.AllPermission;
import java.util.ArrayList;
import java.util.Iterator;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;

public class RecepiesAdapter extends ArrayAdapter<RecepieDetails> {
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Context mContext;
    ArrayList<RecepieDetails> recepiesList;
    ArrayList<String> savedRecepies;
    ArrayList<String> userIngredients;
    View currentItemView;
    public RecepiesAdapter(@NonNull Context context, int resource, ArrayList<RecepieDetails> recepiesList, ArrayList<String> savedRecepies, ArrayList<String> userIngredients) {
        super(context, resource, recepiesList);
        mContext = context;
        this.recepiesList = recepiesList;
        this.savedRecepies = savedRecepies;
        this.userIngredients = userIngredients;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // convertView which is recyclable view
        currentItemView = convertView;
        // of the recyclable view is null then inflate the custom layout for the same
        if (convertView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.card_test, parent, false);
        }

        // get the position of the view from the ArrayAdapter
        RecepieDetails currentRecepiePosition = getItem(position);

        // then according to the position of the view assign the desired image for the same
        ImageView RecepieImage = currentItemView.findViewById(R.id.RecepieImageID);
        assert currentRecepiePosition != null;

        Picasso.get().load(currentRecepiePosition.GetImage()).into(RecepieImage);

        TextView RecepieName = currentItemView.findViewById(R.id.RecepieName);
        RecepieName.setText(currentRecepiePosition.GetRecepieName());

        TextView RecepieMakingTime = currentItemView.findViewById(R.id.RecepieMakingTime);
        RecepieMakingTime.setText(String.format("Making Time: %s", currentRecepiePosition.GetRecepieMakingTime()));

        TextView RecepieServings = currentItemView.findViewById(R.id.RecepieServings);
        RecepieServings.setText(String.format("Servings: %s",currentRecepiePosition.GetRecepieServings()));

        StringBuilder RecepieIngredientsText = new StringBuilder();
        for (String item : currentRecepiePosition.GetIngredients())
        {
            RecepieIngredientsText.append("• " + item + "\n");
        }

        MaterialButton btnShowMore = currentItemView.findViewById(R.id.btnShowMore);

        btnShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(mContext, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
                dialog.setTitle(currentRecepiePosition.GetRecepieName())
                        .setMessage(RecepieIngredientsText.toString())
                        .setNeutralButton("Close", null)
                        .setPositiveButton("Instructions", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(), "Get Ready To Cook!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                intent.setData(Uri.parse(currentRecepiePosition.GetBtnUrl()));
                                mContext.startActivity(intent);
                            }
                        }).show();
            }
        });

        CheckBox SaveSwitch = currentItemView.findViewById(R.id.SaveRecepie);
        if (savedRecepies.contains(currentRecepiePosition.GetID())) {
            SaveSwitch.setChecked(true);
        }
        else {
            SaveSwitch.setChecked(false);
        }

        SaveSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox saveSwitch = v.findViewById(R.id.SaveRecepie);
                mAuth = FirebaseAuth.getInstance();
                database = FirebaseDatabase.getInstance();
                myRef = database.getReference();
                if (saveSwitch.isChecked()) {
                    savedRecepies.add(currentRecepiePosition.GetID());
                    myRef.child("UsersInfo").child(mAuth.getUid()).child("SavedRecepies").push().setValue(currentRecepiePosition.GetID());
                } else {
                    savedRecepies.remove(currentRecepiePosition.GetID()
                    );
                    Query applesQuery = myRef.child("UsersInfo").child(mAuth.getUid()).child("SavedRecepies").orderByValue().equalTo(currentRecepiePosition.GetID());
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
        // then return the recyclable view
        return currentItemView;
    }
}
