package ru.blackbull.eatogether.ui.main.map

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_recycler_place.*
import ru.blackbull.eatogether.R
import ru.blackbull.eatogether.adapters.PlaceAdapter

@AndroidEntryPoint
class RecycleRestaurantsFragment : Fragment(R.layout.fragment_recycler_place) {

    val args: RecycleRestaurantsFragmentArgs by navArgs()
    private val viewModel: MapViewModel by viewModels()

    private lateinit var placeAdapter: PlaceAdapter

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        subscribeToObservers()
        setupRecyclerView()
        val location = args.location
        location?.let {
            // TODO: сделать поиск ближайших мест
//            viewModel.getNearbyPlaces(it.latitude , it.longitude)
        }
        placeAdapter.setOnItemClickListener { location ->
            val bundle = Bundle().apply {
                // TODO: возможно будут проблемы с id места
                putString("placeId" , location.id)
            }
//            findNavController().navigate(
//                R.id.action_recycleRestaurantsFragment_to_placeDetailFragment ,
//                bundle
//            )
        }
    }

    private fun subscribeToObservers() {
//        viewModel.nearbyPlaces.observe(viewLifecycleOwner , EventObserver(
//            onError = {
//                snackbar(it)
//            } ,
//            onLoading = {
//
//            }
//        ) { places ->
//            placeAdapter.places = places
//        })
    }

    private fun setupRecyclerView() {
        placeAdapter = PlaceAdapter()
        rvNearbyPlaces.apply {
            adapter = placeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}