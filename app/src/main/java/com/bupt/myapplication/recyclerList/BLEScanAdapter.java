package com.bupt.myapplication.recyclerList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bbb.bpen.command.BiBiCommand;
import com.bupt.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class BLEScanAdapter extends RecyclerView.Adapter<BLEScanAdapter.ViewHolder>  {

    private List<String> macAddressList = new ArrayList<>();

    public BLEScanAdapter(List<String> macAddressList){
        this.macAddressList = macAddressList;
    }

    public void updateData(List<String> macAddressList) {
        this.macAddressList.clear();
        this.macAddressList.addAll(macAddressList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BLEScanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ble_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String macAddress = macAddressList.get(position);
        Log.e("Adapter", macAddress);
        holder.textView.setText(macAddress);
    }

    @Override
    public int getItemCount() {
        return macAddressList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView){
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 可以优化下，当连接完成后这一项会变色
                    BiBiCommand.connect(view.getContext(), textView.getText().toString());
                }
            });
        }


    }
}
