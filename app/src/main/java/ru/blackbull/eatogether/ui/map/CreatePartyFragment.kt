package ru.blackbull.eatogether.ui.map

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_create_party.*
import ru.blackbull.eatogether.R
import ru.blackbull.eatogether.other.Status


class CreatePartyFragment : Fragment(R.layout.fragment_create_party) {

    private val createPartyViewModel: CreatePartyViewModel by viewModels()
    private val args: CreatePartyFragmentArgs by navArgs()

    private lateinit var placeId: String

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        subscribeToObservers()

        placeId = args.placeId
        tvCreatePartyPlaceName.text = args.placeName
        tvCreatePartyPlaceAddress.text = args.placeAddress

        btnCreatePartyConfirm.setOnClickListener {
            createPartyViewModel.createParty(
                title = etCreatePartyPlaceTitle.text.toString() ,
                description = etCreatePartyPlaceDescription.text.toString() ,
                date = etCreatePartyPickDate.text.toString() ,
                time = etCreatePartyPickTime.text.toString() ,
                placeId = placeId
            )
        }
        btnCreatePartyCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun subscribeToObservers() {
        createPartyViewModel.createPartyResult.observe(
            viewLifecycleOwner ,
            Observer { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        Snackbar.make(
                            requireActivity().flRootLayout ,
                            getString(R.string.success_party_created) ,
                            Snackbar.LENGTH_LONG
                        ).show()
                        btnCreatePartyConfirm.isEnabled = true
                        findNavController().popBackStack()
                    }
                    Status.ERROR -> {
                        val msg = result.msg ?: getString(R.string.errormessage_unknown_error)
                        Snackbar.make(
                            requireView(),
                            msg ,
                            Snackbar.LENGTH_LONG
                        ).show()
                        btnCreatePartyConfirm.isEnabled = true
                    }
                    Status.LOADING -> {
                        btnCreatePartyConfirm.isEnabled = false
                    }
                }
            })
    }
}