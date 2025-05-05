package com.example.expense_tracker.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.expense_tracker.R;
import com.example.expense_tracker.activities.MainActivity;
import com.example.expense_tracker.utils.LocaleHelper;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        
        // Get the language preference
        ListPreference languagePreference = findPreference("language_preference");
        
        if (languagePreference != null) {
            // Set the current language as default
            String currentLanguage = LocaleHelper.getLanguage(requireContext());
            languagePreference.setValue(currentLanguage);
            
            // Set up the listener
            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                // Apply the new language
                String language = newValue.toString();
                LocaleHelper.setLocale(requireContext(), language);
                
                // Restart the activity to apply changes
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                requireActivity().finish();
                
                return true;
            });
        }
    }
}