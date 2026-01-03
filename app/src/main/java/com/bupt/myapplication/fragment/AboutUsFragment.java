package com.bupt.myapplication.fragment;

import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

        // 设置关闭按钮
        ImageButton btnClose = view.findViewById(R.id.btn_close_about_us);
        btnClose.setOnClickListener(v -> {
            if (mCallBack != null) {
                mCallBack.send2main("close_about_us");
            }
        });

        // 设置应用版本号
        TextView tvVersion = view.findViewById(R.id.tv_app_version);
        try {
            PackageInfo packageInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            String versionName = packageInfo.versionName;
            tvVersion.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("1.0");
        }

        // 设置二维码点击放大功能
        ImageView qrCodeImageView = view.findViewById(R.id.iv_qr_code_popup);
        qrCodeImageView.setOnClickListener(v -> showQrCodeDialog());
    }

    /**
     * 显示二维码放大对话框
     */
    private void showQrCodeDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_qr_code);
        
        ImageView qrCodeLarge = dialog.findViewById(R.id.iv_qr_code_large);
        qrCodeLarge.setImageResource(R.drawable.qr_code);
        
        ImageButton btnClose = dialog.findViewById(R.id.btn_close_qr_dialog);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        // 点击对话框外部区域关闭
        dialog.findViewById(R.id.qr_dialog_container).setOnClickListener(v -> dialog.dismiss());
        
        // 点击二维码本身不关闭
        qrCodeLarge.setOnClickListener(null);
        
        dialog.show();
        
        // 设置对话框窗口属性
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
