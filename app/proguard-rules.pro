# Add project specific ProGuard rules here.
#
# http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Hilt/Dagger classes
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class * extends dagger.hilt.internal.GeneratedComponentManager

# Keep Retrofit, OkHttp, and Gson classes
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn com.google.gson.**

# Keep Retrofit service interfaces and their methods
-keep interface com.example.picchallenge.data.remote.** { *; }
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# Keep annotations, which are used by many libraries.
-keepattributes *Annotation*

# Keep generic signatures, which is crucial for Retrofit/Gson deserialization.
-keepattributes Signature

# Keep all data model classes and their members in the 'data.model' package.
# This prevents ProGuard from removing fields that Gson needs for deserialization.
-keep class com.example.picchallenge.data.model.** { *; }


# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep security/crypto classes
-keep class androidx.security.** { *; }
-dontwarn androidx.security.**
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }
