# R8/ProGuard rules for JOY DJ Remix
-keep public class com.joy.djremix.MainActivity {
    public protected *;
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
