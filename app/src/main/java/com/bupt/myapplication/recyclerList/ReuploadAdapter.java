package com.bupt.myapplication.recyclerList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bupt.myapplication.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReuploadAdapter extends RecyclerView.Adapter<ReuploadAdapter.ViewHolder> {

    private List<File> jsonFiles = new ArrayList<>();

    public ReuploadAdapter(List<File> jsonFiles) {
        Log.e("MyAdapter", "construct");
        this.jsonFiles = jsonFiles;
    }

    public void clearData(){
        this.jsonFiles.clear();
    }

    @NonNull
    @Override
    public ReuploadAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ble_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = jsonFiles.get(position);
        Log.e("MyAdapter", file.getName());
        holder.textView.setText(file.getName());
    }

    @Override
    public int getItemCount() {
        return jsonFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ViewHolder(View itemView){
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO:点击一条信息之后的逻辑

                }
            });
        }
    }
}