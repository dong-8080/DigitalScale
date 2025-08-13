package com.bupt.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView; // Keep for qr_code if needed, but the new XML has iv_qr_code_popup

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bupt.myapplication.R;

public class AboutUsFragment extends Fragment {

    private IFragmentCallBack mCallBack; // Callback to communicate with MainActivity

    public AboutUsFragment() {
        // Required empty public constructor
    }

    public static AboutUsFragment newInstance() {
        return new AboutUsFragment();
    }

    // Setter for the callback
    public void setiFragmentCallBack(IFragmentCallBack callBack) {
        this.mCallBack = callBack;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_us, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnClose = view.findViewById(R.id.btn_close_about_us);
        btnClose.setOnClickListener(v -> {
            if (mCallBack != null) {
                mCallBack.send2main("close_about_us"); // Send a specific message
            }
        });

        // ImageView qrCodeImageView = view.findViewById(R.id.iv_qr_code_popup);
        // qrCodeImageView.setImageResource(R.drawable.qr_code); // Already set in XML by android:src
    }
}
