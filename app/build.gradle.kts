plugins {id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
}
android {
    namespace = "com.example.zyntra"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.zyntra"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // ✅ Add this block to resolve the duplicate file conflict
    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
    }
}


dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
    implementation ("com.cloudinary:cloudinary-android:2.3.1")
    implementation ("com.android.volley:volley:1.2.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.messaging)
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-messaging:24.1.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.18.0")
    implementation ("com.google.firebase:firebase-auth:22.3.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}