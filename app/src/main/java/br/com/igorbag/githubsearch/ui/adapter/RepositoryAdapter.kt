package br.com.igorbag.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val repositories: List<Repository>) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    var carItemListener: (Repository) -> Unit = {}
    var btnShareListener: (Repository) -> Unit = {}

    // Cria uma nova view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    // Pega o conteudo da view e troca pela informacao de item de uma lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nome.text = repositories[position].name

        holder.nome.setOnClickListener{
            carItemListener(repositories[position])
        }

        holder.btnShare.setOnClickListener{
            btnShareListener(repositories[position])
        }
    }

    override fun getItemCount(): Int {
        return repositories.count()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nome: TextView
        var btnShare: ImageView

        init {
            view.apply {
                nome = findViewById(R.id.tv_preco)
                btnShare = findViewById(R.id.iv_favorite)
            }
        }
    }
}


