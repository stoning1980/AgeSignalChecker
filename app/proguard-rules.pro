# Age Signals SDK — keep public API surface used via reflection/listeners.
-keep class com.google.android.play.agesignals.** { *; }
-keep interface com.google.android.play.agesignals.** { *; }

# The app reads error codes reflectively (getErrorCode / getStatusCode).
-keepclassmembers class * {
    public int getErrorCode();
    public int getStatusCode();
}
