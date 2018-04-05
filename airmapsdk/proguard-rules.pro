# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Gandhi/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepattributes InnerClasses
-dontoptimize


# Proguard for Mapbox, might be unnecessary stuff in here. Copied from their sample app

# Retrofit 2
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**

-dontwarn okio.**
-dontwarn okhttp3.**
-keep class retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-dontwarn retrofit.**

# Picasso
-dontwarn com.squareup.okhttp.**

-dontwarn android.support.**
-dontwarn java.lang.**
-dontwarn org.codehaus.**
-keep class com.google.** { *; }
-dontwarn com.google.**
-dontwarn java.nio.**
-dontwarn javax.annotation.**

-keep class com.segment.analytics.** { *; }
-keep class com.mapbox.mapboxandroiddemo.model.usermodel.** { *; }
# Proguard for Mapbox, might be unnecessary stuff in here. Copied from their sample app
