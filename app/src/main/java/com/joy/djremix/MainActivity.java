package com.joy.djremix;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);

        webView.setWebViewClient(new WebViewClient());

        // JS bridge: save a base64 recording to Downloads
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void saveRecording(final String base64Data, final String filename) {
                try {
                    final byte[] data = Base64.decode(base64Data, Base64.DEFAULT);
                    String name = (filename == null || filename.isEmpty())
                            ? "Joy_Remix_" + System.currentTimeMillis() + ".webm" : filename;
                    if (Build.VERSION.SDK_INT >= 29) {
                        ContentValues cv = new ContentValues();
                        cv.put(MediaStore.Downloads.DISPLAY_NAME, name);
                        cv.put(MediaStore.Downloads.MIME_TYPE, "audio/webm");
                        cv.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                        Uri uri = getContentResolver().insert(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                        if (uri != null) {
                            OutputStream os = getContentResolver().openOutputStream(uri);
                            if (os != null) { os.write(data); os.close(); }
                        }
                    } else {
                        File f = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS), name);
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(data);
                        fos.close();
                    }
                    final String okName = name.replace("'", "");
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            webView.evaluateJavascript(
                                "if(window.onSaved)onSaved('" + okName + "');", null);
                        }
                    });
                } catch (Exception ignored) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            webView.evaluateJavascript(
                                "if(window.onSaved)onSaved('');", null);
                        }
                    });
                }
            }
        }, "JOY");

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl("file:///android_asset/www/index.html");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
