-injars  build/libs/MagicWands.jar
-outjars build/libs/proguard.jar

-libraryjar "C:\Program Files\Java\jdk1.8.0_281\jre\lib\rt.jar"
-libraryjar "D:\Local Disk\Users\User\Documents\GitHub\MagicWands\libs\spigot-1.16.5.jar"

-dontwarn net.minecraft.**
-dontwarn org.bukkit.**
-dontwarn com.google.**
-dontwarn com.comphenix.**
-dontwarn android.**
-dontwarn org.hibernate.**
-dontwarn com.sk89q.worldedit**
-dontwarn com.sk89q.worldguard**
-dontwarn net.milkbowl.vault.economy**
-dontwarn **metrics**
-dontwarn javax.crypto.**
-dontwarn javassist.**
-dontwarn **slf4j**
-dontwarn io.micrometer.core.instrument.MeterRegistry
-dontwarn org.codehaus.mojo.**

#-dontobfuscate
#-dontoptimize

# Keep your main class
-keep,allowobfuscation,allowoptimization class * extends org.bukkit.plugin.java.JavaPlugin { *; }

# Keep event handlers
-keep,allowobfuscation,allowoptimization class * extends org.bukkit.event.Listener {
    @org.bukkit.event.EventHandler <methods>;
}

# Keep main package name (spigot forum rule)
-keeppackagenames "com.github.secretx33.magicwands"

# Keep public enum names
-keepclassmembers public enum com.github.secretx33.magicwands.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep all ProtocolLib packet listeners
-keep,allowobfuscation,allowoptimization public class com.github.secretx33.magicwands.packetlisteners.** { *; }

# Keep static fields in custom Events
-keepclassmembers class com.github.secretx33.magicwands.** extends org.bukkit.event.Event {
    @com.github.secretx33.dependencies.kotlin.jvm.JvmStatic <fields>;
    @com.github.secretx33.dependencies.kotlin.jvm.JvmStatic <methods>;
}

# Keep KoinApiExtension annotation
-keep,allowobfuscation @interface com.github.secretx33.dependencies.koin.core.component.KoinApiExtension

# If your goal is obfuscating and making things harder to read, repackage your classes with this rule
-repackageclasses "com.github.secretx33.magicwands"
#-allowaccessmodification
-adaptresourcefilecontents "plugin.yml"
-adaptresourcefilecontents "config.yml"
-adaptresourcefilecontents "messages.yml"

# Some attributes that you'll need to keep (to be honest I'm not sure which ones really need to be kept here, but this is what works for me)
-keepattributes Exceptions,Signature,SourceFile,LineNumberTable,EnclosingMethod,InnerClasses
#-keepattributes Exceptions,Signature,Deprecated,LineNumberTable,*Annotation*,EnclosingMethod
#-keepattributes LocalVariableTable,LocalVariableTypeTable,Exceptions,InnerClasses,Signature,Deprecated,LineNumberTable,*Annotation*,EnclosingMethod
