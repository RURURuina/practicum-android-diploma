package ru.practicum.android.diploma.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesVacancyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacancy(vacancy: FavoritesVacancyEntity)

    @Delete
    suspend fun deleteVacancy(vacancy: FavoritesVacancyEntity)

    @Query("SELECT * FROM favorites")
    fun getFavoriteVacancies(): Flow<List<FavoritesVacancyEntity>>

    @Query("SELECT * FROM favorites WHERE id = :id")
    suspend fun getFavoriteVacancyById(id: String): FavoritesVacancyEntity?
}
