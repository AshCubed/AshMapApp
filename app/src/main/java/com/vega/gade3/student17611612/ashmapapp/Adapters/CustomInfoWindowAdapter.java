package com.vega.gade3.student17611612.ashmapapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.vega.gade3.student17611612.ashmapapp.R;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context context;

    public CustomInfoWindowAdapter(Context context) {
        this.context = context;
        this.mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void RenderWindowText(Marker marker, View views){
        String title = marker.getTitle();
        TextView tvTitle = (TextView) views.findViewById(R.id.title);

        if (!title.equals("")){
            tvTitle.setText(title);
        }

        String snippet = marker.getSnippet();
        TextView tvSnippet = (TextView) views.findViewById(R.id.snippet);

        if (!title.equals("")){
            tvSnippet.setText(snippet);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        RenderWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        RenderWindowText(marker, mWindow);
        return mWindow;
    }
}
