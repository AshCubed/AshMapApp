package com.vega.gade3.student17611612.ashmapapp;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.vega.gade3.student17611612.ashmapapp.Adapters.FavouriteLocationsAdapter;
import com.vega.gade3.student17611612.ashmapapp.ObjectClasses.ProfileObjectClass;
import com.vega.gade3.student17611612.ashmapapp.ObjectClasses.SavedLocationsClass;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    private ProfileFragment mViewModel;

    private static final String TAG = "ProfileFragment";
    EditText etName;
    EditText etEmail;
    Spinner favLocations;

    RelativeLayout profile_main;
    RelativeLayout profile_favLocations;
    RecyclerView recyclerView_FavLocations;

    Button btnFavLocations;
    Button btnProfileEdit;
    Button btnSave;
    Button btnLogOut;
    Button btnCloseFavList;

    String name;
    String email;

    boolean metricPlaceHolder;
    boolean imperialPlaceHolder;

    private FirebaseFirestore mDatabase;
    private FirebaseAuth mAuth;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase =  FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        profile_main = view.findViewById(R.id.profile_main);
        profile_favLocations = view.findViewById(R.id.profile_favLocations);

        recyclerView_FavLocations = view.findViewById(R.id.recyclerView_FavLocations);

        etName = view.findViewById(R.id.editTextTextPersonNameProf);
        etEmail = view.findViewById(R.id.editTextTextPersonEmailProf);
        favLocations = view.findViewById(R.id.spinnerFavLocTypeProf);
        btnFavLocations = view.findViewById(R.id.btnSavecLocationsProf);
        btnProfileEdit = view.findViewById(R.id.btnEditProf);
        btnSave = view.findViewById(R.id.btnSaveProf);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        btnCloseFavList = view.findViewById(R.id.btnCloseFavList);

        btnFavLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Method Call here
                expandFavListAnimation();
            }
        });

        btnCloseFavList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contractFavListAnimation();
            }
        });

        btnProfileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditMethod();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMethod();
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogOutMethod();
            }
        });

        btnSave.setVisibility(View.INVISIBLE);
        SetStartVariables();
        SpawnFavLocationCards();
    }

    private void SetStartVariables(){
        etName.setEnabled(false);
        etEmail.setEnabled(false);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.favLocationTypes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        favLocations.setOnItemSelectedListener(ProfileFragment.this);
        favLocations.setAdapter(adapter);
        favLocations.setEnabled(false);


        final String currentUserID = mAuth.getCurrentUser().getUid();
        final DocumentReference docRef = mDatabase.collection("Profiles").document(currentUserID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        etName.setText(document.get("Name").toString());
                        etEmail.setText(mAuth.getCurrentUser().getEmail().toString());
                        favLocations.setSelection(adapter.getPosition(document.get("FavLandMark").toString()));
                        metricPlaceHolder = document.getBoolean("Metric");
                        imperialPlaceHolder = document.getBoolean("Imperial");
                        //set fav landmarks
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

    private void LogOutMethod(){
        mAuth.signOut();
        Toast.makeText(getContext(), "Signed Out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    private void EditMethod(){
        btnFavLocations.setVisibility(View.INVISIBLE);
        btnProfileEdit.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.VISIBLE);
        /*etName.setText("");
        etEmail.setText("");*/

        etName.setEnabled(true);
        etEmail.setEnabled(true);

        favLocations.setEnabled(true);
    }

    private void SaveMethod(){
        name = etName.getText().toString();
        email = etEmail.getText().toString();
        //favLocations get text

        //should put a warning here for changing email
        mAuth.getCurrentUser().updateEmail(email);

        ProfileObjectClass PC = new ProfileObjectClass(name, selectedItem, metricPlaceHolder, imperialPlaceHolder);

        String currentUserID = mAuth.getCurrentUser().getUid();
        DocumentReference docRef = mDatabase.collection("Profiles").document(currentUserID);
        docRef.set(PC.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                btnProfileEdit.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.INVISIBLE);
                etName.setEnabled(false);
                etEmail.setEnabled(false);
                favLocations.setEnabled(false);
                btnFavLocations.setVisibility(View.VISIBLE);
                Log.d(TAG, "DocumentSnapshot successfully written!");
                Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error writing document", e);
                Toast.makeText(getContext(), "ERROR:Profile Not Updated", Toast.LENGTH_SHORT).show();
            }
        });

        hideKeyboard(ProfileFragment.this.getActivity());
    }

    private void SpawnFavLocationCards(){
        final ArrayList<String> locationNames = new ArrayList<>();
        final ArrayList<GeoPoint> geoPoints = new ArrayList<>();

        final CollectionReference collectionRef = mDatabase.collection("SavedLocations").
                document(mAuth.getCurrentUser().getUid()).collection("Locations");
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    final QuerySnapshot currentFoodDatabase = (QuerySnapshot) task.getResult();
                    for (DocumentSnapshot documentSnapshot : currentFoodDatabase.getDocuments()){
                        final SavedLocationsClass savedLocation = documentSnapshot.toObject(SavedLocationsClass.class);
                        locationNames.add(savedLocation.getLocationName());
                        geoPoints.add(savedLocation.getGeoPoint());
                    }
                    final String[] s1 = (String[]) locationNames.toArray(new String[locationNames.size()]);
                    GeoPoint[] s2 = (GeoPoint[]) geoPoints.toArray(new GeoPoint[geoPoints.size()]);

                    FavouriteLocationsAdapter myAdapter = new FavouriteLocationsAdapter(getContext(), s1);
                    recyclerView_FavLocations.setAdapter(myAdapter);
                    recyclerView_FavLocations.setLayoutManager(new LinearLayoutManager(getContext()));
                    myAdapter.setOnItemClickListener(new FavouriteLocationsAdapter.OnItemClickListener() {
                        @Override
                        public void onDirectToClick(int position) {
                            FragmentTransaction t = getParentFragmentManager().beginTransaction();
                            SearchFragment mFrag = new SearchFragment();

                            Bundle b = new Bundle();
                            b.putString("LocationName", s1[position]);
                            mFrag.setArguments(b);
                            t.replace(R.id.fragment_container, mFrag);
                            t.commit();
                        }

                        @Override
                        public void onDeleteClick(int position) {
                            SavedLocationsClass savedLocationsClass = new
                                    SavedLocationsClass(locationNames.get(position),
                                                        geoPoints.get(position));
                            savedLocationsClass.RemoveFromDatabase(mAuth.getCurrentUser().getUid());
                            SpawnFavLocationCards();
                        }
                    });
                }
            }
        });
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

    private void expandFavListAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(profile_favLocations);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                0,
                100);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(profile_main);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                100,
                0);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    private void contractFavListAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(profile_favLocations);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                0);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(profile_main);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                0,
                100);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    String selectedItem;
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