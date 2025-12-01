package com.cibertec.novafit.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.cibertec.novafit.R
import com.cibertec.novafit.data.EjercicioRepository
import com.cibertec.novafit.data.HistorialRepository
import com.cibertec.novafit.data.RutinaRepository
import com.cibertec.novafit.databinding.FragmentEjecutarEjercicioBinding
import com.cibertec.novafit.model.HistorialEntrenamiento
import com.cibertec.novafit.model.RegistroSerie
import com.cibertec.novafit.model.Rutina
import com.google.android.material.textfield.TextInputEditText
import com.bumptech.glide.Glide

class EjecutarEjercicioFragment : Fragment() {

    private var _binding: FragmentEjecutarEjercicioBinding? = null
    private val binding get() = _binding!!

    private var diaIndex: Int = 0
    private var ejercicioActual: Int = 0
    private var serieActual: Int = 1
    private lateinit var ejercicios: List<Rutina.Ejercicio>
    private lateinit var diaRutina: String
    private var timer: CountDownTimer? = null
    private var enDescanso = false
    private val registrosSeries = mutableListOf<RegistroSerie>()
    private var tiempoInicio: Long = 0


    private lateinit var ejercicioRepo: EjercicioRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEjecutarEjercicioBinding.inflate(inflater, container, false)
        ejercicioRepo = EjercicioRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diaIndex = arguments?.getInt("diaIndex", 0) ?: 0
        tiempoInicio = System.currentTimeMillis()

