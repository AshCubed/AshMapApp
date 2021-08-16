package com.vega.gade3.student17611612.ashmapapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vega.gade3.student17611612.ashmapapp.ObjectClasses.ProfileObjectClass;

import java.util.List;

public class RegistrationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String name;
    String eMail;
    String password;
    Boolean metric;
    Boolean imperial;

    EditText etCreateName;
    EditText etCreateEmail;
    EditText etCreatePassword;
    Spinner spinFavLandmarkType;

    Switch measurement;

    /*CheckBox cbMetric;
    CheckBox cbImperial;*/
    Button btnCreateAccount;
    Button btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private static final String TAG = "ProfileFragment";
    List<EditText> things;
    String selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        mDatabase =  FirebaseFirestore.getInstance();


        etCreateName = findViewById(R.id.editTextTextPersonNameReg);
        etCreateEmail = findViewById(R.id.editTextTextEmailAddressReg);
        etCreatePassword = findViewById(R.id.editTextTextPasswordReg);
/*        cbMetric = findViewById(R.id.cbMetrick2);
        cbImperial = findViewById(R.id.cbImperial2);*/
        measurement = findViewById(R.id.switchMeasurementReg);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnBack = findViewById(R.id.btnBack);
        spinFavLandmarkType = findViewById(R.id.spinner);

        metric = true;
        imperial = false;
        measurement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    imperial = true;
                    metric = false;
                }
                else{
                    metric = true;
                    imperial = false;
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegistrationActivity.this.BackButtonMethod();
            }
        });
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(RegistrationActivity.this);
                RegistrationActivity.this.CreateAccount();
            }
        });

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(RegistrationActivity.this,
                R.array.favLocationTypes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinFavLandmarkType.setOnItemSelectedListener(RegistrationActivity.this);
        spinFavLandmarkType.setAdapter(adapter);
        selectedItem = spinFavLandmarkType.getSelectedItem().toString();
    }

    private void BackButtonMethod(){
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void CreateAccount(){
        name = etCreateName.getText().toString();
        eMail = etCreateEmail.getText().toString();
        password = etCreatePassword.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(eMail) ||TextUtils.isEmpty(password) ){
            Toast.makeText(RegistrationActivity.this, "Some fields are empty", Toast.LENGTH_LONG).show();
        }
        else{
            mAuth.createUserWithEmailAndPassword(eMail, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                //Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                ProfileObjectClass PC = new ProfileObjectClass(name, selectedItem, metric, imperial);
                                mDatabase.collection("Profiles").document(mAuth.getUid()).set(PC.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });

                                mAuth.signInWithEmailAndPassword(eMail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                        Toast.makeText(RegistrationActivity.this, "Registration Authentication Success.",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(intent);
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegistrationActivity.this,
                            "Registration Authentication failed." + "\n" + e.getLocalizedMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.spinnerFavLocTypeProf:
                selectedItem = parent.getSelectedItem().toString();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}