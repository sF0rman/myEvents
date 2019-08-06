package no.sforman.myevents;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    public static final String TAG = "MapFragment";


    private String mapKey;
    private MapView mapView;
    private GoogleMap gMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Lifecycle");
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        initMap(savedInstanceState);

        return view;
    }

    public void initMap(Bundle savedInstanceState){
        Log.d(TAG, "initMap: ");
        mapKey = getString(R.string.events_maps_key);
        Log.d(TAG, "initMap: got key: " + mapKey);
        Bundle mapViewBundle = null;
        if(savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(mapKey);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");

        Bundle mapViewBundle = outState.getBundle(mapKey);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(mapKey, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: Lifecycle");
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: Lifecycle");
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: Lifecycle");
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "onMapReady: Lifecycle");
        gMap = map;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: Lifecycle");
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Lifecycle");
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory: Lifecycle");
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void setLocationMarker(LatLng loc){
        Log.d(TAG, "setLocationMarker: ");
        gMap.clear();
        gMap.addMarker(new MarkerOptions().position(loc).title("Marker"));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 12.0f));
    }

    public void setLocationMarkerNoAnim(LatLng loc){
        Log.d(TAG, "setLocationMarkerNoAnim: ");
        gMap.clear();
        gMap.addMarker(new MarkerOptions().position(loc).title("Marker"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12.f));
    }
}