        cargarEjercicios()
        setupBotones()
        mostrarEjercicio()
    }

    private fun cargarEjercicios() {
        val email = (requireActivity() as MainActivity).emailActual
        val rutina = RutinaRepository(requireContext()).obtenerRutina(email)

        if (rutina != null) {
            val dia = rutina.planSemanal[diaIndex]
            diaRutina = dia.nombre
            ejercicios = dia.ejercicios
        }
    }

    private fun setupBotones() {
        binding.btnSiguiente.setOnClickListener {
            if (enDescanso) {
                timer?.cancel()
                siguienteSerie()
            } else {
                mostrarOpcionesRegistro()
            }
        }

        binding.btnSaltarEjercicio.setOnClickListener {
            mostrarDialogoSaltarEjercicio()
        }

        binding.btnCancelarRutina.setOnClickListener {
            mostrarDialogoCancelarRutina()
        }

        binding.btnAnterior.setOnClickListener {
            if (ejercicioActual > 0) {
                timer?.cancel()
                ejercicioActual--
                serieActual = 1
                registrosSeries.clear()
                enDescanso = false
                mostrarEjercicio()
            }
        }

        binding.btnSaltarDescanso.setOnClickListener {
            timer?.cancel()
            siguienteSerie()
        }
    }

    private fun mostrarEjercicio() {
        val ej = ejercicios[ejercicioActual]

        binding.txtProgreso.text = "Ejercicio ${ejercicioActual + 1} / ${ejercicios.size}"
        binding.txtNombre.text = ej.nombre
        binding.txtSeries.text = "Serie $serieActual de ${ej.series}"
        binding.txtReps.text = ej.repeticiones
        binding.txtDescanso.text = "Descanso: ${ej.descanso}"

        if (ej.notas != null) {
            binding.txtNotas.text = "💡 ${ej.notas}"
            binding.cardNotas.visibility = View.VISIBLE
        } else {
            binding.cardNotas.visibility = View.GONE
        }


        cargarImagenEjercicio(ej.nombre)

        binding.llTemporizador.visibility = View.GONE
        binding.btnSaltarDescanso.visibility = View.GONE
        binding.btnSiguiente.text = "Completar Serie $serieActual"
        binding.btnAnterior.visibility = if (ejercicioActual > 0) View.VISIBLE else View.GONE
        binding.btnSaltarEjercicio.visibility = View.VISIBLE
        binding.btnCancelarRutina.visibility = View.VISIBLE
    }


    private fun cargarImagenEjercicio(nombreEjercicio: String) {
        try {
            val ejercicios = ejercicioRepo.obtenerTodos()
            val ejercicio = ejercicios.find { it.nombre == nombreEjercicio }

            if (ejercicio != null) {
                if (ejercicio.esUrl()) {

                    Glide.with(this)
                        .load(ejercicio.obtenerImagenUrl())
                        .placeholder(obtenerPlaceholderAleatorio())  // ✅ PLACEHOLDER ALEATORIO
                        .error(obtenerPlaceholderAleatorio())        // ✅ ERROR ALEATORIO
                        .into(binding.imgEjercicio)

                    Log.d("EjecutarEjercicio", "🖼️ Cargando desde URL: ${ejercicio.obtenerImagenUrl()}")

                } else {

                    val imageResId = ejercicio.obtenerImagenResourceId(requireContext())


                    if (imageResId == android.R.drawable.ic_menu_help) {
                        binding.imgEjercicio.setImageResource(obtenerPlaceholderAleatorio())
                        Log.d("EjecutarEjercicio", "🖼️ Usando placeholder aleatorio")
                    } else {
                        binding.imgEjercicio.setImageResource(imageResId)
                        Log.d("EjecutarEjercicio", "🖼️ Cargando drawable local")
                    }
                }
            } else {

                binding.imgEjercicio.setImageResource(obtenerPlaceholderAleatorio())
                Log.w("EjecutarEjercicio", "⚠️ Ejercicio no encontrado: $nombreEjercicio")
            }
        } catch (e: Exception) {
            binding.imgEjercicio.setImageResource(obtenerPlaceholderAleatorio())
            Log.e("EjecutarEjercicio", "❌ Error cargando imagen: ${e.message}")
        }
    }


    private fun obtenerPlaceholderAleatorio(): Int {
        val placeholders = listOf(
            R.drawable.error,
            R.drawable.error2,
            R.drawable.error3
        )
        return placeholders.random()
    }

    private fun mostrarOpcionesRegistro() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Cómo fue tu serie?")
            .setMessage("Puedes registrar tu progreso o continuar sin registrar")
            .setPositiveButton("📝 Registrar datos") { _, _ ->
                mostrarDialogoRegistro()
            }
            .setNeutralButton("⏭️ Continuar sin registrar") { _, _ ->
                val ej = ejercicios[ejercicioActual]
                if (serieActual >= ej.series) {
                    siguienteEjercicio()
                } else {
                    iniciarDescanso()
                }
            }
            .setNegativeButton("❌ Cancelar", null)
            .show()
    }

    private fun mostrarDialogoRegistro() {
        val ej = ejercicios[ejercicioActual]
        val dialogView = layoutInflater.inflate(R.layout.dialog_registrar_serie, null)

        val etPeso = dialogView.findViewById<TextInputEditText>(R.id.etPeso)
        val etReps = dialogView.findViewById<TextInputEditText>(R.id.etReps)

        if (registrosSeries.isNotEmpty()) {
            val ultimoRegistro = registrosSeries.last()
            etPeso.setText(ultimoRegistro.peso.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle("📝 Serie $serieActual de ${ej.series}")
            .setMessage("Registra tu desempeño:")
            .setView(dialogView)
            .setPositiveButton("✅ Guardar") { _, _ ->
                val peso = etPeso.text.toString().toDoubleOrNull()
                val reps = etReps.text.toString().toIntOrNull()

                if (peso == null || peso <= 0) {
                    Toast.makeText(requireContext(), "⚠️ Ingresa un peso válido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (reps == null || reps <= 0) {
                    Toast.makeText(requireContext(), "⚠️ Ingresa las repeticiones", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                registrosSeries.add(RegistroSerie(serieActual, peso, reps))
                Toast.makeText(requireContext(), "✅ Serie $serieActual registrada: ${peso}kg x ${reps} reps", Toast.LENGTH_SHORT).show()

                if (serieActual >= ej.series) {
                    guardarEjercicioCompleto()
                    siguienteEjercicio()
                } else {
                    iniciarDescanso()
                }
            }
            .setNegativeButton("❌ Cancelar", null)
            .setCancelable(false)
            .show()
    }

    private fun mostrarDialogoSaltarEjercicio() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ ¿Saltar ejercicio?")
            .setMessage("Se guardarán las series que hayas registrado hasta ahora.\n\n¿Estás seguro?")
            .setPositiveButton("✅ Sí, saltar") { _, _ ->
                if (registrosSeries.isNotEmpty()) {
                    guardarEjercicioCompleto()
                    Toast.makeText(requireContext(), "✅ Progreso guardado", Toast.LENGTH_SHORT).show()
                }
                registrosSeries.clear()
                siguienteEjercicio()
            }
            .setNegativeButton("❌ Cancelar", null)
            .show()
    }

    private fun mostrarDialogoCancelarRutina() {
        AlertDialog.Builder(requireContext())
            .setTitle("🛑 ¿Cancelar rutina?")
            .setMessage("Tu progreso hasta ahora quedará guardado.\n\n¿Deseas terminar la sesión?")
            .setPositiveButton("✅ Sí, terminar") { _, _ ->
                if (registrosSeries.isNotEmpty()) {
                    guardarEjercicioCompleto()
                }
                Toast.makeText(requireContext(), "✅ Progreso guardado. ¡Buen trabajo!", Toast.LENGTH_LONG).show()
                (requireActivity() as MainActivity).openFragment(ResumenEntrenamientoFragment())
            }
            .setNegativeButton("❌ Continuar entrenando", null)
            .show()
    }

    private fun iniciarDescanso() {
        enDescanso = true
        val ej = ejercicios[ejercicioActual]
        val segundos = extraerSegundos(ej.descanso)

        binding.llTemporizador.visibility = View.VISIBLE
        binding.btnSaltarDescanso.visibility = View.VISIBLE
        binding.btnSiguiente.text = "Siguiente Serie"
        binding.btnSaltarEjercicio.visibility = View.GONE
        binding.btnCancelarRutina.visibility = View.GONE

        timer = object : CountDownTimer(segundos * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segsRestantes = millisUntilFinished / 1000
                binding.txtTemporizador.text = "$segsRestantes seg"
                binding.progressDescanso.progress = ((segsRestantes.toFloat() / segundos) * 100).toInt()
            }

            override fun onFinish() {
                binding.txtTemporizador.text = "¡Listo!"
                siguienteSerie()
            }
        }.start()
    }

    private fun siguienteSerie() {
        serieActual++
        enDescanso = false
        mostrarEjercicio()
    }

    private fun siguienteEjercicio() {
        ejercicioActual++
        serieActual = 1
        registrosSeries.clear()
        enDescanso = false

        if (ejercicioActual < ejercicios.size) {
            mostrarEjercicio()
        } else {
            Toast.makeText(requireContext(), "🎉 ¡Entrenamiento completado!", Toast.LENGTH_LONG).show()
            (requireActivity() as MainActivity).openFragment(ResumenEntrenamientoFragment())
        }
    }

    private fun guardarEjercicioCompleto() {
        if (registrosSeries.isEmpty()) return

        val ej = ejercicios[ejercicioActual]
        val email = (requireActivity() as MainActivity).emailActual
        val duracion = ((System.currentTimeMillis() - tiempoInicio) / 1000).toInt()

        val historial = HistorialEntrenamiento(
            email = email,
            fecha = System.currentTimeMillis(),
            diaRutina = diaRutina,
            ejercicioNombre = ej.nombre,
            seriesPlanificadas = ej.series,
            seriesCompletadas = registrosSeries.size,
            pesoUsado = registrosSeries.firstOrNull()?.peso ?: 0.0,
            repeticiones = registrosSeries.map { it.repeticiones },
            duracionSegundos = duracion
        )

        HistorialRepository(requireContext()).guardarEntrenamiento(historial)
        tiempoInicio = System.currentTimeMillis()
    }

    private fun extraerSegundos(descanso: String): Long {
        return try {
            descanso.filter { it.isDigit() }.toLong()
        } catch (e: Exception) {
            60L
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}