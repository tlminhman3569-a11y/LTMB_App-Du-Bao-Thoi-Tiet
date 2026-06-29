package com.example.weatherapp.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnHistoryClickListener {
        void onHistoryClick(String cityName);
        void onDeleteClick(int position, String cityName);
    }

    private List<String> historyList;
    private final Context context;
    private OnHistoryClickListener listener;

    public HistoryAdapter(Context context, List<String> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<String> newHistory) {
        this.historyList = newHistory;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_search_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String cityName = historyList.get(position);
        holder.tvHistoryCity.setText(cityName);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHistoryClick(cityName);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(position, cityName);
        });
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryCity;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryCity = itemView.findViewById(R.id.tv_history_city);
            btnDelete = itemView.findViewById(R.id.btn_delete_history);
        }
    }
}