package com.vega.gade3.student17611612.ashmapapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    Switch switchMetricImperial;
    boolean metric;
    boolean imperial;

    private FirebaseFirestore mDatabase;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private static final String TAG = "SettingsFragment";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase =  FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        switchMetricImperial = view.findViewById(R.id.switchMeasurementSetting);
        switchMetricImperial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsFragment.this.SetMetrics(isChecked);
            }
        });

        currentUserID = mAuth.getCurrentUser().getUid();
        final DocumentReference docRef = mDatabase.collection("Profiles").document(currentUserID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        metric = document.getBoolean("Metric");
                        imperial = document.getBoolean("Imperial");
                        if (imperial){
                            switchMetricImperial.setChecked(true);
                        }
                        else if (metric){
                            switchMetricImperial.setChecked(false);
                        }
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    }
                    else {
                        Log.d(TAG, "No such document");
                    }
                }
                else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void SetMetrics(boolean isChecked){
        if (isChecked == true){
            imperial = true;
            metric = false;
            Toast.makeText(this.getContext(), "Set to Imperial", Toast.LENGTH_SHORT).show();
        }
        else{
            imperial = false;
            metric = true;
            Toast.makeText(this.getContext(), "Set to Metric", Toast.LENGTH_SHORT).show();
        }
        DocumentReference docRef = mDatabase.collection("Profiles").document(currentUserID);
        docRef.update("Metric", metric);
        docRef.update("Imperial", imperial);
    }
}


