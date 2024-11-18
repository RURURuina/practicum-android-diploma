package ru.practicum.android.diploma.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchJobBinding
import ru.practicum.android.diploma.domain.models.entity.Vacancy
import ru.practicum.android.diploma.presentation.search.SearchJobViewModel
import ru.practicum.android.diploma.ui.root.RootActivity.Companion.VACANCY_TRANSFER_KEY
import ru.practicum.android.diploma.ui.search.adapters.VacancyAdapter
import ru.practicum.android.diploma.ui.search.models.VacanciesState
import ru.practicum.android.diploma.util.ResponseStatusCode
import ru.practicum.android.diploma.util.debounce

class SearchJobFragment : Fragment() {
    private companion object {
        const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private var binding: FragmentSearchJobBinding? = null
    private val viewModel: SearchJobViewModel by viewModel()
    private val vacancyAdapter = VacancyAdapter()
    private var scrollListener: RecyclerView.OnScrollListener? = null
    private var onItemClick: ((Vacancy) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSearchJobBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEditText()
        initRecyclerView()
        observeViewModel()
        prepareOnItemClick()
    }

    private fun prepareOnItemClick() {
        onItemClick = debounce(
            delayMillis = CLICK_DEBOUNCE_DELAY,
            coroutineScope = viewLifecycleOwner.lifecycleScope,
            useLastParam = true
        ) { vacancy ->
            val bundle = bundleOf(VACANCY_TRANSFER_KEY to vacancy.id)
            /*vacancy id нужно будет вынести в компаньон обджект в апп*/
            findNavController().navigate(R.id.action_searchJobFragment_to_detailsFragment, bundle)
        }

    }

    private fun initEditText() {
        binding?.searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // функция не используется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSearchIcon(s.isNullOrEmpty())
                if (!s.isNullOrBlank()) {
                    updateRecyclerView(emptyList())
                    viewModel.searchDebounce(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

                if (s?.isEmpty() == true) {
                    viewModel.clearVacancies()
                }
            }
        })

        binding?.clearSearchButton?.setOnClickListener {
            binding?.searchEditText?.text?.clear()
            viewModel.clearVacancies()
        }
    }

    private fun initRecyclerView() {
        binding?.vacanciesRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = vacancyAdapter

            scrollListener?.let { removeOnScrollListener(it) }
            scrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount // кол-во элементов на экране
                    val totalItemCount = layoutManager.itemCount // сколько всего элементов в списке
                    val positionFirst =
                        layoutManager.findFirstVisibleItemPosition() // номер первого видимого элемента на экране

                    if (visibleItemCount + positionFirst >= totalItemCount && positionFirst >= 0) {
                        viewModel.loadNextPage()
                    }
                }
            }.also {
                addOnScrollListener(it)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.vacanciesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VacanciesState.Loading -> showLoading()
                is VacanciesState.Error -> {
                    hideLoading()
                    showError(state.responseState)
                }

                is VacanciesState.Success -> {
                    hideLoading()
                    updateRecyclerView(state.vacancies)
                }

                is VacanciesState.Empty -> {
                    hideLoading()
                    showEmptyState()
                }

                is VacanciesState.Hidden -> clearRecyclerView()
            }
        }
    }

    private fun clearRecyclerView() {
        updateRecyclerView(emptyList())
        showHiddenState()
    }

    private fun updateSearchIcon(isEmpty: Boolean) {
        binding?.clearSearchButton?.setImageResource(
            if (isEmpty) R.drawable.search_24px else R.drawable.close_24px
        )
    }

    private fun showHiddenState() {
        binding?.searchLayout?.visibility = View.VISIBLE
        binding?.errorLayout?.visibility = View.GONE
        binding?.noJobsLayout?.visibility = View.GONE
    }

    private fun showLoading() {
        binding?.progressBar?.visibility = View.VISIBLE
        binding?.searchLayout?.visibility = View.GONE
        binding?.errorLayout?.visibility = View.GONE
        binding?.noJobsLayout?.visibility = View.GONE
        binding?.vacanciesRecyclerView?.visibility = View.GONE
    }

    private fun hideLoading() {
        binding?.progressBar?.visibility = View.GONE
    }

    private fun showError(responseState: ResponseStatusCode?) {
        binding?.vacanciesRecyclerView?.visibility = View.GONE
        binding?.searchLayout?.visibility = View.GONE
        binding?.noJobsLayout?.visibility = View.GONE

        when (responseState) {
            null -> {}
            ResponseStatusCode.ERROR -> {
                binding?.errorTv?.setText(R.string.server_error)
                binding?.errorImage?.setImageResource(R.drawable.server_error_on_search_screen)
            }

            ResponseStatusCode.NO_INTERNET -> {
                binding?.errorTv?.setText(R.string.no_internet)
                binding?.errorImage?.setImageResource(R.drawable.no_internet_placeholder)
            }
            else -> {}
        }
        binding?.errorLayout?.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        binding?.vacanciesRecyclerView?.visibility = View.GONE
        binding?.searchLayout?.visibility = View.GONE
        binding?.errorLayout?.visibility = View.GONE
        binding?.noJobsLayout?.visibility = View.VISIBLE

    }

    private fun updateRecyclerView(vacancies: List<Vacancy>) {
        binding?.vacanciesRecyclerView?.visibility = View.VISIBLE
        binding?.noJobsLayout?.visibility = View.GONE
        binding?.searchLayout?.visibility = View.GONE
        binding?.errorLayout?.visibility = View.GONE
        vacancyAdapter.submitList(vacancies)
        onItemClick?.let {
            vacancyAdapter.onItemClick = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        scrollListener = null
    }
}
