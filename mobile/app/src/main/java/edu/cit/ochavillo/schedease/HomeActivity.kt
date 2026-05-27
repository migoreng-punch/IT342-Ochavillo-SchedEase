package edu.cit.ochavillo.schedease

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    // Initialize the ViewModel
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: EstablishmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        HeaderManager(this).setupHeader(R.id.mainToolbar)

        val etSearch = findViewById<EditText>(R.id.etSearch)
        val rvEstablishments = findViewById<RecyclerView>(R.id.rvEstablishments)
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val btnLoadMore = findViewById<MaterialButton>(R.id.btnLoadMore)

        // Setup Adapter and Click Listener
        adapter = EstablishmentAdapter { establishment ->
            // 1. Create the Intent pointing to your new Details screen
            val intent = Intent(this@HomeActivity, EstablishmentDetailActivity::class.java)

            // 2. Attach the specific establishment's ID
            intent.putExtra("EST_ID", establishment.id)

            // 3. Launch the screen!
            startActivity(intent)
        }
        rvEstablishments.adapter = adapter

        // Bind Search Input to ViewModel
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }
        })

        // Bind Load More Button
        btnLoadMore.setOnClickListener {
            viewModel.loadMore()
        }

        // Observe StateFlows from ViewModel
        lifecycleScope.launch {
            viewModel.establishments.collect { items ->
                adapter.submitList(items)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
                if (isLoading) rvEstablishments.visibility = View.GONE else rvEstablishments.visibility = View.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.hasMore.collect { hasMore ->
                // Only show button if there are more items AND we aren't currently doing the initial load
                val shouldShow = hasMore && !viewModel.isLoading.value
                btnLoadMore.visibility = if (shouldShow) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoadingMore.collect { isLoadingMore ->
                btnLoadMore.text = if (isLoadingMore) "Loading..." else "Load More Establishments"
                btnLoadMore.isEnabled = !isLoadingMore
            }
        }
    }
}