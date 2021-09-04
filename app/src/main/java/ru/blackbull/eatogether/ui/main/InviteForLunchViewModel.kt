package ru.blackbull.eatogether.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.blackbull.data.models.firebase.User
import ru.blackbull.eatogether.other.Event
import ru.blackbull.domain.Resource
import ru.blackbull.domain.FirebaseDataSource
import ru.blackbull.domain.models.DomainUser
import javax.inject.Inject

@HiltViewModel
class InviteForLunchViewModel @Inject constructor(
    private val firebaseRepository: FirebaseDataSource
) : ViewModel() {

    private val _friendList = MutableLiveData<Event<Resource<List<DomainUser>>>>()
    val friendList: LiveData<Event<Resource<List<DomainUser>>>> = _friendList

    fun getFriendList(partyId: String) = viewModelScope.launch {
        _friendList.postValue(Event(Resource.Loading()))
        val response = firebaseRepository.getFriendListForParty(partyId).toResource()
        _friendList.postValue(Event(response))
    }

    private val _invitationStatus = MutableLiveData<Event<Resource<User>>>()
    val invitationStatus: LiveData<Event<Resource<User>>> = _invitationStatus

    fun sendInvitation(partyId: String , user: User) = viewModelScope.launch {
        _invitationStatus.postValue(Event(Resource.Loading()))
        val response =
            firebaseRepository.sendLunchInvitation(partyId , user.toDomainUser()).toResource()
        if (response is Resource.Success) {
            _invitationStatus.postValue(Event(Resource.Success(user)))
        } else {
            _invitationStatus.postValue(Event(Resource.Error(response.error , response.msg)))
        }
    }
}