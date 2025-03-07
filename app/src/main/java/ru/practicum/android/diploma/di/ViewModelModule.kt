package ru.practicum.android.diploma.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.practicum.android.diploma.presentation.city.CitySelectViewModel
import ru.practicum.android.diploma.presentation.country.SelectCountryViewModel
import ru.practicum.android.diploma.presentation.details.DetailsFragmentViewModel
import ru.practicum.android.diploma.presentation.favorites.FavoriteJobViewModel
import ru.practicum.android.diploma.presentation.filtration.FiltrationViewModel
import ru.practicum.android.diploma.presentation.industry.IndustryViewModel
import ru.practicum.android.diploma.presentation.region.SelectRegionViewModel
import ru.practicum.android.diploma.presentation.search.SearchJobViewModel
import ru.practicum.android.diploma.presentation.team.TeamViewModel

val viewModelModule = module {
    viewModel {
        SearchJobViewModel(
            hhInteractor = get(),
            filterInteractor = get()
        )
    }
    viewModel {
        TeamViewModel()
    }
    viewModel {
        FavoriteJobViewModel(
            interactor = get()
        )
    }
    viewModel {
        DetailsFragmentViewModel(
            hhInteractor = get(),
            favoritesInteractor = get(),
            vacancySharingInteractor = get(),
        )
    }
    viewModel {
        CitySelectViewModel(
            citySelectInteractor = get(),
            filterInteractor = get()
        )
    }
    viewModel {
        SelectCountryViewModel(
            hhInteractor = get(),
            filterInteractor = get()
        )
    }

    viewModel {
        IndustryViewModel(
            interactor = get(),
            filterInteractor = get()
        )
    }

    viewModel {
        FiltrationViewModel(
            filterInteractor = get()
        )
    }
    viewModel {
        SelectRegionViewModel(
            filterInteractor = get()
        )
    }
}
