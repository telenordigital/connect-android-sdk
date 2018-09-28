# Connect SDK
-keep class com.telenor.** { *; }

# Retrofit 2: https://github.com/square/retrofit#r8--proguard
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature

# Retain service method parameters.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# OkHttp 3: https://github.com/square/okhttp#proguard
# Okio: https://github.com/square/okio#proguard
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Also you must note that if you are using GSON for conversion from JSON to POJO representation, you must ignore those POJO classes from being obfuscated.
# Here include the POJO's that have you have created for mapping JSON response to POJO for example.

# GSON: https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**

# bouncycastle
-dontwarn org.bouncycastle.**
