package com.cibertec.novafit.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.novafit.databinding.ItemComidaBinding
import com.cibertec.novafit.databinding.ItemDiaDietaBinding
import com.cibertec.novafit.model.Dieta

class DietaAdapter : ListAdapter<Dieta.Dia, DietaAdapter.DiaViewHolder>(DiaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaViewHolder {
        val binding = ItemDiaDietaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiaViewHolder(private val binding: ItemDiaDietaBinding) : RecyclerView.ViewHolder(binding.root) {
        private val comidasAdapter = ComidasAdapter()

        init {
            binding.rvComidas.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvComidas.adapter = comidasAdapter
        }

        fun bind(dia: Dieta.Dia) {
            binding.txtDia.text = dia.nombre
            comidasAdapter.submitList(dia.comidas)
        }
    }
}

class ComidasAdapter : ListAdapter<Dieta.Comida, ComidasAdapter.ComidaViewHolder>(ComidaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComidaViewHolder {
        val binding = ItemComidaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ComidaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComidaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ComidaViewHolder(private val binding: ItemComidaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comida: Dieta.Comida) {
            binding.txtNombreComida.text = comida.nombre
            binding.txtHora.text = comida.hora
            binding.txtCalorias.text = "${comida.macros.calorias} kcal"
            binding.txtProteinas.text = "${comida.macros.proteinas}g P"
            binding.txtGrasas.text = "${comida.macros.grasas}g G"
            binding.txtCarbs.text = "${comida.macros.carbohidratos}g C"

            val alimentos = comida.alimentos.joinToString("\n") { "${it.nombre}: ${it.porcion}" }
            binding.txtAlimentos.text = alimentos
        }
    }
}

class DiaDiffCallback : DiffUtil.ItemCallback<Dieta.Dia>() {
    override fun areItemsTheSame(oldItem: Dieta.Dia, newItem: Dieta.Dia): Boolean = oldItem.nombre == newItem.nombre
    override fun areContentsTheSame(oldItem: Dieta.Dia, newItem: Dieta.Dia): Boolean = oldItem == newItem
}

class ComidaDiffCallback : DiffUtil.ItemCallback<Dieta.Comida>() {
    override fun areItemsTheSame(oldItem: Dieta.Comida, newItem: Dieta.Comida): Boolean = oldItem.nombre == newItem.nombre
    override fun areContentsTheSame(oldItem: Dieta.Comida, newItem: Dieta.Comida): Boolean = oldItem == newItem
}