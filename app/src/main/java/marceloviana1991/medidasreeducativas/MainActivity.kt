package marceloviana1991.medidasreeducativas

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marceloviana1991.medidasreeducativas.databinding.ActivityMainBinding
import marceloviana1991.medidasreeducativas.databinding.FormularioEditarBinding
import java.time.LocalDate

class MainActivity : AppCompatActivity() {


    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val medidaReeducativaDao by lazy {
        AppDatabase.instancia(this).medidaReeducativaDao()
    }
    private lateinit var adapter: ArrayAdapter<MedidaReeducativa>
    private val mainScope = MainScope()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val medidasEducativas = mutableListOf<MedidaReeducativa>()
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            medidasEducativas
        )
        binding.listView.adapter = adapter

        mainScope.launch {
            val medidasReeducativas = withContext(Dispatchers.IO) {
                medidaReeducativaDao.buscaTodas()
            }
            withContext(Dispatchers.Main) {
                adapter.addAll(medidasReeducativas)
            }
        }

        binding.buttonSalvar.setOnClickListener {
            val interno = binding.editTextNome.text.toString().trim()
            val intervencao = binding.editTextMedidaReeducativa.text.toString().trim()
            val prazo = binding.editTextPrazo.text.toString().trim()
            if (interno.isNotEmpty() && intervencao.isNotEmpty() && prazo.isNotEmpty()) {
                val medidaReeducativa = MedidaReeducativa(
                    interno = interno,
                    intervencao = intervencao,
                    prazo = prazo,
                    localDate = LocalDate.now()
                )
                mainScope.launch {
                    withContext(Dispatchers.IO) {
                        medidaReeducativaDao.salva(medidaReeducativa)
                    }
                    adapter.add(medidaReeducativa)
                }
                binding.editTextNome.setText("")
                binding.editTextMedidaReeducativa.setText("")
                binding.editTextPrazo.setText("")
            }

        }
        registerForContextMenu(binding.listView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        mainScope.launch {
            val medidasReeducativas = withContext(Dispatchers.IO) {
                medidaReeducativaDao.buscaTodas()
            }
            val medidasParaExcluir = medidasReeducativas.filter { it.verificaPrazo() }
            withContext(Dispatchers.IO) {
                medidasParaExcluir.forEach { medidaReeducativaDao.exclui(it) }
            }
            withContext(Dispatchers.Main) {
                adapter.clear()
                adapter.addAll(medidasReeducativas - medidasParaExcluir)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val position = info.position
        return when (item.itemId) {
            R.id.action_edit -> {
                val bindingFormularioEditarBinding = FormularioEditarBinding.inflate(layoutInflater)
                bindingFormularioEditarBinding.editTextNome.setText(adapter.getItem(position)?.interno)
                bindingFormularioEditarBinding.editTextPrazo.setText(adapter.getItem(position)?.prazo)
                bindingFormularioEditarBinding.editTextMedidaReeducativa.setText(adapter.getItem(position)?.intervencao)
                AlertDialog.Builder(this)
                    .setTitle("Formulário editar")
                    .setView(bindingFormularioEditarBinding.root)
                    .setPositiveButton("CONFIRMAR" ) { _, _ ->
                        val interno = bindingFormularioEditarBinding.editTextNome.text.toString().trim()
                        val intervencao = bindingFormularioEditarBinding.editTextMedidaReeducativa.text.toString().trim()
                        val prazo = bindingFormularioEditarBinding.editTextPrazo.text.toString().trim()
                        if (interno.isNotEmpty() && intervencao.isNotEmpty() && prazo.isNotEmpty()) {
                            val medidaReeducativa = adapter.getItem(position)
                            if (medidaReeducativa != null) {
                                mainScope.launch {
                                    adapter.remove(medidaReeducativa)
                                    medidaReeducativa.interno = interno
                                    medidaReeducativa.intervencao = intervencao
                                    medidaReeducativa.prazo = prazo
                                    withContext(Dispatchers.IO) {
                                        medidaReeducativaDao.edita(medidaReeducativa)
                                    }
                                    withContext(Dispatchers.Main) {
                                        adapter.add(medidaReeducativa)
                                    }
                                }



                            }
                        }
                    }
                    .setNegativeButton("CANCELAR") { _, _ ->

                    }
                    .show()
                true
            }
            R.id.action_delete -> {
                AlertDialog.Builder(this)
                    .setTitle("Botão excluir")
                    .setMessage("Deseja realmente excluir essa medida reeducativa?")
                    .setPositiveButton("CONFIRMAR" ) { _, _ ->
                        val medidaReeducativa = adapter.getItem(position)
                        if (medidaReeducativa != null) {
                            mainScope.launch {
                                withContext(Dispatchers.IO) {
                                    medidaReeducativaDao.exclui(medidaReeducativa)
                                }
                                withContext(Dispatchers.Main) {
                                    adapter.remove(medidaReeducativa)
                                }
                            }
                        }
                    }
                    .setNegativeButton("CANCELAR") { _, _ ->

                    }
                    .show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }
}