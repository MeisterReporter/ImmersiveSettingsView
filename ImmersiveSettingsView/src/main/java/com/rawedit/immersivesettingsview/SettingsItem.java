package com.rawedit.immersivesettingsview;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public abstract class SettingsItem extends FrameLayout {

    public static final String TAG = SettingsItem.class.getSimpleName();

    protected boolean initialized = false;
    protected boolean settingsNameSaveChanged = false;

    protected Type type = Type.TEXT;

    protected String settingNameSave = null;

    protected ArrayList<Runnable> queue = new ArrayList<>();

    protected OnStateLoad onStateLoad;
    protected OnStateSave onStateSave;

    protected SharedPreferences sharedPreferences;

    // Enums

    public enum Type {
        /**
         * Parameter for initializing a TextSettingsItem.
         * Shows a simple Message which is clickable.
         * State Saving must be handled via the OnStateSave or OnStateLoad listeners.
         */
        TEXT,
        /**
         * Parameter for initializing a SwitchSettingsItem.
         * Shows a Switch with a Message.
         * The State Saving is handled automatically but can be modified by setting the OnStateSave or OnStateLoad Listeners.
         * It can also be disabled by setting the SettingNameSave to null.
         */
        SWITCH,
        /**
         * Parameter for initializing a CheckBoxSettingsItem.
         * Shows a CheckBox with a Message, by default at the left (and the CheckBox at the Right).
         * The State Saving is handled automatically but can be modified by setting the OnStateSave or OnStateLoad Listeners.
         * It can also be disabled by setting the SettingNameSave to null.
         */
        CHECKBOX,
        /**
         * Parameter for initializing a EditTextSettingsItem.
         * Shows a Message (can be hidden), EditText and a Button (can be hidden) in this order vertically.
         * The State Saving is handled automatically but can be modified by setting the OnStateSave or OnStateLoad Listeners.
         * It can also be disabled by setting the SettingNameSave to null.
         * If the Button is invisible the Saving is handled via the onTextChangeListener else via a the onClickListener.
         */
        EDITTEXT,
        /**
         * Parameter for initializing a SliderSettingsItem.
         * Shows a Message and a Slider with Minimum (left) and Maximum (right) labels.
         * The State Saving is handled automatically but can be modified by setting the OnStateSave or OnStateLoad Listeners.
         * It can also be disabled by setting the SettingNameSave to null.
         * The Message should not be hidden.
         */
        SLIDER,
        /**
         * Parameter for initializing a CustomSettingsItem
         * It must get a own layout resource in the Constructor.
         * The View shows exactly the layout you defined in the Constructor.
         * For initializing all the self defined views us the onSetupViews Listeners which will be called automatically after adding the View to a parent.
         * For saving/loading the views use the saveViewReference and getViewReference methods.
         * For the ripple color to work the RippleColorChangeListener must be set and the change must be handled in it.
         * The State Handling must be completely handled by yourself using the OnStateSave and OnStateLoad Listeners.
         */
        CUSTOM,
    }

    // Interfaces

    public interface OnStateLoad {
        void loadingState(SharedPreferences prefs, String option);
    }

    public interface OnStateSave {
        void savingState(SharedPreferences prefs, String option);
    }

    // Constructor

    public SettingsItem(Context context) {
        this(context, null);
    }

    public SettingsItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        sharedPreferences = getContext().getSharedPreferences(SettingsView.settingsName, Context.MODE_PRIVATE);
    }

    // Class specific Methods

    protected void executeQueue() {
        for(Runnable r : queue) {
            new Handler(Looper.getMainLooper()).post(r);
        }
    }

    /**
     * Load the last saved state of the Item.
     * Must have a valid SettingNameSave to work.
     * (For CustomSettingsItems the states must be handled by hand with the onStateLoad Listener)
     */
    public void loadSavedState() {
        if(settingNameSave == null) return;
        if(settingNameSave.isEmpty()) {
            String error = "The Setting Name save is Empty, so loading the last State will fail. It is Required to set a Setting Name with setSettingNameSave(String name)";
            try {
                throw new IllegalArgumentException(error);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(onStateLoad != null) onStateLoad.loadingState(sharedPreferences, settingNameSave);
    }

    /**
     * Saves the current state of the Item.
     * Must have a valid SettingsNameSave to work.
     * (For CustomSettingsItems the states must be saved by hand with the onStateSave Listener)
     */
    public void saveCurrentState() {
        if(settingNameSave == null) return;
        if(settingNameSave.isEmpty()) {
            String error = "The Setting Name save is Empty, so saving the last State will fail. It is Required to set a Setting Name with setSettingNameSave(String name)";
            try {
                throw new IllegalArgumentException(error);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(onStateSave != null) onStateSave.savingState(sharedPreferences, settingNameSave);
    }

    public void updateLayout() {
        requestLayout();
        for(int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.requestLayout();
        }
    }

    // Static Methods

    public static SettingsItem createSettingsItem(Type type, Context c) {
        switch (type) {
            case TEXT:
                return new TextSettingsItem(c);
            case SWITCH:
                return new SwitchSettingsItem(c);
            case CHECKBOX:
                return new CheckBoxSettingsItem(c);
            case EDITTEXT:
                return new EditTextSettingsItem(c);
            case SLIDER:
                return new SliderSettingsItem(c);
            default:
                return null;
        }
    }

    public static SettingsItem createSettingsItem(Type type, Context c, int layout) {
        if (type == Type.CUSTOM) {
            return new CustomSettingsItem(c, layout);
        }
        return createSettingsItem(type, c);
    }

    // Abstract Methods

    /**
     * Sets the Ripple color for All Components
     * Depending on the Settings Item it could also set any other empty color
     * (Must be handled separately for CustomSettingsItem)
     * @param color the new Ripple Color
     */
    abstract public void setRippleColor(@ColorInt int color);

    /**
     * Get the Important view of the SettingsItem
     * @return the View
     */
    abstract public View getView();

    // Getter and Setter

    public Type getType() {
        return type;
    }

    /**
     * Set the Android Internal Setting name
     * (Required for CustomSettingsItem)
     * @param name Setting Name
     */
    public void setSettingNameSave(String name) {
        Log.d(TAG, "setSettingNameSave: " + name);
        if(name != null && !name.isEmpty() && !settingsNameSaveChanged) settingsNameSaveChanged = true;
        settingNameSave = name;
        loadSavedState();
    }

    public String getSettingNameSave() {
        return settingNameSave;
    }

    public OnStateLoad getOnStateLoad() {
        return onStateLoad;
    }

    public void setOnStateLoad(OnStateLoad onStateLoad) {
        this.onStateLoad = onStateLoad;
    }

    public OnStateSave getOnStateSave() {
        return onStateSave;
    }

    public void setOnStateSave(OnStateSave onStateSave) {
        this.onStateSave = onStateSave;
    }
}
