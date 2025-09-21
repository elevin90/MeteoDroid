package Database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CityCacheEntity::class], version = 2)
abstract class AppDatabase: RoomDatabase() {
    abstract fun cityCashDAO(): CityCashDAO
}
