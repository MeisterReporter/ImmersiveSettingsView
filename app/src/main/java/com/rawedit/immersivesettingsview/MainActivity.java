package com.rawedit.immersivesettingsview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.rawedit.immersivesettingsview.R;
import com.rawedit.immersivesettingsview.SettingsView;
import com.rawedit.immersivesettingsview.items.CheckBoxSettingsItem;
import com.rawedit.immersivesettingsview.items.CustomSettingsItem;
import com.rawedit.immersivesettingsview.items.EditTextSettingsItem;
import com.rawedit.immersivesettingsview.items.SettingsItem;
import com.rawedit.immersivesettingsview.items.SliderSettingsItem;
import com.rawedit.immersivesettingsview.items.SwitchSettingsItem;
import com.rawedit.immersivesettingsview.items.TextSettingsItem;
import com.rawedit.immersivesettingsview.pages.SettingsPage;

public class MainActivity extends AppCompatActivity {

    private SettingsView settingsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initializing the SettingsView
        settingsView = findViewById(R.id.SettingsView);
        settingsView.setRippleColor(getColor(R.color.purple_200));
        settingsView.setOnPageChangedListener(new SettingsView.OnPageChanged() {
            @Override
            public void pageChanged(String title) {
                if(title.equals(SettingsView.MAIN_PAGE_NAME)) {
                    setTitle(getString(R.string.app_name));
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        getSupportActionBar().setHomeButtonEnabled(false);
                    }
                }else {
                    setTitle(title);
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        getSupportActionBar().setHomeButtonEnabled(true);
                    }
                }
            }
        });
        // Creating an Example Page
        SettingsPage importantSettings = settingsView.createSettingsPage();
        importantSettings.setItemName("Very Important Settings");
        importantSettings.setTitle("Very Important Settings");
        settingsView.add(importantSettings);
        // Creating Example Items
        // Text
        TextSettingsItem item = (TextSettingsItem) settingsView.createSettingsItem(SettingsItem.Type.TEXT);
        item.setText("This is an important Setting you should always be aware of. Use it for triggering dialogs or just displaying a status etc.");
        importantSettings.add(item);
        // Switch
        SwitchSettingsItem item2 = (SwitchSettingsItem) settingsView.createSettingsItem(SettingsItem.Type.SWITCH);
        item2.setSettingNameSave("switch");
        item2.setText("This is an example for a switch Setting, use it for on or off settings.");
        importantSettings.add(item2);
        // Check Box
        CheckBoxSettingsItem item3 = (CheckBoxSettingsItem) settingsView.createSettingsItem(SettingsItem.Type.CHECKBOX);
        item3.setText("This is an example CheckBox, use it for yes or no settings.");
        importantSettings.add(item3);
        // Edit Text
        EditTextSettingsItem item4 = (EditTextSettingsItem) settingsView.createSettingsItem(SettingsItem.Type.EDITTEXT);
        item4.setMessage("Type in your name or email address");
        item4.setEditTextHint("Name or Email");
        item4.setButtonText("Save Setting");
        importantSettings.add(item4);
        // Slider
        SliderSettingsItem item5 = (SliderSettingsItem) settingsView.createSettingsItem(SettingsItem.Type.SLIDER);
        item5.setSliderStyle(R.style.SliderStyle);
        item5.setSettingNameSave("slider");
        item5.setMessage("Use the Slider to change the color mix");
        item5.setMinimum(0, "%f%");
        item5.setMaximum(100, "%f%");
        item5.setStepSize(1f);
        item5.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return value + "%";
            }
        });
        item5.setOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                int mixedColor = ColorUtils.blendARGB(getColor(R.color.purple_200), getColor(R.color.teal_200), value/slider.getValueTo());
                settingsView.setAlternativeColor(mixedColor);
                settingsView.setRippleColor(mixedColor);
            }
        });
        importantSettings.add(item5);
        // Example Items for the Main Page
        CustomSettingsItem itemCustom = (CustomSettingsItem) settingsView.createSettingsItem(SettingsItem.Type.CUSTOM, R.layout.setting_text);
        itemCustom.setSetupViewsListener(new CustomSettingsItem.SetupViews() {
            @Override
            public void setupViews(View root) {
                MaterialButton mb = root.findViewById(R.id.textview);
                mb.setText("This is a custom View");
                mb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Test")
                                .setMessage("Hi, Im a Test Dialog called by a Custom Settings Item.")
                                .setPositiveButton("Close", null)
                                .show();
                    }
                });
                itemCustom.saveViewReference("MaterialButton", mb);
            }
        });
        itemCustom.setChangeRippleColorListener(new CustomSettingsItem.ChangeRippleColor() {
            @Override
            public void changeRippleColor(int color) {
                MaterialButton mb = (MaterialButton) itemCustom.getViewReference("MaterialButton");
                mb.setRippleColor(ColorStateList.valueOf(color));
            }
        });
        settingsView.add(itemCustom);
    }

    @Override
    public void onBackPressed() {
        if(!settingsView.back()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}