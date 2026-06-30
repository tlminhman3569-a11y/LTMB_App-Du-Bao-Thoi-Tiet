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

    public interface OnItemClickListener {
        void onItemClick(FavoriteCity city);
    }

    private final List<FavoriteCity> favoriteCities = new ArrayList<>();
    private OnFavoriteActionListener actionListener;
    private OnItemClickListener itemClickListener;

    public void setOnFavoriteActionListener(OnFavoriteActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
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

        // 1. Tên thành phố + Country
        String displayName = city.getCityName();
        if (city.getCountry() != null && !city.getCountry().isEmpty()) {
            displayName += ", " + city.getCountry();
        }
        holder.tvFavoriteCityName.setText(displayName);

        // 2. Mô tả thời tiết
        String desc = city.getDescription();
        if (desc == null || desc.isEmpty()) {
            desc = "Đang tải...";
        } else {
            desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
        }
        holder.tvFavoriteWeatherDesc.setText(desc);

        // 3. Nhiệt độ (Sử dụng WeatherUtils hiển thị độ C/F cài đặt động)
        double temp = city.getTemperature();
        if (Double.isNaN(temp)) {
            holder.tvFavoriteTemp.setText("--");
        } else {
            int tempInt = (int) Math.round(temp);
            if (com.example.weatherapp.utils.WeatherUtils.isCelsius(holder.itemView.getContext())) {
                holder.tvFavoriteTemp.setText(tempInt + "°C");
            } else {
                int tempInFahrenheit = com.example.weatherapp.utils.WeatherUtils.convertCelsiusToFahrenheit(tempInt);
                holder.tvFavoriteTemp.setText(tempInFahrenheit + "°F");
            }
        }

        // 4. Icon thời tiết bằng Glide
        String iconCode = city.getIconCode();
        if (iconCode == null || iconCode.isEmpty()) {
            holder.imgLocationIcon.setImageResource(android.R.drawable.ic_menu_mylocation);
            holder.imgLocationIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(
                    holder.itemView.getContext(), android.R.color.white));
        } else {
            holder.imgLocationIcon.clearColorFilter();
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(iconUrl)
                    .placeholder(android.R.drawable.ic_menu_mylocation)
                    .into(holder.imgLocationIcon);
        }

        holder.imgDeleteHint.setOnClickListener(v -> {
            if (actionListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    actionListener.onDeleteClick(adapterPosition);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(favoriteCities.get(adapterPosition));
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
        TextView tvFavoriteWeatherDesc;
        TextView tvFavoriteTemp;
        ImageView imgLocationIcon;
        ImageView imgDeleteHint;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFavoriteCityName = itemView.findViewById(R.id.tvFavoriteCityName);
            tvFavoriteWeatherDesc = itemView.findViewById(R.id.tvFavoriteWeatherDesc);
            tvFavoriteTemp = itemView.findViewById(R.id.tvFavoriteTemp);
            imgLocationIcon = itemView.findViewById(R.id.imgLocationIcon);
            imgDeleteHint = itemView.findViewById(R.id.imgDeleteHint);
        }
    }
}
