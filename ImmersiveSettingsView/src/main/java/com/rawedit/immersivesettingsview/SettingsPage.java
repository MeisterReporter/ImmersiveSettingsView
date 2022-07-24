package com.rawedit.immersivesettingsview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingsPage extends ScrollView {

    public static final String TAG = SettingsPage.class.getSimpleName();

    private boolean initialized = false;
    private boolean showDividers = true;

    @ColorInt
    private int rippleColor = 0;

    private String title = "";
    private String itemName = "";

    private HashMap<String, SettingsItem> items = new HashMap<>();

    private LinearLayout layout;

    private ArrayList<Runnable> queue = new ArrayList<>();

    public SettingsPage(Context context) {
        this(context, null);
    }

    public SettingsPage(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsPage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SettingsPage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        layout = new LinearLayout(context, attrs, defStyleAttr, defStyleRes);
        layout.setOrientation(LinearLayout.VERTICAL);
        setVerticalScrollBarEnabled(true);
        addView(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if(showDividers) {
            layout.setDividerDrawable(AppCompatResources.getDrawable(getContext(), com.google.android.material.R.drawable.abc_list_divider_material));
            layout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        }else {
            layout.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        }
        initialized = true;
        for(Runnable r : queue) {
            r.run();
        }
        queue.clear();
    }

    public void add(SettingsItem item) {
        add(item, String.valueOf(items.size()));
    }

    public void add(SettingsItem item, String name) {
        items.put(name, item);
        if(initialized) {
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layout.addView(item, items.size() - 1, layoutParams);
            Log.d(TAG, "Adding view " + items.size() + " to layout");
            item.setRippleColor(rippleColor);
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layout.addView(item, items.size() - 1, layoutParams);
                    Log.d(TAG, "Adding view " + items.size() + " to layout");
                    item.setRippleColor(rippleColor);
                }
            });
        }
    }

    /**
     * Sets the Ripple color of all descendant items
     * @param color the ripple color in @ColorInt format
     */
    public void setRippleColor(@ColorInt int color) {
        this.rippleColor = color;
        if(initialized) {
            for(SettingsItem item : items.values()) {
                item.setRippleColor(color);
            }
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    for(SettingsItem item : items.values()) {
                        item.setRippleColor(color);
                    }
                }
            });
        }
    }

    public void setAlternativeColor(@ColorInt int color) {
        if(initialized) {
            for(SettingsItem o : items.values()) {
                if(o instanceof SwitchSettingsItem) {
                    SwitchSettingsItem i = (SwitchSettingsItem) o;
                    i.setSwitchColor(color);
                }else if(o instanceof CheckBoxSettingsItem) {
                    CheckBoxSettingsItem i = (CheckBoxSettingsItem) o;
                    i.setCheckBoxColor(color);
                }else if(o instanceof EditTextSettingsItem) {
                    EditTextSettingsItem i = (EditTextSettingsItem) o;
                    i.setStrokeColor(color);
                }else if(o instanceof SliderSettingsItem) {
                    SliderSettingsItem i = (SliderSettingsItem) o;
                    i.setRippleColor(color);
                }
            }
        }else {
            queue.add(new Runnable() {
                @Override
                public void run() {
                    for(SettingsItem o : items.values()) {
                        if(o instanceof SwitchSettingsItem) {
                            SwitchSettingsItem i = (SwitchSettingsItem) o;
                            i.setSwitchColor(color);
                        }else if(o instanceof CheckBoxSettingsItem) {
                            CheckBoxSettingsItem i = (CheckBoxSettingsItem) o;
                            i.setCheckBoxColor(color);
                        }else if(o instanceof EditTextSettingsItem) {
                            EditTextSettingsItem i = (EditTextSettingsItem) o;
                            i.setStrokeColor(color);
                        }else if(o instanceof SliderSettingsItem) {
                            SliderSettingsItem i = (SliderSettingsItem) o;
                            i.setRippleColor(color);
                        }
                    }
                }
            });
        }
    }

    public void updateLayout() {
        requestLayout();
        for(SettingsItem i : items.values()) {
            if(i != null) i.updateLayout();
        }
    }

    public void resetStates() {
        for(SettingsItem i : items.values()) {
            if(i != null) i.loadSavedState();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isShowDividers() {
        return showDividers;
    }

    public void setShowDividers(boolean showDividers) {
        this.showDividers = showDividers;
    }
}
