package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.webkit.ConsoleMessage
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
        getAllReposByUserName()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        btnConfirmar = findViewById(R.id.btn_confirmar)
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener { saveUserLocal() }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
       val digitado = nomeUsuario.text.toString()

        if(digitado.isEmpty()) return

        var pref = getPreferences(Context.MODE_PRIVATE)

        with(pref.edit()){
            putString(getString(R.string.sharedpred_nome_usuario), digitado)
            apply()
        }

        getAllReposByUserName()
    }

    private fun showUserName() {
        var pref = getPreferences(Context.MODE_PRIVATE) ?: return

        var nomeAnterior = pref.getString(getString(R.string.sharedpred_nome_usuario), "") ?: ""

        if(nomeAnterior.isEmpty()) return

        nomeUsuario.setText(nomeAnterior)
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    private fun checkForInternet(): Boolean{
        val conManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val network = conManager.activeNetwork ?: return false

            val activeNetwork = conManager.getNetworkCapabilities(network) ?: return false

            return when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false

            }
        }else{
            @Suppress("DEPRECATION")
            val info = conManager.activeNetworkInfo?:return false
            @Suppress("DEPRECATION")
            return info.isConnected
        }

    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        var pref = getPreferences(Context.MODE_PRIVATE) ?: return

        var nomeUsuario = pref.getString(getString(R.string.sharedpred_nome_usuario), "") ?: ""

        if(nomeUsuario.isEmpty()) return

        if(!checkForInternet()) return

        githubApi.getAllRepositoriesByUser(nomeUsuario).enqueue(object: Callback<List<Repository>>{
                override fun onResponse(
                    call: Call<List<Repository>>,
                    response: Response<List<Repository>>
                ) {

                    if(response.isSuccessful){
                        response.body()?.let{
                            setupAdapter(it)
                        }
                    }

                }

                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    return
                }
            })
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        val ad = RepositoryAdapter(list)

        ad.btnShareListener = {shareRepositoryLink(it.htmlUrl)}
        ad.carItemListener = {openBrowser(it.htmlUrl)}

        listaRepositories.adapter = ad
    }


    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}