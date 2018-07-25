/*
 * Copyright (C) 2016 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.slim.device.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import com.slim.device.SRGBModeSwitch;
import com.slim.device.DCIModeSwitch;
import android.preference.TwoStatePreference;
import com.slim.device.KernelControl;
import com.slim.device.R;
import com.slim.device.util.FileUtils;
import android.util.Log;
import android.text.TextUtils;
import android.provider.Settings;


public class DeviceSettings extends PreferenceActivity
        implements OnPreferenceChangeListener {

    public static final String KEY_SRGB_SWITCH = "srgb";
    public static final String KEY_DCI_SWITCH = "dci";
    private static final String KEY_CATEGORY_GRAPHICS = "graphics";
    public static final String SLIDER_SWAP_NODE = "/proc/s1302/key_rep";
    public static final String KEYCODE_SLIDER_TOP = "slider_top";
    public static final String KEYCODE_SLIDER_MIDDLE = "slider_middle";
    public static final String KEYCODE_SLIDER_BOTTOM = "slider_bottom";
    public static final String BUTTON_EXTRA_KEY_MAPPING = "/sys/devices/virtual/switch/tri-state-key/state";
    private SwitchPreference mSliderSwap;
    private ListPreference mSliderModeTop;
    private ListPreference mSliderModeCenter;
    private ListPreference mSliderModeBottom;
    private TwoStatePreference mSRGBModeSwitch;
    private TwoStatePreference mDCIModeSwitch;

@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main);

        mSliderSwap = (SwitchPreference) findPreference("button_swap");
        mSliderSwap.setOnPreferenceChangeListener(this);

        mSliderModeTop = (ListPreference) findPreference(KEYCODE_SLIDER_TOP);
        mSliderModeTop.setOnPreferenceChangeListener(this);
        int sliderModeTop = getSliderAction(0);
        int valueIndex = mSliderModeTop.findIndexOfValue(String.valueOf(sliderModeTop));
        mSliderModeTop.setValueIndex(valueIndex);
        mSliderModeTop.setSummary(mSliderModeTop.getEntries()[valueIndex]);

        mSliderModeCenter = (ListPreference) findPreference(KEYCODE_SLIDER_MIDDLE);
        mSliderModeCenter.setOnPreferenceChangeListener(this);
        int sliderModeCenter = getSliderAction(1);
        valueIndex = mSliderModeCenter.findIndexOfValue(String.valueOf(sliderModeCenter));
        mSliderModeCenter.setValueIndex(valueIndex);
        mSliderModeCenter.setSummary(mSliderModeCenter.getEntries()[valueIndex]);

        mSliderModeBottom = (ListPreference) findPreference(KEYCODE_SLIDER_BOTTOM);
        mSliderModeBottom.setOnPreferenceChangeListener(this);
        int sliderModeBottom = getSliderAction(2);
        valueIndex = mSliderModeBottom.findIndexOfValue(String.valueOf(sliderModeBottom));
        mSliderModeBottom.setValueIndex(valueIndex);
        mSliderModeBottom.setSummary(mSliderModeBottom.getEntries()[valueIndex]);
        mSRGBModeSwitch = (TwoStatePreference) findPreference(KEY_SRGB_SWITCH);
        mSRGBModeSwitch.setEnabled(SRGBModeSwitch.isSupported());
        mSRGBModeSwitch.setChecked(SRGBModeSwitch.isCurrentlyEnabled(this));
        mSRGBModeSwitch.setOnPreferenceChangeListener(new SRGBModeSwitch());

        mDCIModeSwitch = (TwoStatePreference) findPreference(KEY_DCI_SWITCH);
        mDCIModeSwitch.setEnabled(DCIModeSwitch.isSupported());
        mDCIModeSwitch.setChecked(DCIModeSwitch.isCurrentlyEnabled(this));
        mDCIModeSwitch.setOnPreferenceChangeListener(new DCIModeSwitch());


    }

    private void setSummary(ListPreference preference, String file) {
        String keyCode;
        if ((keyCode = FileUtils.readOneLine(file)) != null) {
            preference.setValue(keyCode);
            preference.setSummary(preference.getEntry());
        }
    }



@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {



        if (preference == mSliderSwap) {
           Boolean value = (Boolean) newValue;
          FileUtils.writeLine(KernelControl.SLIDER_SWAP_NODE, value ? "1" : "0");
         }

        if (preference == mSliderModeTop) {
            String value = (String) newValue;
            int sliderMode = Integer.valueOf(value);
            setSliderAction(0, sliderMode);
            int valueIndex = mSliderModeTop.findIndexOfValue(value);
            mSliderModeTop.setSummary(mSliderModeTop.getEntries()[valueIndex]);
        }
        if (preference == mSliderModeCenter) {
            String value = (String) newValue;
            int sliderMode = Integer.valueOf(value);
            setSliderAction(1, sliderMode);
            int valueIndex = mSliderModeCenter.findIndexOfValue(value);
            mSliderModeCenter.setSummary(mSliderModeCenter.getEntries()[valueIndex]);
        }
        if (preference == mSliderModeBottom) {
            String value = (String) newValue;
            int sliderMode = Integer.valueOf(value);
            setSliderAction(2, sliderMode);
            int valueIndex = mSliderModeBottom.findIndexOfValue(value);
            mSliderModeBottom.setSummary(mSliderModeBottom.getEntries()[valueIndex]);
        }
        return true;
}

    private int getSliderAction(int position) {
        String value = Settings.System.getString(getContentResolver(),
                    BUTTON_EXTRA_KEY_MAPPING);
        final String defaultValue = "5,3,0";

        if (value == null) {
            value = defaultValue;
        } else if (value.indexOf(",") == -1) {
            value = defaultValue;
        }
        try {
            String[] parts = value.split(",");
            return Integer.valueOf(parts[position]);
        } catch (Exception e) {
        }
        return 0;
    }

    private void setSliderAction(int position, int action) {
        String value = Settings.System.getString(getContentResolver(),
                    BUTTON_EXTRA_KEY_MAPPING);
        final String defaultValue = "5,3,0";

        if (value == null) {
            value = defaultValue;
        } else if (value.indexOf(",") == -1) {
            value = defaultValue;
        }
        try {
            String[] parts = value.split(",");
            parts[position] = String.valueOf(action);
            String newValue = TextUtils.join(",", parts);
            Settings.System.putString(getContentResolver(),
                    BUTTON_EXTRA_KEY_MAPPING, newValue);
            Log.d("maxwen", newValue);
        } catch (Exception e) {
        }
}
}
