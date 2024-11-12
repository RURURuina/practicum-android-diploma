package ru.practicum.android.diploma.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchJobBinding
import ru.practicum.android.diploma.domain.models.entity.Vacancy
import ru.practicum.android.diploma.ui.search.adapters.VacancyAdapter

class SearchJobFragment : Fragment() {

    private var binding: FragmentSearchJobBinding? = null
    private val viewModel: SearchJobViewModel by viewModel()
    private val vacancyAdapter = VacancyAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchJobBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEditText()
        initRecyclerView()
        observeViewModel()
    }

    private fun initEditText() {
        binding?.searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSearchIcon(s.isNullOrEmpty())
                viewModel.searchVacancies(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding?.clearSearchButton?.setOnClickListener {
            binding?.searchEditText?.text?.clear()
            viewModel.searchVacancies("")
        }
    }

    private fun initRecyclerView() {
        binding?.vacanciesRecyclerView?.adapter = vacancyAdapter
    }

    private fun observeViewModel() {
        viewModel.vacanciesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VacanciesState.Loading -> showLoading()
                is VacanciesState.Error -> showError(state.message)
                is VacanciesState.Success -> {
                    hideLoading()
                    updateRecyclerView(state.vacancies)
                }
                is VacanciesState.Empty -> showEmptyState()
            }
        }
    }

    private fun updateSearchIcon(isEmpty: Boolean) {
        binding?.searchEditText?.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.search_24px,
            0,
            if (isEmpty) 0 else R.drawable.close_24px,
            0
        )
    }

    private fun showLoading() {
        binding?.progressBar?.visibility = View.VISIBLE
        binding?.searchLayout?.visibility = View.GONE
        binding?.noInternetLayout?.visibility = View.GONE
        binding?.noJobsLayout?.visibility = View.GONE
    }

    private fun hideLoading() {
        binding?.progressBar?.visibility = View.GONE
    }

    private fun showError(message: Int) {
        binding?.noInternetLayout?.visibility = View.VISIBLE
        binding?.searchLayout?.visibility = View.GONE
        binding?.noJobsLayout?.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding?.noJobsLayout?.visibility = View.VISIBLE
        binding?.searchLayout?.visibility = View.GONE
        binding?.noInternetLayout?.visibility = View.GONE
    }

    private fun updateRecyclerView(vacancies: List<Vacancy>) {
        binding?.vacanciesRecyclerView?.visibility = View.VISIBLE
        binding?.noJobsLayout?.visibility = View.GONE
        vacancyAdapter.submitList(vacancies)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
