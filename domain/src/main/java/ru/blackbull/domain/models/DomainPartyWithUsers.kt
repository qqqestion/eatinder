package ru.blackbull.domain.models

data class DomainPartyWithUser(
    var id: String? = null ,
    val placeId: String? = null ,
    var isCurrentUserInParty: Boolean = false ,
    var time: Long? = null ,
    val users: MutableList<DomainUser> = mutableListOf()
)
