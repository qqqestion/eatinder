package ru.blackbull.eatogether.googleplacesapi

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Review(
    var author_name: String ,
    var author_url: String ,
    var language: String ,
    var profile_photo_url: String ,
    var rating: Int ,
    var relative_time_description: String ,
    var text: String ,
    var time: Int ,
) {

}
