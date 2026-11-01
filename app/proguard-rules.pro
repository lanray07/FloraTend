# Preserve the Amazon SDK's reflective API and annotations.
# https://developer.amazon.com/docs/in-app-purchasing/iap-obfuscate-the-code.html
-dontwarn com.amazon.**
-keep class com.amazon.** { *; }
-keepattributes *Annotation*
