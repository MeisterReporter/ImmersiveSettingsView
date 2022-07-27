package com.rawedit.immersivesettingsview.items;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.button.MaterialButton;
import com.rawedit.immersivesettingsview.R;

public class TextSettingsItem extends SettingsItem {

    public static final String TAG = TextSettingsItem.class.getSimpleName();

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;

    private MaterialButton textView;

    private OnClickListener onClickListener;

    public TextSettingsItem(Context context) {
        this(context, null);
    }

    public TextSettingsItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TextSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        type = Type.TEXT;
        // Inflating Layout
        inflate(context, R.layout.setting_text, this);
        // Finding Views
        textView = findViewById(R.id.textview);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentState();
                if(onClickListener != null) onClickListener.onClick(v);
            }
        });
        executeQueue();
        initialized = true;
        loadSavedState();
        settingNameSave = "";
    }

    /**
     * Set the Displayed Text
     * @param string text to be dispalyed
     */
    public void setText(String string) {
        if(initialized) {
            textView.setText(string);
            if(!settingsNameSaveChanged) setSettingNameSave(String.valueOf(string.hashCode()));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    textView.setText(string);
                    if(!settingsNameSaveChanged) setSettingNameSave(String.valueOf(string.hashCode()));
                }
            });
        }
    }

    /**
     * Change the text color
     * @param color the color in @ColorInt format
     */
    public void setTextColor(@ColorInt int color) {
        if(initialized) {
            textView.setTextColor(color);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    textView.setTextColor(color);
                }
            });
        }
    }

    public void setRippleColor(@ColorInt int color) {
        Log.d(TAG, "Setting RippleColor: " + color);
        if(initialized) {
            textView.setRippleColor(ColorStateList.valueOf(color));
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    textView.setRippleColor(ColorStateList.valueOf(color));
                }
            });
        }
    }

    public void setDrawable(Drawable d) {
        if(initialized) {
            setDrawable(d, LEFT);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    setDrawable(d, LEFT);
                }
            });
        }
    }

    public void setDrawable(@DrawableRes int res) {
        if(initialized) {
            setDrawable(res, LEFT);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    setDrawable(res, LEFT);
                }
            });
        }
    }

    public void setDrawable(@DrawableRes int res, int position) {
        if(initialized) {
            setDrawable(AppCompatResources.getDrawable(getContext(), res), position);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    setDrawable(AppCompatResources.getDrawable(getContext(), res), position);
                }
            });
        }
    }

    public void setDrawable(Drawable d, int position) {
        if(initialized) {
            switch(position) {
                case LEFT:
                    textView.setCompoundDrawables(d, null, null, null);
                    break;
                case RIGHT:
                    textView.setCompoundDrawables(null, null, d, null);
                    break;
                case TOP:
                    textView.setCompoundDrawables(null, d, null, null);
                    break;
                case BOTTOM:
                    textView.setCompoundDrawables(null, null, null, d);
                    break;
            }
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    switch(position) {
                        case LEFT:
                            textView.setCompoundDrawables(d, null, null, null);
                            break;
                        case RIGHT:
                            textView.setCompoundDrawables(null, null, d, null);
                            break;
                        case TOP:
                            textView.setCompoundDrawables(null, d, null, null);
                            break;
                        case BOTTOM:
                            textView.setCompoundDrawables(null, null, null, d);
                            break;
                    }
                }
            });
        }
    }

    @Override
    public MaterialButton getView() {
        return textView;
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
