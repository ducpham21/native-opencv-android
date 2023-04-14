package com.example.nativeopencvandroidtemplate.modal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nativeopencvandroidtemplate.databinding.FragmentGaussianBlurDialogBinding
import com.example.nativeopencvandroidtemplate.model.GaussianBlur
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BlurSettingDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGaussianBlurDialogBinding? = null

    private val binding get() = _binding!!

    var applyBlur: ((GaussianBlur) -> Unit) ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGaussianBlurDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBlurApply.setOnClickListener {
            val sigmaXString = binding.edtValueSigmaX.text.toString()
            val sigmaYString = binding.edtValueSigmaY.text.toString()
            if(sigmaXString.isNotEmpty() && sigmaYString.isNotEmpty()){
                val sigmaX = sigmaXString.toDouble()
                val sigmaY = sigmaYString.toDouble()
                applyBlur?.invoke(GaussianBlur(sigmaX, sigmaY))
            }
            dismiss()
        }
    }

    companion object {
        fun newInstance(): BlurSettingDialogFragment =
            BlurSettingDialogFragment().apply {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}