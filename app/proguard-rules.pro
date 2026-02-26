# ============================================================
# [Common] Android
# ============================================================
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes Exceptions
-dontwarn sun.misc.**
-dontwarn javax.annotation.**

# Enum 보존
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable 보존
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Serializable 보존
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}

# ============================================================
# [Kotlin]
# ============================================================
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# ============================================================
# [Kotlinx Coroutines]
# ============================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ============================================================
# [Kotlinx Serialization]
# ============================================================
-keep class kotlinx.serialization.** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-dontwarn kotlinx.serialization.**

# ============================================================
# [Hilt / Dagger]
# ============================================================
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep interface * extends javax.inject.Provider
-dontwarn dagger.hilt.**

# ============================================================
# [Retrofit / OkHttp]
# ============================================================
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================================
# [Jetpack Compose]
# ============================================================
-keep class androidx.compose.runtime.RecomposeScopeImpl { *; }
-keep class * implements androidx.compose.runtime.Stable { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ============================================================
# [AndroidX Lifecycle / ViewModel]
# ============================================================
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ============================================================
# [Navigation Compose]
# ============================================================
-keep class androidx.navigation.** { *; }

# ============================================================
# [AndroidX DataStore]
# ============================================================
-keep class androidx.datastore.** { *; }

# ============================================================
# [AndroidX Credentials / Google Sign-In]
# ============================================================
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.libraries.identity.googleid.**

# ============================================================
# [Coil]
# ============================================================
-keep class coil.** { *; }
-dontwarn coil.**

# ============================================================
# [Firebase]
# ============================================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Crashlytics: 스택 트레이스 보존을 위해 Exception 하위 클래스 유지
-keep public class * extends java.lang.Exception

# ============================================================
# [Lottie]
# ============================================================
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ============================================================
# [Kakao SDK]
# ============================================================
-keep class com.kakao.sdk.** { *; }
-dontwarn com.kakao.sdk.**

# ============================================================
# [Domain / Data Models] - API 통신 데이터 클래스 보존
# ============================================================
# Domain models
-keep class com.sseotdabwa.buyornot.domain.model.** { *; }

# Network DTOs (request / response)
-keep class com.sseotdabwa.buyornot.core.network.dto.** { *; }

# DataStore preferences holder
-keep class com.sseotdabwa.buyornot.core.datastore.** { *; }

# ============================================================
# [App] Firebase Cloud Messaging Service
# ============================================================
-keep class com.sseotdabwa.buyornot.notification.** { *; }
