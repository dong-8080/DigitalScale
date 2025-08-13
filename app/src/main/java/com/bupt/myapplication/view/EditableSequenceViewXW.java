package com.bupt.myapplication.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.bupt.myapplication.R;

public class EditableSequenceViewXW extends LinearLayout {

    // 所有的 EditText 引用
    private EditText[] editTexts;
    private Button clearButton;

    public EditableSequenceViewXW(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // 加载自定义的 editable_sequence_view.xml 布局
        inflate(context, R.layout.editable_sequence_view_xw, this);

        // 初始化 EditText 数组，顺序为布局中的顺序
        editTexts = new EditText[]{
                findViewById(R.id.editTextX1),
                findViewById(R.id.editTextX2),
                findViewById(R.id.editTextX3),
                findViewById(R.id.editTextX4),
                findViewById(R.id.editTextYear1),
                findViewById(R.id.editTextYear2),
                findViewById(R.id.editTextMonth1),
                findViewById(R.id.editTextMonth2)
        };

        // 初始化 "清除按钮"
        clearButton = findViewById(R.id.clear_button);
        if (clearButton != null) {
            clearButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    clearAllEditTexts();
                }
            });
        }

        // 设置输入框的监听器，自动跳转至下一个输入框
        for (int i = 0; i < editTexts.length; i++) {
            final int currentIndex = i; // 当前索引用于标识 EditText
            editTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    // 自动跳转到下一个输入框
                    if (s.length() == 1) {
                        moveToNextEditText(currentIndex);
                    }
                }
            });
        }
    }

    /**
     * 清空所有 `EditText` 的文本内容
     */
    private void clearAllEditTexts() {
        for (EditText editText : editTexts) {
            if (editText != null) {
                editText.setText("");
            }
        }
        // 重置焦点到第一个输入框
        if (editTexts.length > 0) {
            editTexts[0].requestFocus();
        }
    }

    private void moveToNextEditText(int currentIndex) {
        if (currentIndex >= 0 && currentIndex < editTexts.length - 1) {
            EditText nextEditText = editTexts[currentIndex + 1];
            nextEditText.requestFocus(); // 移动焦点到下一个输入框
        }
    }

    /**
     * 获取拼接后的字符串
     *
     * @return 字符串表示
     */
    public String getText() {
        return "BUPTHZ__"
                + "00" + editTexts[0].getText() + editTexts[1].getText()
                + editTexts[2].getText() + editTexts[3].getText()
                + "__20" + editTexts[4].getText() + editTexts[5].getText()
                + editTexts[6].getText() + editTexts[7].getText();
    }
}