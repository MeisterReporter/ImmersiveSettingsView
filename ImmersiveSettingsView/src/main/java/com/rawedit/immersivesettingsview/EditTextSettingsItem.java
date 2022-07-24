package com.rawedit.immersivesettingsview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.airbnb.paris.Paris;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditTextSettingsItem extends SettingsItem {

    private boolean strokeColorChanged = false;
    private boolean showMessage = true;
    private boolean showButton = true;

    @ColorInt
    private int rippleColor = 0;
    @ColorInt
    private int strokeColor = 0;

    private int editTextStyle = com.google.android.material.R.style.Widget_MaterialComponents_TextInputEditText_FilledBox;
    private int buttonStyle = com.google.android.material.R.style.Widget_Material3_Button;

    private final View v;
    private TextView textView;
    private TextInputLayout inputLayout;
    private TextInputEditText editText;
    private MaterialButton button;

    private OnClickListener onClickListener;
    private TextWatcher onTextChangeListener;

    public EditTextSettingsItem(Context context) {
        this(context, null);
    }

    public EditTextSettingsItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EditTextSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        type = Type.EDITTEXT;
        v = inflate(context, R.layout.setting_edittext, this);
        textView = v.findViewById(R.id.message);
        inputLayout = v.findViewById(R.id.editTextLayout);
        editText = v.findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(!showButton) saveCurrentState();
                if(onTextChangeListener != null) onTextChangeListener.beforeTextChanged(s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!showButton) saveCurrentState();
                if(onTextChangeListener != null) onTextChangeListener.onTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!showButton) saveCurrentState();
                if(onTextChangeListener != null) onTextChangeListener.afterTextChanged(s);
            }
        });
        button = v.findViewById(R.id.confirm);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showButton) saveCurrentState();
                if(onClickListener != null) onClickListener.onClick(v);
            }
        });
        applyColor();
        executeQueue();
        initialized = true;
        loadSavedState();
        settingNameSave = "";
    }

    @Override
    public void setRippleColor(int color) {
        rippleColor = color;
        if(initialized) {
            if(rippleColor == strokeColor) {
                button.setRippleColor(ColorStateList.valueOf(getContext().getColor(R.color.white)));
            }else {
                button.setRippleColor(ColorStateList.valueOf(rippleColor));
            }
            if(!strokeColorChanged) {
                setStrokeColor(color);
            }
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    if(rippleColor == strokeColor) {
                        button.setRippleColor(ColorStateList.valueOf(getContext().getColor(R.color.white)));
                    }else {
                        button.setRippleColor(ColorStateList.valueOf(rippleColor));
                    }
                    if(!strokeColorChanged) {
                        setStrokeColor(color);
                    }
                }
            });
        }
    }

    @Override
    public View getView() {
        return v;
    }

    @Override
    public void loadSavedState() {
        super.loadSavedState();
        if(initialized) {
            try {
                String s = sharedPreferences.getString(settingNameSave, "");
                editText.setText(s);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveCurrentState() {
        super.saveCurrentState();
        if(initialized) {
            try {
                Editable e = editText.getText();
                if(e != null) sharedPreferences.edit().putString(settingNameSave, e.toString()).apply();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void applyColor() {
        if(initialized) {
            inputLayout.setBoxStrokeColor(strokeColor);
            inputLayout.setHintTextColor(ColorStateList.valueOf(strokeColor));
            SettingsView.setCursorColor(editText, strokeColor);
            if(rippleColor == strokeColor) {
                button.setRippleColor(ColorStateList.valueOf(getContext().getColor(R.color.white)));
            }else {
                button.setRippleColor(ColorStateList.valueOf(rippleColor));
            }
            button.setBackgroundTintList(ColorStateList.valueOf(strokeColor));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    inputLayout.setBoxStrokeColor(strokeColor);
                    inputLayout.setHintTextColor(ColorStateList.valueOf(strokeColor));
                    SettingsView.setCursorColor(editText, strokeColor);
                    if(rippleColor == strokeColor) {
                        button.setRippleColor(ColorStateList.valueOf(getContext().getColor(R.color.white)));
                    }else {
                        button.setRippleColor(ColorStateList.valueOf(rippleColor));
                    }
                    button.setBackgroundTintList(ColorStateList.valueOf(strokeColor));
                }
            });
        }
    }

    /**
     * Set the Text Input Type
     * @param inputType Use InputType.xxx
     */
    public void setInputType(int inputType) {
        if(initialized) {
            editText.setInputType(inputType);
        }
    }

    // Getter and Setter

    public int getStrokeColor() {
        return this.strokeColor;
    }

    /**
     * Sets the Color for the Button Background and the Stroke of the EditText Component
     * @param color the stroke color in @ColorInt format
     */
    public void setStrokeColor(int color) {
        if(initialized) {
            if(!strokeColorChanged && color != 0) strokeColorChanged = true;
            strokeColor = color;
            applyColor();
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    if(!strokeColorChanged && color != 0) strokeColorChanged = true;
                    strokeColor = color;
                    applyColor();
                }
            });
        }
    }

    /**
     * Sets the Text Color for all Components (Message, EditText, Button)
     * @param color a color in @ColorInt format
     */
    public void setTextColor(int color) {
        setTextColor(color, true, true, true);
    }

    /**
     * Sets the Text Color for the selected Components
     * @param color a color in @ColorInt format
     * @param toMessage should the Message Text Color be changed
     * @param toEditText should the EditText Text Color be changed
     * @param toButton should the Button Text Color be changed
     */
    public void setTextColor(int color, boolean toMessage, boolean toEditText, boolean toButton) {
        if(initialized) {
            if(toMessage) textView.setTextColor(color);
            if(toEditText) editText.setTextColor(color);
            if(toButton) button.setTextColor(color);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    if(toMessage) textView.setTextColor(color);
                    if(toEditText) editText.setTextColor(color);
                    if(toButton) button.setTextColor(color);
                }
            });
        }
    }

    /**
     * Set the message (works only of the Message is visible)
     * @param msg the Message
     */
    public void setMessage(String msg) {
        if(initialized) {
            textView.setText(msg);
            if(!settingsNameSaveChanged && showMessage) setSettingNameSave(String.valueOf(msg.hashCode()));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    textView.setText(msg);
                    if(!settingsNameSaveChanged && showMessage) setSettingNameSave(String.valueOf(msg.hashCode()));
                }
            });
        }
    }

    /**
     * Set the hint text for the EditText Component
     * @param hint the Hint Text
     */
    public void setEditTextHint(String hint) {
        if(initialized) {
            inputLayout.setHint(hint);
            if(!settingsNameSaveChanged) setSettingNameSave(String.valueOf(hint.hashCode()));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    inputLayout.setHint(hint);
                    if(!settingsNameSaveChanged) setSettingNameSave(String.valueOf(hint.hashCode()));
                }
            });
        }
    }

    /**
     * Force set the Content for the EditText Component
     * @param content the content
     */
    public void setEditTextContent(String content) {
        if(initialized) {
            editText.setText(content);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    editText.setText(content);
                }
            });
        }
    }

    /**
     * Set the Button text
     * @param txt Button text
     */
    public void setButtonText(String txt) {
        if(initialized) {
            button.setText(txt);
            if(!settingsNameSaveChanged && showButton) setSettingNameSave(String.valueOf(txt.hashCode()));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    button.setText(txt);
                    if(!settingsNameSaveChanged && showButton) setSettingNameSave(String.valueOf(txt.hashCode()));
                }
            });
        }
    }

    /**
     * Change the style of the EditText component at Runtime
     * @param style the style resource (Example: R.style.my_edittext_style)
     */
    public void setEditTextStyle(int style) {
        if(initialized) {
            editTextStyle = style;
            Paris.style(inputLayout).apply(style);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    editTextStyle = style;
                    Paris.style(inputLayout).apply(style);
                }
            });
        }
    }

    /**
     * Change the style of the Button component at Runtime
     * @param style the style resource (Example: R.style.my_button_style)
     */
    public void setButtonStyle(int style) {
        if(initialized) {
            buttonStyle = style;
            Paris.style(button).apply(style);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    buttonStyle = style;
                    Paris.style(button).apply(style);
                }
            });
        }
    }

    /**
     * Show or hide the Message
     * @param show true = show -or- false = hide
     */
    public void setShowMessage(boolean show) {
        if(initialized) {
            showMessage = show;
            if(show) {
                textView.setVisibility(VISIBLE);
            }else {
                textView.setVisibility(GONE);
            }
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    showMessage = show;
                    if(show) {
                        textView.setVisibility(VISIBLE);
                    }else {
                        textView.setVisibility(GONE);
                    }
                }
            });
        }
    }

    /**
     * Show or hide the Button
     * @param show true = show -or- false = hide
     */
    public void setShowButton(boolean show) {
        if(initialized) {
            showButton = show;
            if(show) {
                button.setVisibility(VISIBLE);
            }else {
                button.setVisibility(GONE);
            }
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    showButton = show;
                    if(show) {
                        button.setVisibility(VISIBLE);
                    }else {
                        button.setVisibility(GONE);
                    }
                }
            });
        }
    }

    public boolean isShowMessage() {
        return showMessage;
    }

    public boolean isShowButton() {
        return showButton;
    }

    public int getEditTextStyle() {
        return editTextStyle;
    }

    public int getButtonStyle() {
        return buttonStyle;
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public TextWatcher getOnTextChangeListener() {
        return onTextChangeListener;
    }

    public void setOnTextChangeListener(TextWatcher onTextChangeListener) {
        this.onTextChangeListener = onTextChangeListener;
    }
}
