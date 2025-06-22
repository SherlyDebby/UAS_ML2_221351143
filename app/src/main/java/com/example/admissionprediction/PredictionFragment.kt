package com.example.admissionprediction

import android.content.res.AssetFileDescriptor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.admissionprediction.databinding.FragmentPredictionBinding
import com.google.android.material.slider.Slider
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.Locale

class PredictionFragment : Fragment() {

    private var _binding: FragmentPredictionBinding? = null
    private val binding get() = _binding!!

    private lateinit var tflite: Interpreter
    private val tfliteModelName = "graduate_admission.tflite"

    // Nilai StandardScaler dari Python
    private val scalerMean = floatArrayOf(316.7275f, 107.335f, 3.1125f, 3.375f, 3.48f, 8.58075f, 0.555f)
    private val scalerScale = floatArrayOf(10.95551203f, 6.08381254f, 1.14448405f, 0.98583721f, 0.92038036f, 0.60057592f, 0.49696579f)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load model TFLite
        try {
            tflite = Interpreter(loadModelFile())
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error loading model: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        // Setup UI Listeners
        setupListeners()
    }

    private fun setupListeners() {
        // Listener untuk tombol kembali
        binding.btnToolbarBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Listener untuk tombol prediksi
        binding.btnPredict.setOnClickListener {
            val result = runPrediction()
            val resultPercentage = result * 100
            binding.tvResult.text = String.format(Locale.US, "%.2f%%", resultPercentage)
        }

        // Listener untuk setiap slider agar nilai di TextView ikut berubah
        binding.sliderGre.addOnChangeListener { _, value, _ ->
            updateSliderValueText(binding.tvGreValue, "(%.0f)".format(value))
        }
        binding.sliderToefl.addOnChangeListener { _, value, _ ->
            updateSliderValueText(binding.tvToeflValue, "(%.0f)".format(value))
        }
        binding.sliderRating.addOnChangeListener { _, value, _ ->
            updateSliderValueText(binding.tvRatingValue, "(%.0f)".format(value))
        }
        binding.sliderSop.addOnChangeListener { _, value, _ ->
            updateSliderValueText(binding.tvSopValue, "(%.1f)".format(value))
        }
        binding.sliderLor.addOnChangeListener { _, value, _ ->
            updateSliderValueText(binding.tvLorValue, "(%.1f)".format(value))
        }
        binding.sliderCgpa.addOnChangeListener { _, value, _ ->
            updateSliderValueText(binding.tvCgpaValue, "(%.2f)".format(value))
        }
    }

    private fun updateSliderValueText(textView: TextView, text: String) {
        textView.text = text
    }

    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor: AssetFileDescriptor = requireContext().assets.openFd(tfliteModelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun scaleFeatures(features: FloatArray): FloatArray {
        val scaledFeatures = FloatArray(features.size)
        for (i in features.indices) {
            // Rumus StandardScaler: (x - mean) / scale (std dev)
            scaledFeatures[i] = (features[i] - scalerMean[i]) / scalerScale[i]
        }
        return scaledFeatures
    }

    private fun runPrediction(): Float {
        // 1. Ambil input dari slider dan switch
        val inputs = floatArrayOf(
            binding.sliderGre.value,
            binding.sliderToefl.value,
            binding.sliderRating.value,
            binding.sliderSop.value,
            binding.sliderLor.value,
            binding.sliderCgpa.value,
            if (binding.switchResearch.isChecked) 1.0f else 0.0f
        )

        // 2. Lakukan penskalaan data
        val scaledInputs = scaleFeatures(inputs)

        // 3. Siapkan buffer input (1 baris, 7 fitur, 4 byte per float)
        val inputBuffer = ByteBuffer.allocateDirect(1 * 7 * 4).apply {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().put(scaledInputs)
        }

        // 4. Siapkan buffer output untuk regresi (1 output float)
        val outputBuffer = Array(1) { FloatArray(1) }

        // 5. Jalankan model
        tflite.run(inputBuffer, outputBuffer)

        // 6. Kembalikan hasil output (probabilitas)
        return outputBuffer[0][0]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::tflite.isInitialized) tflite.close()
        _binding = null
    }
}