pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0" 
        
        id("com.google.gms.google-services") version "4.4.2" //4.4.3
    }
}

rootProject.name = "LeafLog"
include("app")
