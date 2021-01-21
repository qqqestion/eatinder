package ru.blackbull.eatogether.repository

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import ru.blackbull.eatogether.api.NetworkModule
import ru.blackbull.eatogether.models.firebase.Party
import ru.blackbull.eatogether.models.firebase.User
import ru.blackbull.eatogether.other.Resource
import ru.blackbull.eatogether.state.RegistrationState
import java.lang.Exception


class FirebaseRepository {

    suspend fun searchPartyByPlace(placeId: String) =
        NetworkModule.firebaseApiService.searchPartyByPlace(placeId)

    suspend fun addParty(party: Party) =
        NetworkModule.firebaseApiService.addParty(party)

    suspend fun updateParty(party: Party) {
        NetworkModule.firebaseApiService.updateParty(party)
    }

    suspend fun getPartiesByCurrentUser() =
        NetworkModule.firebaseApiService.getPartiesByCurrentUser()


    suspend fun getCurrentUser(): User? {
        FirebaseAuth.getInstance().currentUser?.let {
            return@getCurrentUser NetworkModule.firebaseApiService.getUser(it.uid)
        }
        return null
    }

    suspend fun getUser(uid: String): User {
        return NetworkModule.firebaseApiService.getUser(uid)
    }

    fun getCurrentUserId(): String = FirebaseAuth.getInstance().currentUser!!.uid

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    suspend fun updateUser(user: User) {
        NetworkModule.firebaseApiService.updateUser(user)
    }

    fun isAuthenticated(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    suspend fun signIn(email: String , password: String): Resource<Boolean> {
        val firebaseUser: FirebaseUser?
        try {
            val result = FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email , password)
                .await()
            firebaseUser = result.user
        } catch (e: FirebaseException) {
            return Resource.error(e , null , null)
        }
        return Resource.success(firebaseUser != null)
    }

    suspend fun signUpWithEmailAndPassword(
        userInfo: User ,
        password: String
    ): Resource<Unit> {
        val firebaseUser: FirebaseUser?
        try {
            val result = FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(userInfo.email!! , password)
                .await()
            firebaseUser = result.user
        } catch (e: FirebaseException) {
            Log.d("RegistrationDebug" , "an error occurred" , e)
            return Resource.error(e , null , null)
        }
        NetworkModule.firebaseApiService.addUser(firebaseUser?.uid!! , userInfo)
        return Resource.success(null)
    }

    suspend fun getNearbyUsers(): MutableList<User> {
        return NetworkModule.firebaseApiService.getNearbyUsers()
    }

    suspend fun dislikeUser(user: User) {
        NetworkModule.firebaseApiService.dislikeUser(user)
    }

    suspend fun likeUser(user: User): Boolean {
        return NetworkModule.firebaseApiService.likeUser(user)
    }

    suspend fun getPartyById(id: String): Party? {
        return NetworkModule.firebaseApiService.getPartyById(id)
    }

    suspend fun getPartyParticipants(party: Party): MutableList<User> {
        return NetworkModule.firebaseApiService.getPartyParticipants(party)
    }

    suspend fun addCurrentUserToParty(party: Party) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        if (!party.users.contains(uid)) {
            party.users.add(uid)
            updateParty(party)
        }
    }

    suspend fun sendLikeNotification(user: User) {
        NetworkModule.firebaseApiService.sendLikeNotification(user)
    }
}
