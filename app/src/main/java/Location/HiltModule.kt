package Location

import Database.AppDatabase
import Database.CityCashDAO
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    @Provides @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context
    ): LocationService {
        return  DefaultLocationService(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDataBase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "weather.db")
            .fallbackToDestructiveMigration() // на время разработки
            .build()

    @Provides
    fun provideCityCashDAO(db: AppDatabase): CityCashDAO = db.cityCashDAO()
}