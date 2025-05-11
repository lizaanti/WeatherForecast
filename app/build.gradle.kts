

    plugins {
        id("com.android.application")
        id("com.google.gms.google-services")
       }


    android {
        namespace = "com.example.weatherforecast"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.example.weatherforecast"
            minSdk = 26
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            packaging {
                resources {
                    excludes += "META-INF/DEPENDENCIES"
                }
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
        }

        dependencies {

            implementation(libs.yandex.maps)
            implementation (libs.volley)
            implementation (libs.picasso)
            implementation (libs.androidx.core)
            implementation (libs.maps.mobile.v461full)
            implementation(libs.maps.mobile)
            implementation(libs.guava)
            implementation(libs.central.publishing.maven.plugin)
            implementation(platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.appcompat)
            implementation(libs.material)
            implementation(libs.activity)
            implementation(libs.constraintlayout)
            implementation(libs.androidx.room.common)
            implementation(libs.support.annotations)
            implementation(libs.play.services.location)
            implementation(libs.androidx.annotation)

            testImplementation(libs.junit)
            androidTestImplementation(libs.ext.junit)
            androidTestImplementation(libs.espresso.core)
            implementation(libs.gson)
            implementation(libs.play.services.location.license)

            implementation(libs.androidx.runtime.livedata)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.room.runtime)
            annotationProcessor(libs.androidx.room.room.compiler)
            implementation(libs.firebase.messaging)
            runtimeOnly(libs.androidx.core)
        }
    }
    dependencies {
        implementation(libs.play.services.maps)
    }

