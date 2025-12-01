package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.novafit.data.PerfilRepository
import com.cibertec.novafit.databinding.FragmentCredencialesBinding
import com.cibertec.novafit.model.Perfil

class CredencialesFragment : Fragment() {

    private var _binding: FragmentCredencialesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCredencialesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnFinalizar.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Completa email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Email inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = arguments ?: return@setOnClickListener
            val perfil = Perfil(
                email = email,
                password = password,
                nombre = bundle.getString("nombre") ?: "",
                fechaNacimiento = bundle.getLong("fechaNacimiento", 0L),
                edad = bundle.getInt("edad", 0),
                genero = bundle.getString("genero") ?: "",
                peso = bundle.getDouble("peso", 0.0),
                altura = bundle.getDouble("altura", 0.0),
                circunferenciaCuello = bundle.getDouble("circunferenciaCuello"),
                circunferenciaCintura = bundle.getDouble("circunferenciaCintura"),
                circunferenciaCadera = bundle.getDouble("circunferenciaCadera"),
                nivelActividad = bundle.getString("nivelActividad"),
                objetivo = bundle.getString("objetivo") ?: "",
                tipoCuerpo = bundle.getString("tipoCuerpo"),
                nivelExperiencia = bundle.getString("nivelExperiencia")
            )


            if (perfil.nombre.isEmpty() || perfil.genero.isEmpty() || perfil.peso <= 0 || perfil.altura <= 0 || perfil.objetivo.isEmpty()) {
                Toast.makeText(requireContext(), "Datos incompletos, revisa el registro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val repo = PerfilRepository(requireContext())
            repo.guardarPerfil(perfil)
            (requireActivity() as MainActivity).emailActual = email

            Toast.makeText(requireContext(), "¡Perfil creado exitosamente!", Toast.LENGTH_SHORT).show()
            (requireActivity() as MainActivity).mostrarMenuPrincipal()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}