package dev.portfolio.checklist
import android.app.Application
import androidx.room.Room
class PortfolioApp:Application(){val db by lazy{Room.databaseBuilder(this,TaskDatabase::class.java,"tasks.db").build()};val repository by lazy{TaskRepository(db.dao(),this)}}
