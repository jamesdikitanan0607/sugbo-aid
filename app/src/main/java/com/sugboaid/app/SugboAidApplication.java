package com.sugboaid.app;

import android.app.Application;
import com.sugboaid.app.util.ThemeUtils;
import com.sugboaid.app.util.SampleDataGenerator;

public class SugboAidApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize theme utilities
        ThemeUtils.initialize(this);
        ThemeUtils.applyTheme(this);
        
        // Generate sample data for demo purposes
        SampleDataGenerator.generateSampleData(this);
    }
}