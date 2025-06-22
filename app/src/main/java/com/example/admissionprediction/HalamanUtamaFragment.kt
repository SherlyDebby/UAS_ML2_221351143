package com.example.admissionprediction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.admissionprediction.R
import com.example.admissionprediction.databinding.FragmentHalamanUtamaBinding


class HalamanUtamaFragment : Fragment() {

    private var _binding: FragmentHalamanUtamaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHalamanUtamaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToolbarBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.cardMenuPredict.setOnClickListener {
            findNavController().navigate(R.id.action_halamanUtamaFragment_to_predictionFragment)
        }
        binding.cardMenuFeatures.setOnClickListener {
            findNavController().navigate(R.id.action_halamanUtamaFragment_to_featureInfoFragment)
        }
        binding.cardMenuModel.setOnClickListener {
            findNavController().navigate(R.id.action_halamanUtamaFragment_to_modelInfoFragment)
        }
        binding.cardMenuDataset.setOnClickListener {
            findNavController().navigate(R.id.action_halamanUtamaFragment_to_datasetInfoFragment)
        }
        binding.cardMenuAbout.setOnClickListener {
            findNavController().navigate(R.id.action_halamanUtamaFragment_to_aboutAppFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}