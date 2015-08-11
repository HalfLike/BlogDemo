package com.halflike.dynamicload;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.halflike.module.ModuleInterface;

import java.io.File;
import java.util.Enumeration;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * Created by luox on 15/6/3.
 */
public class MainActivity extends Activity {

    static final String TAG = "dynamic";
    ModuleInterface mModule = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mModule != null) {
                    Toast.makeText(getApplicationContext(), mModule.print("load succeed"), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "load faild", Toast.LENGTH_LONG).show();
                }
            }
        });
        loadModule();
    }

    void logger(String msg) {
        Log.d(TAG, msg);
    }

    public void loadModule() {
        String dexPath = Environment.getExternalStorageDirectory() + File.separator +  "output.jar";
        File optimizedDir = this.getCacheDir();
        if (! new File(dexPath).exists()) {
            logger("dexFile is no exits");
            return;
        }

        DexClassLoader dexClassLoader = new DexClassLoader(dexPath,
                optimizedDir.getAbsolutePath(), null, this.getClassLoader());
        try {
            // 显示 dex 文件中的类名
            /*
            DexFile dx = DexFile.loadDex(dexPath,File.createTempFile("opt", "dex",
                    getCacheDir()).getPath(), 0);
            for (Enumeration<String> classNames = dx.entries(); classNames.hasMoreElements(); ) {
                logger(classNames.nextElement());
            }
            */
            Class<?> module = dexClassLoader.loadClass("com.halflike.module.QQModule");
            mModule = (ModuleInterface) module.newInstance();
            logger(mModule.print("load success"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
