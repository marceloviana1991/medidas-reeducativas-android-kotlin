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

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            medidaReeducativaDao.buscaTodas()
        )
        binding.listView.adapter = adapter

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
                medidaReeducativaDao.salva(medidaReeducativa)
                adapter.add(medidaReeducativa)
                adapter.notifyDataSetChanged()
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
        for (medidaReeducativa: MedidaReeducativa in medidaReeducativaDao.buscaTodas()) {
            if (medidaReeducativa.verificaPrazo()) {
                medidaReeducativaDao.exclui(medidaReeducativa)
                adapter.remove(medidaReeducativa)
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
                                adapter.remove(medidaReeducativa)
                                medidaReeducativa.interno = interno
                                medidaReeducativa.intervencao = intervencao
                                medidaReeducativa.prazo = prazo
                                medidaReeducativaDao.edita(medidaReeducativa)
                                adapter.add(medidaReeducativa)

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
                            medidaReeducativaDao.exclui(medidaReeducativa)
                            adapter.remove(medidaReeducativa)
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