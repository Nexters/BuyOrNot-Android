# [Common] Android
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-dontwarn sun.misc.**
-dontwarn javax.annotation.**

# [Kotlin]
-keep class kotlin.Metadata { *; }

# [Hilt / Dagger]
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *
-keep interface * extends javax.inject.Provider

# [Retrofit / OkHttp]
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes *Annotation*
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# [Kotlinx Serialization]
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# [Domain/Data Models] - API 통신 데이터 클래스 보존
# 모든 모듈의 domain.model, network.model 하위의 클래스들을 보존합니다.
-keep class com.sseotdabwa.buyornot.domain.model.** { *; }
-keep class com.sseotdabwa.buyornot.core.network.model.** { *; }

# [Coil]
-keep class coil.** { *; }
-dontwarn coil.**

# [Firebase]
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# [Lottie]
-keep class com.airbnb.lottie.** { *; }

# [Kakao SDK]
-keep class com.kakao.sdk.** { *; }
-dontwarn com.kakao.sdk.**

# [Jetpack Compose]
-keep class androidx.compose.runtime.RecomposeScopeImpl { *; }
-keep class * implements androidx.compose.runtime.Parcelable { *; }
