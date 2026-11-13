package com.example.ra5generarinformes

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerCiudad: Spinner
    private lateinit var botonGenerarInforme: Button
    private lateinit var botonExportarPdf: Button
    private lateinit var contenedorInforme: LinearLayout
    private lateinit var textoCiudadSeleccionada: TextView
    private lateinit var textoNumeroOfertas: TextView
    private lateinit var textoSalarioTotal: TextView
    private lateinit var textoSalarioMedio: TextView
    private lateinit var textoSalarioMaximo: TextView
    private lateinit var textoSalarioMinimo: TextView
    private lateinit var textoConclusion: TextView
    private lateinit var barraValencia: ProgressBar
    private lateinit var barraMadrid: ProgressBar
    private lateinit var barraAlicante: ProgressBar
    private lateinit var barraBarcelona: ProgressBar
    private lateinit var textoCantidadValencia: TextView
    private lateinit var textoCantidadMadrid: TextView
    private lateinit var textoCantidadAlicante: TextView
    private lateinit var textoCantidadBarcelona: TextView
    private lateinit var contenedorOfertas: LinearLayout

    private val ofertas = ArrayList<Oferta>()

    private val creadorPdf = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        if (uri != null) {
            guardarPdfEnUri(uri)
        } else {
            Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerCiudad = findViewById(R.id.spinnerCiudad)
        botonGenerarInforme = findViewById(R.id.botonGenerarInforme)
        botonExportarPdf = findViewById(R.id.botonExportarPdf)
        contenedorInforme = findViewById(R.id.contenedorInforme)
        textoCiudadSeleccionada = findViewById(R.id.textoCiudadSeleccionada)
        textoNumeroOfertas = findViewById(R.id.textoNumeroOfertas)
        textoSalarioTotal = findViewById(R.id.textoSalarioTotal)
        textoSalarioMedio = findViewById(R.id.textoSalarioMedio)
        textoSalarioMaximo = findViewById(R.id.textoSalarioMaximo)
        textoSalarioMinimo = findViewById(R.id.textoSalarioMinimo)
        textoConclusion = findViewById(R.id.textoConclusion)
        barraValencia = findViewById(R.id.barraValencia)
        barraMadrid = findViewById(R.id.barraMadrid)
        barraAlicante = findViewById(R.id.barraAlicante)
        barraBarcelona = findViewById(R.id.barraBarcelona)
        textoCantidadValencia = findViewById(R.id.textoCantidadValencia)
        textoCantidadMadrid = findViewById(R.id.textoCantidadMadrid)
        textoCantidadAlicante = findViewById(R.id.textoCantidadAlicante)
        textoCantidadBarcelona = findViewById(R.id.textoCantidadBarcelona)
        contenedorOfertas = findViewById(R.id.contenedorOfertas)

        configurarSpinner()
        ofertas.addAll(cargarOfertas())

        botonGenerarInforme.setOnClickListener {
            val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada)
            mostrarResultado(ciudadSeleccionada, ofertasFiltradas)
        }

        botonExportarPdf.setOnClickListener {
            val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
            creadorPdf.launch("Informe_Ofertas_$ciudadSeleccionada.pdf")
        }
    }

    private fun configurarSpinner() {
        val ciudades = arrayOf("Todas", "Valencia", "Madrid", "Alicante", "Barcelona")
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, ciudades)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCiudad.adapter = adaptador
    }

    private fun cargarOfertas(): ArrayList<Oferta> {
        val listaOfertas = ArrayList<Oferta>()
        val textoJson = assets.open("ofertas.json").bufferedReader().use { it.readText() }
        val arrayJson = JSONArray(textoJson)

        for (i in 0 until arrayJson.length()) {
            val objetoJson = arrayJson.getJSONObject(i)
            listaOfertas.add(
                Oferta(
                    objetoJson.getInt("id"),
                    objetoJson.getString("puesto"),
                    objetoJson.getString("empresa"),
                    objetoJson.getString("ciudad"),
                    objetoJson.getInt("salario"),
                    objetoJson.getString("modalidad"),
                ),
            )
        }
        return listaOfertas
    }

    private fun filtrarOfertasPorCiudad(ciudad: String): List<Oferta> {
        return if (ciudad == "Todas") {
            ofertas
        } else {
            ofertas.filter { it.ciudad == ciudad }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarResultado(ciudad: String, ofertasFiltradas: List<Oferta>) {
        val numeroOfertas = ofertasFiltradas.size
        val salarioTotal = ofertasFiltradas.sumOf { it.salario }
        val salarioMedio = if (ofertasFiltradas.isNotEmpty()) {
            ofertasFiltradas.asSequence().map { it.salario }.average()
        } else {
            0.0
        }
        val salarioMaximo = ofertasFiltradas.maxOfOrNull { it.salario } ?: 0
        val salarioMinimo = ofertasFiltradas.minOfOrNull { it.salario } ?: 0

        textoCiudadSeleccionada.text = "Ciudad seleccionada: $ciudad"
        textoNumeroOfertas.text = "Numero de ofertas: $numeroOfertas"
        textoSalarioTotal.text = "Salario total: ${formatearSalario(salarioTotal)}"
        textoSalarioMedio.text = "Salario medio: ${formatearSalario(salarioMedio.toInt())}"
        textoSalarioMaximo.text = "Salario maximo: ${formatearSalario(salarioMaximo)}"
        textoSalarioMinimo.text = "Salario minimo: ${formatearSalario(salarioMinimo)}"

        val mejorOferta = ofertasFiltradas.maxByOrNull { it.salario }
        if (mejorOferta != null) {
            textoConclusion.text = "La oferta mejor pagada del filtro es: ${mejorOferta.puesto} (${formatearSalario(mejorOferta.salario)})"
        } else {
            textoConclusion.text = "La oferta mejor pagada del filtro es: -"
        }

        mostrarGrafico(ofertasFiltradas)

        contenedorOfertas.removeAllViews()
        ofertasFiltradas.forEachIndexed { indice, oferta ->
            val textoOferta = TextView(this)
            textoOferta.text = "${indice + 1}. ${oferta.puesto} (${oferta.modalidad})\n" +
                    "${oferta.empresa} - ${oferta.ciudad} - ${formatearSalario(oferta.salario)}"
            textoOferta.textSize = 16f
            textoOferta.setPadding(0, 0, 0, 24)
            contenedorOfertas.addView(textoOferta)
        }
    }

    private fun formatearSalario(salario: Int): String {
        return "%,d".format(salario).replace(",", ".") + " €"
    }

    private fun mostrarGrafico(ofertasFiltradas: List<Oferta>) {
        val ofertasValencia = ofertasFiltradas.count { it.ciudad == "Valencia" }
        val ofertasMadrid = ofertasFiltradas.count { it.ciudad == "Madrid" }
        val ofertasAlicante = ofertasFiltradas.count { it.ciudad == "Alicante" }
        val ofertasBarcelona = ofertasFiltradas.count { it.ciudad == "Barcelona" }

        val valorMaximo = maxOf(ofertasValencia, ofertasMadrid, ofertasAlicante, ofertasBarcelona, 1)

        barraValencia.max = valorMaximo
        barraMadrid.max = valorMaximo
        barraAlicante.max = valorMaximo
        barraBarcelona.max = valorMaximo

        barraValencia.progress = ofertasValencia
        barraMadrid.progress = ofertasMadrid
        barraAlicante.progress = ofertasAlicante
        barraBarcelona.progress = ofertasBarcelona

        textoCantidadValencia.text = ofertasValencia.toString()
        textoCantidadMadrid.text = ofertasMadrid.toString()
        textoCantidadAlicante.text = ofertasAlicante.toString()
        textoCantidadBarcelona.text = ofertasBarcelona.toString()
    }

    private fun guardarPdfEnUri(uri: android.net.Uri) {
        try {
            val documentoPdf = android.graphics.pdf.PdfDocument()

            val infoPagina = android.graphics.pdf.PdfDocument.PageInfo.Builder(
                contenedorInforme.width,
                contenedorInforme.height,
                1,
            ).create()

            val pagina = documentoPdf.startPage(infoPagina)
            val canvas = pagina.canvas

            contenedorInforme.draw(canvas)
            documentoPdf.finishPage(pagina)

            contentResolver.openOutputStream(uri)?.use { salida ->
                documentoPdf.writeTo(salida)
            }
            documentoPdf.close()

            Toast.makeText(this, "PDF guardado correctamente", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar el PDF", Toast.LENGTH_SHORT).show()
        }
    }
}