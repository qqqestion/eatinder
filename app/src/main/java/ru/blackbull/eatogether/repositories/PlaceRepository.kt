package ru.blackbull.eatogether.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yandex.mapkit.GeoObjectCollection
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.*
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import ru.blackbull.eatogether.models.PlaceDetail
import ru.blackbull.eatogether.other.Constants
import ru.blackbull.eatogether.other.Event
import ru.blackbull.eatogether.other.Resource
import timber.log.Timber
import javax.inject.Inject


class PlaceRepository @Inject constructor(
    private val searchManager: SearchManager ,
) {
//
//    suspend fun getPlaceDetail(
//        placeId: String
//    ): Resource<PlaceDetail> = withContext(Dispatchers.IO) {
//        val response = googlePlaceApiService.getPlaceDetail(placeId)
//        return@withContext if (response.status == "OK") {
//            Resource.Success(response.placeDetail)
//        } else {
//            Resource.Error(msg = response.errorMessage)
//        }
//    }
//
//    suspend fun getPlacesByName(
//        placeName: String
//    ): Resource<List<BasicLocation>> = withContext(Dispatchers.IO) {
//        val response = googlePlaceApiService.getPlacesByName(placeName)
//        return@withContext if (response.status == "OK") {
//            Resource.Success(response.placeList)
//        } else {
//            Resource.Error(msg = response.errorMessage)
//        }
//    }
//
//    suspend fun getNearbyPlaces(
//        location: String
//    ): Resource<List<BasicLocation>> = withContext(Dispatchers.IO) {
//        val response = googlePlaceApiService.getNearbyPlaces(location)
//        return@withContext if (response.status == "OK") {
//            Resource.Success(response.placeList)
//        } else {
//            Resource.Error(msg = response.errorMessage)
//        }
//    }

    private val _placeDetail: MutableLiveData<Event<Resource<PlaceDetail>>> = MutableLiveData()
    val placeDetail: LiveData<Event<Resource<PlaceDetail>>> = _placeDetail

    private val _searchPlaces: MutableLiveData<Event<Resource<List<GeoObjectCollection.Item>>>> =
        MutableLiveData()
    val searchPlaces: LiveData<Event<Resource<List<GeoObjectCollection.Item>>>> = _searchPlaces

    private var searchSession: Session? = null

    init {
        val point = Geometry.fromPoint(Point(59.95 , 30.32))
        searchSession = searchManager.submit(
            "" ,
            point ,
            SearchOptions() ,
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    Timber.d("Init: ${filters(response)}")
                }

                override fun onSearchError(error: Error) {
                    val errorMessage = when (error) {
                        is RemoteError -> "Remote error"
                        is NetworkError -> "Network error"
                        else -> "Unknown error"
                    }
                    Timber.d("Error during map sdk request: $errorMessage")
                }
            }
        )
    }

    fun search(query: String , geometry: Geometry) {
//        val enumFilter = BusinessFilter(
//            "type_cuisine" ,
//            "" ,
//            false ,
//            false ,
//            BusinessFilter.Values.fromEnums(
//                listOf(
//                    BusinessFilter.EnumValue(
//                        Feature.FeatureEnumValue(
//                            /* id= */ "italian_cuisine" ,
//                            /* name= */ "" ,
//                            /* imageUrlTemplate= */ ""
//                        ) ,
//                        true ,
//                        true
//                    )
//                )
//            )
//        )
        searchSession = searchManager.submit(
            query ,
            geometry ,
            SearchOptions().apply {
                snippets = Snippet.BUSINESS_IMAGES.value
            } ,
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    Timber.d("Search: ${filters(response)}")
                    _searchPlaces.postValue(Event(Resource.Success(
                        response.collection.children.filter { item ->
                            val name =
                                item.obj!!.metadataContainer.getItem(BusinessObjectMetadata::class.java).name
                            item.obj!!.metadataContainer.getItem(BusinessObjectMetadata::class.java).categories.forEach {
                                Timber.d("Place $name, category: name - ${it.name}, category class - ${it.categoryClass}, tags - ${it.tags}")
                            }
                            item.obj!!.metadataContainer.getItem(BusinessObjectMetadata::class.java).categories.find {
                                it.categoryClass in Constants.CATEGORIES
                            } != null
                        }
                    )))
                }

                override fun onSearchError(error: Error) {
                    val errorMessage = when (error) {
                        is RemoteError -> "Remote error"
                        is NetworkError -> "Network error"
                        else -> "Unknown error"
                    }
                    _searchPlaces.postValue(Event(Resource.Error(null , errorMessage)))
                }
            }
        )
    }

    private fun filters(response: Response): String? {
        fun enumValues(filter: BusinessFilter) = filter
            .values
            .enums
            ?.joinToString(prefix = " -> ") { e -> e.value.name }
            ?: ""

        return response
            .metadata
            .businessResultMetadata
            ?.businessFilters
            ?.joinToString(separator = "\n") { f -> "${f.id}${enumValues(f)}" }
    }

    private val baseUri = "ymapsbm1://org?oid=%s"

    fun getPlaceDetail(placeId: String) {
        _placeDetail.postValue(Event(Resource.Loading()))
        searchSession = searchManager.resolveURI(
            baseUri.format(placeId) ,
            SearchOptions().apply {
                snippets = Snippet.BUSINESS_RATING1X.value
            } ,
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    Timber.d("Place detail: ${filters(response)}")
                    for (searchResult in response.collection.children) {
                        val obj = searchResult.obj!!
                        val businessMetadata =
                            obj.metadataContainer.getItem(BusinessObjectMetadata::class.java)
                        val ratingMetadata =
                            obj.metadataContainer.getItem(BusinessRating1xObjectMetadata::class.java)
                        val id = businessMetadata.oid
                        val score = ratingMetadata?.score
                        val ratings = ratingMetadata?.ratings
                        val name = businessMetadata.name
                        val address = businessMetadata.address.formattedAddress
                        val phones = businessMetadata.phones.map { it.formattedNumber }
                        val workingHours = businessMetadata.workingHours?.state?.text
                        val categories = businessMetadata.categories.map { it.name }
                        val features = businessMetadata.features.map { it.id to it.value }
                        val links = businessMetadata.links
                        _placeDetail.postValue(
                            Event(
                                Resource.Success(
                                    PlaceDetail(
                                        id ,
                                        name ,
                                        address ,
                                        phones.firstOrNull() ,
                                        workingHours ,
                                        score ,
                                        ratings ,
                                        categories
                                    )
                                )
                            )
                        )
                    }
                }

                override fun onSearchError(error: Error) {
                    val errorMessage = when (error) {
                        is RemoteError -> "Remote error"
                        is NetworkError -> "Network error"
                        else -> "Unknown error"
                    }
                    Timber.d("Yandex search error: $errorMessage")
                    _placeDetail.postValue(Event(Resource.Error(null , errorMessage)))
                }
            }
        )
    }
}
