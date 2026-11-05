package io.github.seifeldinnasser24.hesabaty;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(HesabatySysPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
