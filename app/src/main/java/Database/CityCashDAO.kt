package Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CityCashDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCity(city: CityCacheEntity)

    @Query("SELECT * FROM city_cache ORDER BY dt DESC")
    fun getAllSortedByFreshness(): Flow<List<CityCacheEntity>>

    @Query("SELECT * FROM city_cache WHERE cityId = :cityId LIMIT 1")
    suspend fun getCity(cityId: Long): CityCacheEntity?

    @Query("DELETE FROM city_cache WHERE cityId = :cityId")
    suspend fun removeCity(cityId: Long)

    @Query("DELETE FROM city_cache")
    suspend fun clearAll()
}