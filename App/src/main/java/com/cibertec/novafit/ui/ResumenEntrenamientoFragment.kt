package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cibertec.novafit.data.HistorialRepository
import com.cibertec.novafit.databinding.FragmentResumenEntrenamientoBinding
import java.text.SimpleDateFormat
import java.util.*

class ResumenEntrenamientoFragment : Fragment() {

    private var _binding: FragmentResumenEntrenamientoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResumenEntrenamientoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarEstadisticasSesion()

        binding.btnVolverMenu.setOnClickListener {
            (requireActivity() as MainActivity).openFragment(MenuFragment())
        }
    }

    private fun cargarEstadisticasSesion() {
        val email = (requireActivity() as MainActivity).emailActual
        val historialRepo = HistorialRepository(requireContext())


        val hace24Horas = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        val entrenamientosHoy = historialRepo.obtenerHistorialPorEmail(email, 100)
            .filter { it.fecha >= hace24Horas }

        if (entrenamientosHoy.isEmpty()) {

            binding.txtEjerciciosCompletados.text = "0"
            binding.txtGruposTrabajados.text = "Ninguno"
            binding.txtMensaje.text = "No se registraron ejercicios en esta sesión"
            return
        }


        val ejerciciosCompletados = entrenamientosHoy.count {
            val porcentaje = (it.seriesCompletadas.toFloat() / it.seriesPlanificadas) * 100
            porcentaje >= 50
        }


        val gruposTrabajados = entrenamientosHoy
            .map { it.diaRutina }
            .distinct()
            .joinToString(", ") { dia ->

                dia.substringAfter("-").trim()
            }


        binding.txtEjerciciosCompletados.text = ejerciciosCompletados.toString()
        binding.txtGruposTrabajados.text = gruposTrabajados.ifEmpty { "N/A" }


        val mensaje = when {
            ejerciciosCompletados == entrenamientosHoy.size ->
                "🔥 ¡Excelente trabajo! Completaste todos los ejercicios. Sigue así y alcanzarás tus objetivos. 💪"
            ejerciciosCompletados > 0 ->
                "💪 ¡Buen esfuerzo! Completaste $ejerciciosCompletados de ${entrenamientosHoy.size} ejercicios. ¡Cada paso cuenta! 🚀"
            else ->
                "⚠️ No completaste ningún ejercicio completo, pero registraste tu progreso. ¡Sigue intentándolo! 💪"
        }
        binding.txtMensaje.text = mensaje


        mostrarDetallesAdicionales(entrenamientosHoy)
    }

    private fun mostrarDetallesAdicionales(entrenamientos: List<com.cibertec.novafit.model.HistorialEntrenamiento>) {
        val detalles = StringBuilder()

        entrenamientos.forEachIndexed { index, h ->
            val completado = if ((h.seriesCompletadas.toFloat() / h.seriesPlanificadas) >= 0.5) "✅" else "⚠️"
            val reps = h.repeticiones.joinToString(", ")

            detalles.append("$completado ${h.ejercicioNombre}\n")
            detalles.append("   ${h.seriesCompletadas}/${h.seriesPlanificadas} series | ${h.pesoUsado}kg | [$reps] reps\n")

            if (index < entrenamientos.size - 1) {
                detalles.append("\n")
            }
        }


        val mensajeCompleto = binding.txtMensaje.text.toString() +
                "\n\n📋 Resumen detallado:\n" + detalles.toString()
        binding.txtMensaje.text = mensajeCompleto
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}