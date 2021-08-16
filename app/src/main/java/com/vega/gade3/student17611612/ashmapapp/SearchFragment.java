package com.vega.gade3.student17611612.ashmapapp;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.vega.gade3.student17611612.ashmapapp.Adapters.CustomInfoWindowAdapter;
import com.vega.gade3.student17611612.ashmapapp.ObjectClasses.SavedLocationsClass;
import com.vega.gade3.student17611612.ashmapapp.ObjectClasses.UserLocationClass;
import com.vega.gade3.student17611612.ashmapapp.services.LocationService;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class SearchFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnPoiClickListener{

    private static final String TAG = "SearchFragment";
    private static final float DEFAULT_ZOOM = 15f;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136)
    );
    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    private int mMapLayoutState = 1;

    FusedLocationProviderClient mFusedLocationClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    private GoogleMap mGoogleMap;
    private GeoApiContext mGeoApiContext = null;
    private LatLngBounds mMapBoundary;
    private UserLocationClass mUserLocation;
    private Marker userMarker;

    private PlacesClient placesClient;
    private ArrayList<PolylineData> mPolylineData = new ArrayList<>();

    private RelativeLayout mMapContainer;
    private RelativeLayout mDirectionsView;

    private Button btnCloseDirections;
    private Button btnDirections;
    private Button btnFavourite;
    private TextView mLocationName;
    private TextView mDirectionInfo;
    private EditText mSearchText;
    private ImageView mGps;
    private ImageView mInfo;

    private boolean isMetric;
    private boolean isImperial;

    private String passedThroughLocationName = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle b = getArguments();
        if (b != null){
            String locationName = b.getString("LocationName");
            if (locationName != null){
                passedThroughLocationName = b.getString("LocationName");
            }
        }
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        if (mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_api_key))
                    .build();
        }
        mMapContainer = (RelativeLayout) this.requireActivity().findViewById(R.id.map_container);
        mDirectionsView = (RelativeLayout) this.requireActivity().findViewById(R.id.directions_container);
        mLocationName = (TextView) this.requireActivity().findViewById(R.id.txtLNAME);
        mDirectionInfo  = (TextView) this.requireActivity().findViewById(R.id.txtDirectionI);
        btnDirections = (Button) requireActivity().findViewById(R.id.btnDirections);
        btnCloseDirections = (Button) requireActivity().findViewById(R.id.directions_close);
        btnFavourite = (Button) requireActivity().findViewById(R.id.btnFavLocation);
        mSearchText = (EditText) getActivity().findViewById(R.id.input_search);
        mGps = (ImageView) this.requireActivity().findViewById(R.id.ic_gps);
        mInfo = (ImageView) this.requireActivity().findViewById(R.id.ic_info);
        init();

    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                //Toast.makeText(getContext(), "Perms granted", Toast.LENGTH_SHORT).show();
            }
            else {
                //Toast.makeText(getContext(), "No Perms", Toast.LENGTH_SHORT).show();
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            mGoogleMap.setOnInfoWindowClickListener(SearchFragment.this);
            mGoogleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(SearchFragment.this.getContext()));
            mGoogleMap.setOnPolylineClickListener(SearchFragment.this);
            mGoogleMap.setOnPoiClickListener(SearchFragment.this);

            if (passedThroughLocationName != null){
                geoLocate(passedThroughLocationName);
                passedThroughLocationName = null;
                GetLastKnownLocation(false);
            }
            else {
                GetLastKnownLocation(true);
            }
            //startLocationService();
        }
    };

    @Override
    public void onInfoWindowClick(final Marker marker) {

        if(mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED){
            mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
            contractMapAnimation();

            String locationName = marker.getTitle();
            mLocationName.setText(locationName);
            Log.d(TAG, "mLocationName: " + mLocationName.getText().toString());

            btnDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calculateDirections(marker);
                }
            });

            btnFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GeoPoint geoPoint =
                            new GeoPoint(marker.getPosition().latitude, marker.getPosition().latitude);
                    SavedLocationsClass savedLocation
                            = new SavedLocationsClass(marker.getTitle(), geoPoint);
                    savedLocation.SendToDatabase(mAuth.getCurrentUser().getUid().toString());
                    Toast.makeText(getContext(), "Location Saved", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void init(){
        Log.d(TAG, "init: initializing");

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    String searchString = mSearchText.getText().toString();
                    mSearchText.setText(" ");
                    hideKeyboard(SearchFragment.this.getActivity());
                    geoLocate(searchString);
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked gps icon");
                mGoogleMap.clear();
                //mLocationName.setText("");
                //mDirectionInfo.setText("");
                GetLastKnownLocation(true);
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked place info");
                try{
                    if(userMarker.isInfoWindowShown()){
                        userMarker.hideInfoWindow();
                    }else{
                        Log.d(TAG, "onClick: place info: " /*+ mPlace.toString()*/);
                        userMarker.showInfoWindow();
                    }
                }catch (NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException: " + e.getMessage() );
                }
            }
        });

        btnCloseDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED){
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                    expandMapAnimation();
                    mGoogleMap.clear();
                    GetLastKnownLocation(true);
                    //mLocationName.setText("");
                    //mDirectionInfo.setText("");
                }
            }
        });

        final DocumentReference docRef = mDatabase.collection("Profiles").document(mAuth.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        isMetric = document.getBoolean("Metric");
                        isImperial = document.getBoolean("Imperial");
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

        //hideSoftKeyBoard();
    }

    private void geoLocate(String searchString){
        Log.d(TAG, "geoLocate: geo locating");
        mGoogleMap.clear();

        mGoogleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(SearchFragment.this.getContext()));

       /* String searchString = mSearchText.getText().toString();
        mSearchText.setText(" ");*/

        Geocoder geocoder = new Geocoder(SearchFragment.this.getActivity().getApplicationContext());
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (Exception e) {
            Log.e(TAG, "geoLocateL IOException: " + e.getMessage() + e.getCause());
        }

        if (list.size() > 0){
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found a location: " + address.toString());

            AddMapMarker(new LatLng(address.getLatitude(), address.getLongitude()),
                    address.getAddressLine(0) , DEFAULT_ZOOM);
        }
    }

    private void GetLastKnownLocation(final boolean moveCam) {
        Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "onComplete: latitude: " + geoPoint.getLatitude());
                    Log.d(TAG, "onComplete: longitude: " + geoPoint.getLongitude());

                    mUserLocation = new UserLocationClass();
                    mUserLocation.setGeoPoint(geoPoint);
                    mUserLocation.setTimeStamp(null);
                    mUserLocation.setUserID(mAuth.getCurrentUser().getUid().toString());
                    //AddMapMarker(location, "Current Locale");
                    SaveUserLocation();
                    if (moveCam){
                        SetMapCamera(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM);
                    }
                }
            }
        });
    }

    private void AddMapMarker(LatLng latLng, String text, float zoom) {
        //LatLng test = new LatLng(location.getLatitude(), location.getLongitude());
        userMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(text + "\n"));

        if (zoom != 0){
            SetMapCamera(latLng, zoom);
        }
    }

    private void UpdateMarker(LatLng latLng, String text) {
        userMarker.setPosition(latLng);
        userMarker.setTitle(text);
        Location location = null;
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        SetMapCamera(latLng, DEFAULT_ZOOM);
    }

    private void SetMapCamera(LatLng latLng, float zoom) {
        //overall map view window: 0.2 * 0.2 = 0.04
/*        double bottomBoundary = latLng.latitude - .1;
        double leftBoundary = latLng.longitude - .1;
        double topBoundary = latLng.latitude + .1;
        double rightBoundary = latLng.longitude + .1;

        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));*/
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        //hideSoftKeyBoard();
    }

    private void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");

        //Hide Info Window when looking for directions
        Log.d(TAG, "onClick: clicked place info");
        try{
            if(marker.isInfoWindowShown()){
                marker.hideInfoWindow();
            }else{
                Log.d(TAG, "onClick: place info: " /*+ mPlace.toString()*/);
                marker.showInfoWindow();
            }
        }catch (NullPointerException e){
            Log.e(TAG, "onClick: NullPointerException: " + e.getMessage() );
        }

        //Directions Code
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mUserLocation.getGeoPoint().getLatitude(),
                        mUserLocation.getGeoPoint().getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolylinesToMap(result);
            }
            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                if (mPolylineData.size() > 0){
                    for (PolylineData polylineData: mPolylineData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylineData.clear();
                    mPolylineData = new ArrayList<>();
                }

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                    polyline.setClickable(true);
                    mPolylineData.add(new PolylineData(polyline, route.legs[0]));
                    zoomRoute(polyline.getPoints());
                }
            }
        });
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mGoogleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mGoogleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onPolylineClick(Polyline polyline) {
        int index = 0;
        for(PolylineData polylineData: mPolylineData){
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.blue1));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );
                Log.d(TAG, "Metric: " + isMetric + " Imperial: " + isImperial);
                if (isMetric){
                    String string1 = "Trip: #" + index + "\n" + "Duration: " +
                            polylineData.getLeg().duration  + "   Distance: " +
                            polylineData.getLeg().distance;
                    mDirectionInfo.setText(string1.toString());
                }
                else if (isImperial) {
                    long tempDistance = polylineData.getLeg().distance.inMeters;
                    float numReturn = (tempDistance / 1609f);
                    String string2 = "Trip: #" + index + "\n" + "Duration: " +
                            polylineData.getLeg().duration  + "   Distance: " +
                            numReturn + "Miles";
                    mDirectionInfo.setText(string2.toString());
                }
                Log.d(TAG, "mDirectionInfo: " + mDirectionInfo.getText().toString());
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }



    private void SaveUserLocation() {
        if (mUserLocation != null) {
            DocumentReference locationRef = mDatabase.collection("UserLocations").
                    document(mUserLocation.getUserID());
            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                                "\n latitude: " + mUserLocation.getGeoPoint().getLatitude() +
                                "\n longitude: " + mUserLocation.getGeoPoint().getLongitude());
                    }
                }
            });
        }
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this.getActivity(), LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                SearchFragment.this.getActivity().startForegroundService(serviceIntent);
            } else {
                SearchFragment.this.getActivity().startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) SearchFragment.this.getActivity().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.vega.gade3.student17611612.ashmapapp.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }


   /* private void hideSoftKeyBoard(){
        this.getActivity().getWindow().setSoftInputMode
                (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }*/

    private void expandMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                70,
                100);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mDirectionsView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                30,
                0);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    private void contractMapAnimation(){
        mDirectionInfo.setText(" ");

        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                70);
        mapAnimation.setDuration(800);


        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mDirectionsView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                0,
                30);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
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
    public void onPoiClick(PointOfInterest pointOfInterest) {
        if (mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED){
            mGoogleMap.clear();
            AddMapMarker(pointOfInterest.latLng, pointOfInterest.name, 0);
        }
    }
}
