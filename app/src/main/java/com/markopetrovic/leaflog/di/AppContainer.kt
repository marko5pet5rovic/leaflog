package com.markopetrovic.leaflog.di

import com.markopetrovic.leaflog.BuildConfig
import com.cloudinary.Cloudinary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.markopetrovic.leaflog.data.repository.*
import com.markopetrovic.leaflog.services.auth.AuthViewModel

object AppContainer {

    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val authInstance: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val profileRepository: ProfileRepository by lazy {
        FirestoreProfileRepository(firestoreInstance)
    }

    private val config: Map<String, String> = HashMap<String, String>().apply {
        put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME)
        put("api_key", BuildConfig.CLOUDINARY_API_KEY)
        put("api_secret", BuildConfig.CLOUDINARY_API_SECRET)
    }

    private val cloudinary: Cloudinary = Cloudinary(config)
    val storageRepository: StorageRepository = StorageRepository(cloudinary)

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(authInstance, profileRepository)
    }

    val locationRepository: LocationRepository by lazy {
        FirestoreLocationRepository(firestoreInstance, authInstance)
    }

    val authViewModel: AuthViewModel by lazy {
        AuthViewModel()
    }
}