package ru.blackbull.eatogether.repositories

import android.location.Location
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.blackbull.eatogether.api.BaseFirebaseApi
import ru.blackbull.eatogether.models.firebase.Party
import ru.blackbull.eatogether.models.firebase.User
import ru.blackbull.eatogether.other.Resource
import ru.blackbull.eatogether.other.safeCall
import javax.inject.Inject


class FirebaseRepository @Inject constructor(
    private val firebaseApi: BaseFirebaseApi
) {

    private val auth = FirebaseAuth.getInstance()
    private val usersRef = Firebase.firestore.collection("users")
    private val partiesRef = Firebase.firestore.collection("parties")
    private val notificationsRef = Firebase.firestore.collection("notifications")
    val matchesRef = Firebase.firestore.collection("matches")

    /**
     * Updates last location user's field with given location
     *
     * @param location
     */
    suspend fun updateUserLocation(location: Location): Unit = withContext(Dispatchers.IO) {
        firebaseApi.updateUserLocation(location)
    }

    /**
     * Calls FirebaseApi.searchPartyByPlace method and returns Success. If error occurs return Error
     *
     * @param placeId place id in Google Places API
     * @return Resource
     */
    suspend fun searchPartyByPlace(
        placeId: String
    ): Resource<List<Party>> = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(firebaseApi.searchPartyByPlace(placeId))
        }
    }

    /**
     * Creates party in collection and returns Success. If error occurs return Error
     *
     * @param party created party
     * @return Resource
     */
    suspend fun addParty(party: Party): Resource<Unit> = withContext(Dispatchers.IO) {
        safeCall {
            firebaseApi.addParty(party)
            Resource.Success()
        }
    }

    suspend fun updateParty(party: Party) = withContext(Dispatchers.IO) {
        firebaseApi.updateParty(party)
    }

    /**
     * Gets party in which current user is member
     *
     * @return
     */
    suspend fun getPartiesByCurrentUser(): Resource<List<Party>> = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(firebaseApi.getPartiesByCurrentUser())
        }
    }

    /**
     * Gets current user by his id
     *
     * @return
     */
    suspend fun getCurrentUser(): Resource<User> = getUser(getCurrentUserId())

    /**
     * Gets user with given id
     *
     * @param uid
     * @return
     */
    suspend fun getUser(uid: String): Resource<User> = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(firebaseApi.getUser(uid))
        }
    }

    fun getCurrentUserId(): String = firebaseApi.getCurrentUserId()

    /**
     * Signs out
     *
     */
    fun signOut() {
        firebaseApi.signOut()
    }

    suspend fun updateUser(user: User , photoUri: Uri) {
        val firebaseUser = auth.currentUser ?: return
        if (firebaseUser.email != user.email) {
            firebaseUser.updateEmail(user.email!!)
            auth.updateCurrentUser(firebaseUser)
        }

        // Чтобы не фотография из firebase не загружалась повторно в firebase
        // TODO: добавить обновление фотографии
        if (photoUri.host != "firebasestorage.googleapis.com") {
            val res = FirebaseStorage.getInstance().reference.child(firebaseUser.uid).putFile(
                photoUri
            ).await()
            val imageUri = res.metadata?.reference?.downloadUrl?.await()
            user.imageUri = imageUri.toString()
        }

        usersRef.document(auth.uid!!).set(user).await()
    }

    fun isAuthenticated(): Boolean {
        return firebaseApi.isAuthenticated()
    }

    suspend fun signIn(email: String , password: String) = withContext(Dispatchers.IO) {
        safeCall {
            firebaseApi.signIn(email , password)
            Resource.Success(Unit)
        }
    }

    suspend fun signUpWithEmailAndPassword(
        user: User ,
        password: String
    ) = withContext(Dispatchers.IO) {
        safeCall {
            firebaseApi.signUpWithEmailAndPassword(user , password)
            Resource.Success<Unit>()
        }
    }

    suspend fun getNearbyUsers(): Resource<MutableList<User>> = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(firebaseApi.getNearbyUsers().toMutableList())
        }
    }

    suspend fun dislikeUser(user: User) = withContext(Dispatchers.IO) {
        safeCall {
            firebaseApi.dislikeUser(user)
            Resource.Success<Unit>()
        }
    }

    suspend fun likeUser(user: User) = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(firebaseApi.likeUser(user))
        }
    }

    suspend fun getPartyById(partyId: String): Resource<Party> = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(firebaseApi.getPartyById(partyId))
        }
    }

    suspend fun getPartyParticipants(
        party: Party
    ): Resource<List<User>> = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(firebaseApi.getPartyParticipants(party))
        }
    }

    suspend fun addCurrentUserToParty(party: Party) = withContext(Dispatchers.IO) {
        firebaseApi.addCurrentUserToParty(party)
    }

    suspend fun sendLikeNotification(user: User) {
//        val notification = Notification(userId = user.id , type = "like")
//        notificationsRef.add(notification).await()
    }
}
