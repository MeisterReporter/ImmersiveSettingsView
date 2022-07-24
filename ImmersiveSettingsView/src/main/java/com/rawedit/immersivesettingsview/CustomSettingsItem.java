package com.rawedit.immersivesettingsview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.HashMap;

public class CustomSettingsItem extends SettingsItem {

    private View view;

    private HashMap<String, View> views = new HashMap<>();

    private ChangeRippleColor changeRippleColor;
    private SetupViews setupViews;

    public interface ChangeRippleColor {
        void changeRippleColor(@ColorInt int color);
    }

    public interface SetupViews {
        void setupViews(View root);
    }

    public CustomSettingsItem(Context context, int layout) {
        this(context, null, layout);
    }

    public CustomSettingsItem(Context context, @Nullable AttributeSet attrs, int layout) {
        this(context, attrs, 0, layout);
    }

    public CustomSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int layout) {
        this(context, attrs, defStyleAttr, 0, layout);
    }

    public CustomSettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes, int layout) {
        super(context, attrs, defStyleAttr, defStyleRes);
        type = Type.CUSTOM;
        // Inflating Custom Layout
        if(context.getResources().getIdentifier(String.valueOf(layout), "layout", context.getPackageName()) != 0) {
            view = inflate(context, layout, this);
            if(setupViews != null) setupViews.setupViews(view);
        }else {
            Log.d(TAG, "Passed invalid layout " + layout);
        }
        loadSavedState();
        initialized = true;
    }

    @Override
    public void setRippleColor(int color) {
        if(changeRippleColor != null) changeRippleColor.changeRippleColor(color);
    }

    @Override
    public View getView() {
        return view;
    }

    public ChangeRippleColor getChangeRippleColorListener() {
        return changeRippleColor;
    }

    public void setChangeRippleColorListener(ChangeRippleColor changeRippleColor) {
        this.changeRippleColor = changeRippleColor;
    }

    public SetupViews getSetupViewsListener() {
        return setupViews;
    }

    /**
     * This Listener notifies once when the item is added to the View
     * @param setupViews the Listener
     */
    public void setSetupViewsListener(SetupViews setupViews) {
        this.setupViews = setupViews;
    }

    /**
     * Save the View with a key in the CustomSettingsItem.
     * Use this Method to save Views in the SetupViews listener.
     * @param name the unique key
     * @param v the view
     */
    public void saveViewReference(String name, View v) {
        views.put(name, v);
    }

    /**
     * Get a View via a unique key.
     * Use this method for getting the views in any other case then the SetupViews Listener
     * @param name the key for the saved view
     * @return the view saved before or null if not found
     */
    public View getViewReference(String name) {
        return views.get(name);
    }
}
