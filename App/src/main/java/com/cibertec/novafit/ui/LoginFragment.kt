package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.novafit.data.PerfilRepository
import com.cibertec.novafit.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnIniciarSesion.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Completa email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            binding.btnIniciarSesion.isEnabled = false
            binding.btnIniciarSesion.text = "Verificando..."

            val repo = PerfilRepository(requireContext())


            repo.validarLogin(email, password) { exitoso, mensaje ->

                requireActivity().runOnUiThread {

                    binding.btnIniciarSesion.isEnabled = true
                    binding.btnIniciarSesion.text = "Iniciar Sesión"

                    if (exitoso) {

                        (requireActivity() as MainActivity).emailActual = email
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                        (requireActivity() as MainActivity).mostrarMenuPrincipal()
                    } else {

                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.btnRegistrarse.setOnClickListener {
            (requireActivity() as MainActivity).openFragment(RegistroFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}