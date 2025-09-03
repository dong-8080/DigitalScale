package com.bupt.myapplication.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bupt.myapplication.R;
import com.bupt.myapplication.fragment.IFragmentCallBack;

public class MyAccumulator extends Fragment {

    private TextView mTextView1;
    private TextView mTextView2;
    private Button mAddButton1;
    private Button mAddButton2;
    private Button closeButton;
    private Button mClearButton;
    private View rootView;
    private int mCount1 = 0;
    private int mCount2 = 0;
    private IFragmentCallBack iFragmentCallBack;

    public MyAccumulator() {
        // Required empty public constructor
    }
    public void setiFragmentCallBack(IFragmentCallBack callback){
        iFragmentCallBack = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_my_accumulator, container, false);
        }

        mTextView1 = rootView.findViewById(R.id.text_view1);
        mTextView1.setText(String.valueOf(mCount1));
        mTextView2 = rootView.findViewById(R.id.text_view2);
        mTextView2.setText(String.valueOf(mCount2));
        closeButton=rootView.findViewById(R.id.close_button);
        mAddButton1 = rootView.findViewById(R.id.button_add1);
        mClearButton=rootView.findViewById(R.id.button_clear);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCount1=0;
                mCount2=0;
                mTextView1.setText(String.valueOf(mCount1));
                mTextView2.setText(String.valueOf(mCount2));
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iFragmentCallBack.send2main("close");
            }
        });
        mAddButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCount1++;
                mTextView1.setText(String.valueOf(mCount1));
            }
        });

        mAddButton2 = rootView.findViewById(R.id.button_add2);
        mAddButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCount2++;
                mTextView2.setText(String.valueOf(mCount2));
            }
        });

        return rootView;
    }
}