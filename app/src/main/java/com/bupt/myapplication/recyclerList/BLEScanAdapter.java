package com.bupt.myapplication.recyclerList;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.bbb.bpen.command.BiBiCommand;
import com.bupt.myapplication.GlobalVars;
import com.bupt.myapplication.R;

import java.util.ArrayList;
import java.util.List;

// 没用
public class BLEScanAdapter extends RecyclerView.Adapter<BLEScanAdapter.ViewHolder>  {

    private List<String> macAddressList = new ArrayList<>();

    private String BindId = "";

    public void setBindId(String s){
        BindId = s;
    }

    public BLEScanAdapter(List<String> macAddressList){
        Log.e("BLEScanAdapter", "construct");
        this.macAddressList = macAddressList;
    }

    public void updateData(List<String> macAddressList) {
        this.macAddressList.clear();
        this.macAddressList.addAll(macAddressList);
        notifyDataSetChanged();
    }

    public void addData(String s){
        this.macAddressList.add(s);
    }

    public void clearData(){
        this.macAddressList.clear();
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
        //TODO:动态变色
        //holder.itemView.setBackgroundResource(R.drawable.bubble_background);
        if (macAddress.equals(GlobalVars.getInstance().getGlobalAddr())) {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float fraction = animation.getAnimatedFraction();
                    int color = ColorUtils.blendARGB(Color.WHITE, Color.rgb(120,222,130), fraction);
                    holder.itemView.setBackgroundColor(color);
                }
            });
            animator.start();
        }
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
                    Toast.makeText(view.getContext(), "尝试连接蓝牙笔，请稍等", Toast.LENGTH_SHORT).show();
                    BiBiCommand.connect(view.getContext(), textView.getText().toString());
                    // textView.setBackgroundColor(Color.GREEN);
                }
            });
        }


    }
}
