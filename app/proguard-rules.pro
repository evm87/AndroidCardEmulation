# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes EnclosingMethod,Deprecated,InnerClasses,AnnotationDefault,Signature,Exceptions
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations
-keepattributes SourceFile,LineNumberTable,MethodParameters

#Needed for retorolambda
-dontwarn java.lang.invoke.*

-dontnote kotlin.jvm.internal.Reflection
-dontnote kotlin.internal.PlatformImplementationsKt

# Remove logging code
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int w(...);
    public static int d(...);
}


-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

#Keep serializable derivatives
-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}


#Don't warn about Google licensing
-dontnote com.android.vending.licensing.**

-dontnote android.support.**
-keep,includedescriptorclasses class android.support.v4.** { *; }
-keep,includedescriptorclasses interface android.support.v4.app.** { *; }
-keep,includedescriptorclasses class android.support.v4.widget.CursorAdapter { *; }
-keep,includedescriptorclasses class android.support.v4.content.Loader { *; }
-keep,includedescriptorclasses class android.support.v4.content.Loader$OnLoadCompleteListener { *; }
-keep,includedescriptorclasses class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep interface android.support.v7.internal.** { *; }
-dontwarn android.support.v7.**

#Google databinding
-keep class android.databinding.** { *; }

# rxjava
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}

-dontnote rx.internal.util.PlatformDependent

#Turn off notes for Realm
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
#-dontwarn javax.**
-dontwarn io.realm.**
-dontnote io.realm.**

# Google Guava
-keep class com.google.common.io.Resources {
    public static <methods>;
}

-keep class com.google.common.collect.Lists {
    public static ** reverse(**);
}
-keep class * extends com.google.common.base.Charsets {
    public static <fields>;
}

-dontwarn com.google.common.**
-dontnote com.google.common.**

-keep class com.google.common.base.Joiner {
    public static com.google.common.base.Joiner on(java.lang.String);
    public ** join(...);
}

-keep class com.google.common.collect.MapMakerInternalMap$ReferenceEntry
-keep class com.google.common.cache.LocalCache$ReferenceEntry

-dontwarn javax.annotation.**
-dontwarn javax.inject.**

-dontwarn io.reactivex.rxjava2.**
-dontnote io.reactivex.rxjava2.**

-dontwarn rx.internal.**
-dontnote rx.internal.**

-dontwarn net.grandcentrix.thirtyinch.**
-dontnote net.grandcentrix.thirtyinch.**

-dontwarn java.lang.ClassValue
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-dontnote org.apache.**
-dontnote com.google.appengine.api.ThreadManager

#Not sure which lib it comes from but here we specify not to report it.
#-dontnote android.widget.ImageView

#Rules for the library https://github.com/81813780/AVLoadingIndicatorView
-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }

#Turn off notes for Realm
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *


#-dontwarn javax.**
-dontwarn io.realm.**
-dontnote io.realm.**

-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**