package com.rawedit.immersivesettingsview.items;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.paris.Paris;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.rawedit.immersivesettingsview.R;
import com.rawedit.immersivesettingsview.SettingsView;

public class SliderSettingsItem extends SettingsItem{

    private boolean shouldMinimumBeUseForCurrentValue = true;

    @ColorInt
    private int rippleColor = 0;

    private String minimumPattern = "%f";
    private String currentValuePattern = "%f";

    private final View v;
    private TextView textView;
    private TextView minimum;
    private Slider slider;
    private TextView maximum;

    private Slider.OnChangeListener onChangeListener;
    private LabelFormatter labelFormatter;

    public SliderSettingsItem(Context context) {
        this(context, null);
    }

    public SliderSettingsItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SliderSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SliderSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        v = inflate(context, R.layout.setting_slider, this);
        textView = v.findViewById(R.id.message);
        minimum = v.findViewById(R.id.minimum);
        slider = v.findViewById(R.id.slider);
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                saveCurrentState();
                if(shouldMinimumBeUseForCurrentValue) minimum.setText(putFloatInPattern(currentValuePattern, value));
                if(onChangeListener != null) onChangeListener.onValueChange(slider, value, fromUser);
            }
        });
        slider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                if(labelFormatter != null) {
                    return labelFormatter.getFormattedValue(value);
                }
                return String.valueOf(value);
            }
        });
        maximum = v.findViewById(R.id.maximum);
        applyColors();
        executeQueue();
        initialized = true;
        loadSavedState();
        settingNameSave = "";
    }

    /**
     * Sets the Color for the Slider Track and Thumb
     * @param color the new Slider Color
     */
    @Override
    public void setRippleColor(int color) {
        rippleColor = color;
        if(initialized) {
            applyColors();
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    applyColors();
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
                float f = sharedPreferences.getFloat(settingNameSave, 0f);
                slider.setValue(f);
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
                float f = slider.getValue();
                sharedPreferences.edit().putFloat(settingNameSave, f).apply();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Class Specific

    private void applyColors() {
        if(initialized) {
            slider.setThumbTintList(ColorStateList.valueOf(rippleColor));
            slider.setTrackActiveTintList(ColorStateList.valueOf(rippleColor));
            slider.setTrackInactiveTintList(ColorStateList.valueOf(SettingsView.adjustAlpha(rippleColor, 0.3f)));
            slider.setBackground(SettingsView.getAdaptiveRippleDrawable(getContext().getColor(R.color.white), rippleColor, 0));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    slider.setThumbTintList(ColorStateList.valueOf(rippleColor));
                    slider.setTrackActiveTintList(ColorStateList.valueOf(rippleColor));
                    slider.setTrackInactiveTintList(ColorStateList.valueOf(SettingsView.adjustAlpha(rippleColor, 0.3f)));
                    slider.setBackground(SettingsView.getAdaptiveRippleDrawable(getContext().getColor(R.color.white), rippleColor, 0));
                }
            });
        }
    }

    private String putFloatInPattern(String pattern, float value) {
        return pattern.replaceAll("%f", String.valueOf(value));
    }

    // Getter and Setter

    public void setMessage(String msg) {
        if(initialized) {
            textView.setText(msg);
            if(!settingsNameSaveChanged) setSettingNameSave(String.valueOf(msg.hashCode()));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    textView.setText(msg);
                    if(!settingsNameSaveChanged) setSettingNameSave(String.valueOf(msg.hashCode()));
                }
            });
        }
    }

    public void setMinimum(float min) {
        setMinimum(min, "%f");
    }

    public void setMinimum(float min, String pattern) {
        minimumPattern = pattern;
        if(initialized) {
            slider.setValueFrom(min);
            minimum.setText(putFloatInPattern(pattern, min));
            setShouldMinimumBeUseForCurrentValue(shouldMinimumBeUseForCurrentValue, pattern);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    slider.setValueFrom(min);
                    minimum.setText(putFloatInPattern(pattern, min));
                    setShouldMinimumBeUseForCurrentValue(shouldMinimumBeUseForCurrentValue, pattern);
                }
            });
        }
    }

    public void setValue(float val) {
        if(initialized) {
            slider.setValue(val);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    slider.setValue(val);
                }
            });
        }
    }

    public void setMaximum(float max) {
        setMaximum(max, "%f");
    }

    public void setMaximum(float max, String pattern) {
        if(initialized) {
            slider.setValueTo(max);
            maximum.setText(putFloatInPattern(pattern, max));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    slider.setValueTo(max);
                    maximum.setText(putFloatInPattern(pattern, max));
                }
            });
        }
    }

    public void setSliderStyle(int style) {
        if(initialized) {
            Paris.style(slider).apply(style);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    Paris.style(slider).apply(style);
                }
            });
        }
    }

    public void setStepSize(float stepSize) {
        if(initialized) {
            slider.setStepSize(stepSize);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    slider.setStepSize(stepSize);
                }
            });
        }
    }

    public void setTickVisible(boolean visible) {
        if(initialized) {
            slider.setTickVisible(visible);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    slider.setTickVisible(visible);
                }
            });
        }
    }

    public float getValue() {
        if(slider != null) {
            return slider.getValue();
        }else {
            return 0f;
        }
    }

    public Slider.OnChangeListener getOnChangeListener() {
        return onChangeListener;
    }

    public void setOnChangeListener(Slider.OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public LabelFormatter getLabelFormatter() {
        return labelFormatter;
    }

    public void setLabelFormatter(LabelFormatter labelFormatter) {
        this.labelFormatter = labelFormatter;
    }

    public boolean isShouldMinimumBeUseForCurrentValue() {
        return shouldMinimumBeUseForCurrentValue;
    }

    public void setShouldMinimumBeUseForCurrentValue(boolean shouldIt, String pattern) {
        this.currentValuePattern = pattern;
        setShouldMinimumBeUseForCurrentValue(shouldIt);
    }

    public void setShouldMinimumBeUseForCurrentValue(boolean shouldMinimumBeUseForCurrentValue) {
        this.shouldMinimumBeUseForCurrentValue = shouldMinimumBeUseForCurrentValue;
        if(initialized) {
            if(shouldMinimumBeUseForCurrentValue) {
                minimum.setText(putFloatInPattern(currentValuePattern, slider.getValue()));
            }else {
                setMinimum(slider.getValueFrom(), minimumPattern);
            }
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    if(shouldMinimumBeUseForCurrentValue) {
                        minimum.setText(putFloatInPattern(currentValuePattern, slider.getValue()));
                    }else {
                        setMinimum(slider.getValueFrom(), minimumPattern);
                    }
                }
            });
        }
    }

    public String getCurrentValueAsMinimumPattern() {
        return currentValuePattern;
    }

    public void setCurrentValueAsMinimumPattern(String currentValuePattern) {
        this.currentValuePattern = currentValuePattern;
        setShouldMinimumBeUseForCurrentValue(shouldMinimumBeUseForCurrentValue);
    }
}
