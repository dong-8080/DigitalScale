package com.bupt.myapplication.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bupt.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class FeedbackFragment extends Fragment {

    private IFragmentCallBack mCallBack;
    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etSubject;
    private TextInputEditText etContent;
    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilSubject;
    private TextInputLayout tilContent;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

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
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图
        etName = view.findViewById(R.id.et_feedback_name);
        etEmail = view.findViewById(R.id.et_feedback_email);
        etSubject = view.findViewById(R.id.et_feedback_subject);
        etContent = view.findViewById(R.id.et_feedback_content);
        tilName = view.findViewById(R.id.til_feedback_name);
        tilEmail = view.findViewById(R.id.til_feedback_email);
        tilSubject = view.findViewById(R.id.til_feedback_subject);
        tilContent = view.findViewById(R.id.til_feedback_content);

        // 设置返回按钮
        com.google.android.material.appbar.MaterialToolbar toolbar = view.findViewById(R.id.toolbar_feedback);
        toolbar.setNavigationOnClickListener(v -> {
            if (mCallBack != null) {
                mCallBack.send2main("close_feedback");
            }
        });

        // 设置提交按钮
        view.findViewById(R.id.btn_submit_feedback).setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        // 清除之前的错误提示
        clearErrors();

        // 获取输入内容
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String subject = etSubject.getText() != null ? etSubject.getText().toString().trim() : "";
        String content = etContent.getText() != null ? etContent.getText().toString().trim() : "";

        // 验证输入
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            tilName.setError("请输入您的姓名");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("请输入您的邮箱");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("请输入有效的邮箱地址");
            isValid = false;
        }

        if (TextUtils.isEmpty(subject)) {
            tilSubject.setError("请输入反馈主题");
            isValid = false;
        }

        if (TextUtils.isEmpty(content)) {
            tilContent.setError("请输入反馈内容");
            isValid = false;
        } else if (content.length() < 10) {
            tilContent.setError("反馈内容至少需要10个字符");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // 发送邮件
        sendFeedbackEmail(name, email, subject, content);
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilSubject.setError(null);
        tilContent.setError(null);
    }

    private void sendFeedbackEmail(String name, String email, String subject, String content) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dong1024mail@163.com"}); // 接收反馈的邮箱
            intent.putExtra(Intent.EXTRA_SUBJECT, "【意见反馈】" + subject);

            // 构建邮件正文
            String emailBody = "反馈人姓名：" + name + "\n\n";
            emailBody += "反馈人邮箱：" + email + "\n\n";
            emailBody += "反馈主题：" + subject + "\n\n";
            emailBody += "反馈内容：\n" + content;

            intent.putExtra(Intent.EXTRA_TEXT, emailBody);

            // 检查是否有邮件应用
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(Intent.createChooser(intent, "选择邮件应用"));
                Toast.makeText(requireContext(), "正在打开邮件应用...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "未找到邮件应用，请先安装邮件客户端", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "发送失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

