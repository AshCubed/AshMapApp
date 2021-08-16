package com.vega.gade3.student17611612.ashmapapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vega.gade3.student17611612.ashmapapp.R;

public class FavouriteLocationsAdapter extends RecyclerView.Adapter<FavouriteLocationsAdapter.MyViewHolder> {

    private OnItemClickListener mListener;

    private Context context;
    private String[] LocationItemName;

    public interface OnItemClickListener{
        void onDirectToClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocationText;
        Button btnDirectTo;
        Button btnDelete;

        public MyViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            tvLocationText = itemView.findViewById(R.id.tvLocationName);
            btnDirectTo = itemView.findViewById(R.id.btnDirectTO);
            btnDelete = itemView.findViewById(R.id.btnDeleteLocal);

            btnDirectTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onDirectToClick(position);
                        }
                    }
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onDeleteClick(position);
                            Toast.makeText(v.getContext(), "Deleted!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    public FavouriteLocationsAdapter(Context context, String[] locationItemName) {
        this.context = context;
        this.LocationItemName = locationItemName;
    }


    @NonNull
    @Override
    public FavouriteLocationsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.favouritelocations_my_row, parent, false);
        return new FavouriteLocationsAdapter.MyViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvLocationText.setText(LocationItemName[position]);
    }

    @Override
    public int getItemCount() {
        return LocationItemName.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
