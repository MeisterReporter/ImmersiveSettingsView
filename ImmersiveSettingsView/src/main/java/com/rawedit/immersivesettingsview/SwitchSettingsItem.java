package com.rawedit.immersivesettingsview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SwitchSettingsItem extends SettingsItem {

    public static final String TAG = SwitchSettingsItem.class.getSimpleName();

    private boolean switchColorChanged = false;

    @ColorInt
    private int rippleColor = 0;
    @ColorInt
    private int switchColor = 0;

    private SwitchMaterial mSwitch;

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public SwitchSettingsItem(Context context) {
        this(context, null);
    }

    public SwitchSettingsItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        type = Type.SWITCH;
        rippleColor = context.getColor(R.color.defaultRipple);
        View v = inflate(context, R.layout.setting_switch, this);
        mSwitch = v.findViewById(R.id.switchMaterial);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveCurrentState();
                if(onCheckedChangeListener != null) onCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
            }
        });
        switchColor = Color.BLACK;
        applyColorToSwitch();
        executeQueue();
        initialized = true;
        loadSavedState();
        settingNameSave = "";
    }

    private void applyColorToSwitch() {
        if(initialized) {
            int[][] states = new int[][] {
                    new int[] {-android.R.attr.state_checked},
                    new int[] {android.R.attr.state_checked},
            };
            int[] thumbColors = new int[] {
                    Color.parseColor("#f1f1f1"),
                    switchColor,
            };
            int[] trackColors = new int[] {
                    SettingsView.adjustAlpha(Color.BLACK, 0.3f),
                    SettingsView.adjustAlpha(switchColor, 0.3f),
            };
            mSwitch.setTrackTintList(new ColorStateList(states, trackColors));
            mSwitch.setThumbTintList(new ColorStateList(states, thumbColors));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    int[][] states = new int[][] {
                            new int[] {-android.R.attr.state_checked},
                            new int[] {android.R.attr.state_checked},
                    };
                    int[] thumbColors = new int[] {
                            Color.parseColor("#f1f1f1"),
                            switchColor,
                    };
                    int[] trackColors = new int[] {
                            SettingsView.adjustAlpha(Color.BLACK, 0.3f),
                            SettingsView.adjustAlpha(switchColor, 0.3f),
                    };
                    mSwitch.setTrackTintList(new ColorStateList(states, trackColors));
                    mSwitch.setThumbTintList(new ColorStateList(states, thumbColors));
                }
            });
        }
    }

    @Override
    public void setRippleColor(int color) {
        rippleColor = color;
        if(initialized) {
            mSwitch.setBackground(SettingsView.getAdaptiveRippleDrawable(getContext().getColor(R.color.white), rippleColor, 0));
            if(!switchColorChanged) {
                setSwitchColor(color);
            }
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    mSwitch.setBackground(SettingsView.getAdaptiveRippleDrawable(getContext().getColor(R.color.white), rippleColor, 0));
                    if(!switchColorChanged) {
                        setSwitchColor(color);
                    }
                }
            });
        }
    }

    @Override
    public SwitchMaterial getView() {
        return mSwitch;
    }

    @Override
    public void loadSavedState() {
        super.loadSavedState();
        if(initialized) {
            try {
                Log.d(TAG, "loadSavedState: " + settingNameSave);
                boolean state = sharedPreferences.getBoolean(settingNameSave, false);
                mSwitch.setChecked(state);
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
                boolean state = mSwitch.isChecked();
                sharedPreferences.edit().putBoolean(settingNameSave, state).apply();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the Color of the Switch itself
     * @param color the color in the @ColorInt format
     */
    public void setSwitchColor(int color) {
        Log.d(TAG, "Set Switch color to " + color);
        if(initialized) {
            if(!switchColorChanged && color != 0) switchColorChanged = true;
            switchColor = color;
            applyColorToSwitch();
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    if(!switchColorChanged && color != 0) switchColorChanged = true;
                    switchColor = color;
                    applyColorToSwitch();
                }
            });
        }
    }

    /**
     * Sets the given color to the switch message
     * @param color a color in the @ColorInt format
     */
    public void setTextColor(int color) {
        if(initialized) {
            if(mSwitch != null) mSwitch.setTextColor(color);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    if(mSwitch != null) mSwitch.setTextColor(color);
                }
            });
        }
    }

    /**
     * Setts the message of the Switch to the given String
     * @param text the message to be set to the string
     */
    public void setText(String text) {
        if(initialized) {
            if(mSwitch != null) mSwitch.setText(text);
            if(!settingsNameSaveChanged) setSettingNameSave(String.valueOf(text.hashCode()));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    if(mSwitch != null) mSwitch.setText(text);
                    if(!settingsNameSaveChanged) setSettingNameSave(String.valueOf(text.hashCode()));
                }
            });
        }
    }

    public int getRippleColor() {
        return rippleColor;
    }

    public int getSwitchColor() {
        return switchColor;
    }

    public CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }
}
