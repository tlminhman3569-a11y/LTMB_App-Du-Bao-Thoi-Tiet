package com.example.weatherapp.ui.favorite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.models.favorite.FavoriteCity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {
    public interface OnFavoriteActionListener {
        void onDeleteClick(int position);
    }

    private final List<FavoriteCity> favoriteCities = new ArrayList<>();
    private OnFavoriteActionListener actionListener;

    public void setOnFavoriteActionListener(OnFavoriteActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setData(List<FavoriteCity> cities) {
        favoriteCities.clear();

        if (cities != null) {
            favoriteCities.addAll(cities);
        }

        notifyDataSetChanged();
    }

    public FavoriteCity getItem(int position) {
        if (position < 0 || position >= favoriteCities.size()) {
            return null;
        }

        return favoriteCities.get(position);
    }

    public void removeItem(int position) {
        if (position < 0 || position >= favoriteCities.size()) {
            return;
        }

        favoriteCities.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(FavoriteCity city, int position) {
        if (city == null) {
            return;
        }

        favoriteCities.add(position, city);
        notifyItemInserted(position);
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_city, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteCity city = favoriteCities.get(position);

        holder.tvFavoriteCityName.setText(city.getCityName());

        String coordinates = String.format(
                Locale.getDefault(),
                "Lat: %.4f, Lon: %.4f",
                city.getLatitude(),
                city.getLongitude()
        );
        holder.tvFavoriteCityCoords.setText(coordinates);

        holder.imgDeleteHint.setOnClickListener(v -> {
            if (actionListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    actionListener.onDeleteClick(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteCities.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView tvFavoriteCityName;
        TextView tvFavoriteCityCoords;
        ImageView imgDeleteHint;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFavoriteCityName = itemView.findViewById(R.id.tvFavoriteCityName);
            tvFavoriteCityCoords = itemView.findViewById(R.id.tvFavoriteCityCoords);
            imgDeleteHint = itemView.findViewById(R.id.imgDeleteHint);
        }
    }
}
