package com.example.weatherapp.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.models.common.SearchResultItem;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SearchResultItem item);
        void onFavoriteClick(SearchResultItem item);
    }

    private List<SearchResultItem> results;
    private final Context context;
    private OnItemClickListener listener;

    public SearchResultAdapter(Context context, List<SearchResultItem> results) {
        this.context = context;
        this.results = results;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<SearchResultItem> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultItem item = results.get(position);
        holder.tvCityName.setText(item.getCityName() + ", " + item.getCountry());
        holder.tvTemperature.setText(String.format("%.0f°C", item.getTemperature()));
        holder.tvWeatherDesc.setText(item.getWeatherDesc());

        String iconUrl = "https://openweathermap.org/img/wn/" + item.getIconCode() + "@2x.png";
        Glide.with(context).load(iconUrl).into(holder.ivWeatherIcon);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName, tvTemperature, tvWeatherDesc;
        ImageView ivWeatherIcon;
        ImageButton btnFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tv_city_name);
            tvTemperature = itemView.findViewById(R.id.tv_temperature);
            tvWeatherDesc = itemView.findViewById(R.id.tv_weather_desc);
            ivWeatherIcon = itemView.findViewById(R.id.iv_weather_icon);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}