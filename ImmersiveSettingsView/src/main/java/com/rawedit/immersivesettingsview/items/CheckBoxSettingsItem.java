package com.rawedit.immersivesettingsview.items;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.rawedit.immersivesettingsview.R;
import com.rawedit.immersivesettingsview.SettingsView;

public class CheckBoxSettingsItem extends SettingsItem {

    private boolean checkBoxColorChanged = false;

    @ColorInt
    int rippleColor = 0;
    @ColorInt
    int checkBoxColor = 0;

    private MaterialCheckBox checkBox;

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public CheckBoxSettingsItem(Context context) {
        this(context, null);
    }

    public CheckBoxSettingsItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckBoxSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CheckBoxSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        type = Type.CHECKBOX;
        View v = inflate(context, R.layout.setting_checkbox, this);
        checkBox = v.findViewById(R.id.materialCheckBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveCurrentState();
                if(onCheckedChangeListener != null) onCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
            }
        });
        executeQueue();
        initialized = true;
        loadSavedState();
        settingNameSave = "";
    }

    @Override
    public void setRippleColor(int color) {
        rippleColor = color;
        if(initialized) {
            checkBox.setBackground(SettingsView.getAdaptiveRippleDrawable(getContext().getColor(R.color.white), rippleColor, 0));
            if(!checkBoxColorChanged) {
                setCheckBoxColor(rippleColor);
            }
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    checkBox.setBackground(SettingsView.getAdaptiveRippleDrawable(getContext().getColor(R.color.white), rippleColor, 0));
                    if(!checkBoxColorChanged) {
                        setCheckBoxColor(rippleColor);
                    }
                }
            });
        }
    }

    @Override
    public MaterialCheckBox getView() {
        return checkBox;
    }

    @Override
    public void loadSavedState() {
        super.loadSavedState();
        if(initialized) {
            try {
                boolean state = sharedPreferences.getBoolean(settingNameSave, false);
                checkBox.setChecked(state);
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
                boolean state = checkBox.isChecked();
                sharedPreferences.edit().putBoolean(settingNameSave, state).apply();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the Color of the Checkbox itself
     * @param color the color in @ColorInt format
     */
    public void setCheckBoxColor(int color) {
        if(initialized) {
            if(!checkBoxColorChanged && color != 0) checkBoxColorChanged = true;
            checkBoxColor = color;
            applyColorToCheckBox();
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    if(!checkBoxColorChanged && color != 0) checkBoxColorChanged = true;
                    checkBoxColor = color;
                    applyColorToCheckBox();
                }
            });
        }
    }

    private void applyColorToCheckBox() {
        if(initialized) {
            ColorStateList colorStateList = new ColorStateList(
                    new int[][] {
                            new int[] { -android.R.attr.state_checked }, // unchecked
                            new int[] {  android.R.attr.state_checked }  // checked
                    },
                    new int[] {
                            getContext().getColor(R.color.defaultRipple),
                            checkBoxColor
                    }
            );
            checkBox.setButtonTintList(colorStateList);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    ColorStateList colorStateList = new ColorStateList(
                            new int[][] {
                                    new int[] { -android.R.attr.state_checked }, // unchecked
                                    new int[] {  android.R.attr.state_checked }  // checked
                            },
                            new int[] {
                                    getContext().getColor(R.color.defaultRipple),
                                    checkBoxColor
                            }
                    );
                    checkBox.setButtonTintList(colorStateList);
                }
            });
        }
    }

    /**
     * Set the message for the Checkbox
     * @param text the message
     */
    public void setText(String text) {
        if(initialized) {
            checkBox.setText(text);
            if(!settingsNameSaveChanged) setSettingNameSave(String.valueOf(text.hashCode()));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    checkBox.setText(text);
                    if(!settingsNameSaveChanged) setSettingNameSave(String.valueOf(text.hashCode()));
                }
            });
        }
    }

    /**
     * Sets the color of the message text
     * @param color the Color in @ColorInt format
     */
    public void setTextColor(int color) {
        if(initialized) {
            checkBox.setTextColor(color);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    checkBox.setTextColor(color);
                }
            });
        }
    }

    /**
     * Set the Location of the Checkbox
     * @param dir LayoutDirection.LTR or LayoutDirection.RTL
     */
    public void setLayoutDirection(int dir) {
        if(initialized) {
            checkBox.setLayoutDirection(dir);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    checkBox.setLayoutDirection(dir);
                }
            });
        }
    }

    public int getRippleColor() {
        return rippleColor;
    }

    public int getCheckBoxColor() {
        return checkBoxColor;
    }

    public CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }
}
