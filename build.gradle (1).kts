plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("com.google.gms.google-services")
}
android {
 namespace = "com.shamshadrice.business"
 compileSdk = 35
 defaultConfig {
  applicationId = "com.shamshadrice.business"
  minSdk = 24
  targetSdk = 35
  versionCode = 3
  versionName = "3.0"
 }
}
dependencies {
 implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
 implementation("com.google.firebase:firebase-auth")
 implementation("com.google.firebase:firebase-firestore")
}
