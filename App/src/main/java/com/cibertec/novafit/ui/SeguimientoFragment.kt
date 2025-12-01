package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cibertec.novafit.data.HistorialRepository
import com.cibertec.novafit.databinding.FragmentSeguimientoBinding
import java.text.SimpleDateFormat
import java.util.*

class SeguimientoFragment : Fragment() {

    private var _binding: FragmentSeguimientoBinding? = null
    private val binding get() = _binding!!
    private lateinit var historialRepo: HistorialRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeguimientoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historialRepo = HistorialRepository(requireContext())
        val email = (requireActivity() as MainActivity).emailActual

        cargarEstadisticas(email)
    }

    private fun cargarEstadisticas(email: String) {

        val racha = historialRepo.obtenerRachaActual(email)
        binding.txtRacha.text = "$racha días"
        binding.txtRachaDescripcion.text = if (racha > 0) {
            "¡Sigue así! 💪"
        } else {
            "Empieza tu racha hoy"
        }


        val diasMes = historialRepo.obtenerDiasEntrenadosEsteMes(email)
        val diasTotalesMes = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val porcentaje = (diasMes.toFloat() / diasTotalesMes * 100).toInt()

        binding.txtDiasMes.text = "$diasMes / $diasTotalesMes días"
        binding.progressMes.progress = porcentaje
        binding.txtPorcentajeMes.text = "$porcentaje%"


        val records = historialRepo.obtenerRecordsPorEjercicio(email)
        if (records.isEmpty()) {
            binding.txtRecords.text = "Aún no tienes récords.\n¡Empieza a entrenar!"
        } else {
            val recordsText = records.entries.take(5).joinToString("\n") { (ejercicio, data) ->
                val (peso, reps) = data
                "• $ejercicio: ${peso}kg x ${reps} reps"
            }
            binding.txtRecords.text = recordsText
        }


        val historial = historialRepo.obtenerHistorialPorEmail(email, 5)
        if (historial.isEmpty()) {
            binding.txtUltimosEntrenamientos.text = "No hay entrenamientos registrados"
        } else {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val historialText = historial.joinToString("\n\n") { h ->
                val fecha = dateFormat.format(Date(h.fecha))
                val reps = h.repeticiones.joinToString(", ")
                "📅 $fecha\n" +
                        "${h.diaRutina}\n" +
                        "💪 ${h.ejercicioNombre}\n" +
                        "⚡ ${h.pesoUsado}kg x [${reps}] reps (${h.seriesCompletadas} series)"
            }
            binding.txtUltimosEntrenamientos.text = historialText
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}