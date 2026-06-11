package com.bupt.myapplication.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatEditText;

import com.bupt.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class EditableSequenceView extends LinearLayout {

    public interface OnCompleteListener {
        void onComplete(String formattedValue);
    }

    public interface OnValueChangedListener {
        void onValueChanged(String formattedValue, String rawInput, boolean complete);
    }

//    private static final String DEFAULT_PATTERN = "HEAD_Sxx_00xxxx_20xxxx";
//    private static final String DEFAULT_PATTERN = "_____ATHENAxxxxxx_____";
    // 301
    private static final String DEFAULT_PATTERN = "__PEA_xx_xxxx_20xxxx__";
    private static final char DEFAULT_SLOT_CHAR = 'x';

    private LinearLayout tokenContainer;
    private MaterialButton clearButton;

    private final List<AppCompatEditText> inputCells = new ArrayList<>();

    private String pattern = DEFAULT_PATTERN;
    private char slotChar = DEFAULT_SLOT_CHAR;
    private boolean showClearButton = true;

    private boolean internalUpdate = false;

    @Nullable
    private OnCompleteListener onCompleteListener;
    @Nullable
    private OnValueChangedListener onValueChangedListener;

    public EditableSequenceView(Context context) {
        super(context);
        init(context, null);
    }

    public EditableSequenceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EditableSequenceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.editable_sequence_view, this, true);
        tokenContainer = findViewById(R.id.token_container);
        clearButton = findViewById(R.id.clear_button);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditableSequenceView);
            String patternFromXml = typedArray.getString(R.styleable.EditableSequenceView_esv_pattern);
            String slotCharFromXml = typedArray.getString(R.styleable.EditableSequenceView_esv_slot_char);
            showClearButton = typedArray.getBoolean(R.styleable.EditableSequenceView_esv_show_clear_button, true);
            typedArray.recycle();

            if (patternFromXml != null && !patternFromXml.isEmpty()) {
                pattern = patternFromXml;
            }
            if (slotCharFromXml != null && !slotCharFromXml.isEmpty()) {
                slotChar = slotCharFromXml.charAt(0);
            }
        }

        clearButton.setVisibility(showClearButton ? VISIBLE : GONE);
        clearButton.setOnClickListener(v -> clear());

        rebuildTokens();
    }

    public void setPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return;
        }
        this.pattern = pattern;
        rebuildTokens();
    }

    public String getPattern() {
        return pattern;
    }

    public void setOnCompleteListener(@Nullable OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void setOnValueChangedListener(@Nullable OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    public String getRawInput() {
        StringBuilder builder = new StringBuilder();
        for (AppCompatEditText editText : inputCells) {
            Editable editable = editText.getText();
            if (editable != null) {
                builder.append(editable.toString());
            }
        }
        return builder.toString();
    }

    public String getFormattedValue() {
        StringBuilder builder = new StringBuilder();
        int slotIndex = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == slotChar) {
                if (slotIndex < inputCells.size()) {
                    Editable editable = inputCells.get(slotIndex).getText();
                    if (editable != null) {
                        builder.append(editable.toString());
                    }
                }
                slotIndex++;
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    // Backward-compatible API
    public String getText() {
        return getFormattedValue();
    }

    public boolean isComplete() {
        if (inputCells.isEmpty()) {
            return true;
        }
        for (AppCompatEditText editText : inputCells) {
            Editable editable = editText.getText();
            if (editable == null || editable.length() != 1) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        internalUpdate = true;
        for (AppCompatEditText editText : inputCells) {
            editText.setText("");
        }
        internalUpdate = false;
        focusFirstSlot();
        notifyValueChanged();
    }

    public void setValue(String value) {
        if (value == null) {
            clear();
            return;
        }

        internalUpdate = true;
        for (AppCompatEditText editText : inputCells) {
            editText.setText("");
        }

        List<Character> digitsToFill = new ArrayList<>();
        if (value.length() == pattern.length()) {
            for (int i = 0; i < pattern.length() && i < value.length(); i++) {
                if (pattern.charAt(i) == slotChar) {
                    char candidate = value.charAt(i);
                    if (Character.isDigit(candidate)) {
                        digitsToFill.add(candidate);
                    }
                }
            }
        } else {
            for (int i = 0; i < value.length(); i++) {
                char candidate = value.charAt(i);
                if (Character.isDigit(candidate)) {
                    digitsToFill.add(candidate);
                }
            }
        }

        int limit = Math.min(digitsToFill.size(), inputCells.size());
        for (int i = 0; i < limit; i++) {
            inputCells.get(i).setText(String.valueOf(digitsToFill.get(i)));
        }
        internalUpdate = false;

        if (isComplete()) {
            inputCells.get(inputCells.size() - 1).requestFocus();
        } else {
            focusFirstEmptySlot();
        }
        notifyValueChanged();
    }

    private void rebuildTokens() {
        tokenContainer.removeAllViews();
        inputCells.clear();

        Context context = getContext();
        int tokenGap = getResources().getDimensionPixelSize(R.dimen.esv_token_gap);

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == slotChar) {
                AppCompatEditText input = createInputCell(context, inputCells.size());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.esv_cell_min_width),
                        getResources().getDimensionPixelSize(R.dimen.esv_cell_min_height));
                params.setMarginEnd(tokenGap);
                tokenContainer.addView(input, params);
                inputCells.add(input);
            } else {
                MaterialTextView fixedText = new MaterialTextView(context);
                fixedText.setText(String.valueOf(c));
                fixedText.setTextAppearance(R.style.Widget_MyApplication_EditableSequence_FixedText);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.setMarginEnd(tokenGap);
                tokenContainer.addView(fixedText, params);
            }
        }
        focusFirstSlot();
        notifyValueChanged();
    }

    private AppCompatEditText createInputCell(Context context, int index) {
        AppCompatEditText editText = new AppCompatEditText(context);
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        editText.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        editText.setFilters(new InputFilter[0]);
        editText.setSingleLine(true);
        editText.setSelectAllOnFocus(false);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setCursorVisible(true);
        editText.setClickable(true);
        editText.setLongClickable(true);
        editText.setTextIsSelectable(false);
        editText.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        editText.setTextColor(getResources().getColor(R.color.ui_text_primary));
        editText.setHintTextColor(getResources().getColor(R.color.ui_text_secondary));
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.esv_cell_text_size));
        editText.setGravity(android.view.Gravity.CENTER);
        int paddingH = getResources().getDimensionPixelSize(R.dimen.esv_cell_padding_h);
        int paddingV = getResources().getDimensionPixelSize(R.dimen.esv_cell_padding_v);
        editText.setPadding(paddingH, paddingV, paddingH, paddingV);
        editText.setMinWidth(getResources().getDimensionPixelSize(R.dimen.esv_cell_min_width));
        editText.setMinHeight(getResources().getDimensionPixelSize(R.dimen.esv_cell_min_height));
        editText.setBackgroundResource(R.drawable.esv_input_background_normal);
        applyCursorDrawable(editText);
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            editText.setCursorVisible(hasFocus);
            editText.setTypeface(hasFocus ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            editText.setBackgroundResource(hasFocus
                    ? R.drawable.esv_input_background_focused
                    : R.drawable.esv_input_background_normal);
            if (hasFocus) {
                Editable text = editText.getText();
                int length = text == null ? 0 : text.length();
                editText.setSelection(length);
            }
        });

        editText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                Editable editable = editText.getText();
                if (editable == null || editable.length() == 0) {
                    moveFocusToPrevious(index, true);
                    return true;
                }
            }
            return false;
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (internalUpdate) {
                    return;
                }
                handleCellChanged(index, s == null ? "" : s.toString());
            }
        });
        return editText;
    }

    private void applyCursorDrawable(AppCompatEditText editText) {
        Drawable cursor = AppCompatResources.getDrawable(getContext(), R.drawable.esv_cursor);
        if (cursor == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            editText.setTextCursorDrawable(cursor);
            return;
        }
        try {
            java.lang.reflect.Field resField = android.widget.TextView.class.getDeclaredField("mCursorDrawableRes");
            resField.setAccessible(true);
            resField.set(editText, R.drawable.esv_cursor);
        } catch (Exception ignored) {
        }
    }

    private void handleCellChanged(int index, String current) {
        String digits = keepDigits(current);
        internalUpdate = true;

        if (digits.isEmpty()) {
            inputCells.get(index).setText("");
            inputCells.get(index).setSelection(0);
        } else if (digits.length() == 1) {
            inputCells.get(index).setText(digits);
            inputCells.get(index).setSelection(1);
            moveFocusToNext(index);
        } else {
            applyPastedDigits(index, digits);
        }

        internalUpdate = false;
        notifyValueChanged();
    }

    private void applyPastedDigits(int fromIndex, String digits) {
        int cursor = fromIndex;
        for (int i = 0; i < digits.length() && cursor < inputCells.size(); i++) {
            AppCompatEditText target = inputCells.get(cursor);
            target.setText(String.valueOf(digits.charAt(i)));
            target.setSelection(1);
            cursor++;
        }
        if (cursor < inputCells.size()) {
            inputCells.get(cursor).requestFocus();
        } else if (!inputCells.isEmpty()) {
            inputCells.get(inputCells.size() - 1).requestFocus();
        }
    }

    private void moveFocusToNext(int index) {
        if (index >= 0 && index < inputCells.size() - 1) {
            inputCells.get(index + 1).requestFocus();
        }
    }

    private void moveFocusToPrevious(int index, boolean clearPrevious) {
        if (index > 0 && index < inputCells.size()) {
            AppCompatEditText previous = inputCells.get(index - 1);
            previous.requestFocus();
            if (clearPrevious) {
                previous.setText("");
            }
        }
    }

    private void focusFirstSlot() {
        if (!inputCells.isEmpty()) {
            AppCompatEditText first = inputCells.get(0);
            first.requestFocus();
            first.setCursorVisible(true);
            Editable text = first.getText();
            first.setSelection(text == null ? 0 : text.length());
        }
    }

    private void focusFirstEmptySlot() {
        for (AppCompatEditText cell : inputCells) {
            Editable editable = cell.getText();
            if (editable == null || editable.length() == 0) {
                cell.requestFocus();
                cell.setCursorVisible(true);
                cell.setSelection(0);
                return;
            }
        }
        if (!inputCells.isEmpty()) {
            AppCompatEditText last = inputCells.get(inputCells.size() - 1);
            last.requestFocus();
            last.setCursorVisible(true);
            Editable text = last.getText();
            last.setSelection(text == null ? 0 : text.length());
        }
    }

    private void notifyValueChanged() {
        String formattedValue = getFormattedValue();
        String rawInput = getRawInput();
        boolean complete = isComplete();
        if (onValueChangedListener != null) {
            onValueChangedListener.onValueChanged(formattedValue, rawInput, complete);
        }
        if (complete && onCompleteListener != null) {
            onCompleteListener.onComplete(formattedValue);
        }
    }

    private String keepDigits(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
