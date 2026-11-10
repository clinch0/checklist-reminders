package dev.portfolio.checklist
import android.content.Context
import androidx.room.*
import androidx.work.*
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

@Entity(tableName="tasks") data class Task(@PrimaryKey(autoGenerate=true)val id:Long=0,val title:String,val done:Boolean=false,val createdAt:Long=System.currentTimeMillis(),val dueAtMillis:Long?=null,val remind:Boolean=false)
@Dao interface TaskDao{@Query("SELECT * FROM tasks ORDER BY done,dueAtMillis IS NULL,dueAtMillis,createdAt DESC")fun all():Flow<List<Task>>;@Insert(onConflict=OnConflictStrategy.REPLACE)suspend fun save(t:Task):Long;@Delete suspend fun delete(t:Task)}
@Database(entities=[Task::class],version=1,exportSchema=false)abstract class TaskDatabase:RoomDatabase(){abstract fun dao():TaskDao}
enum class TaskFilter{ALL,ACTIVE,DONE}
fun filtered(tasks:List<Task>,filter:TaskFilter)=when(filter){TaskFilter.ALL->tasks;TaskFilter.ACTIVE->tasks.filterNot{it.done};TaskFilter.DONE->tasks.filter{it.done}}
fun reminderDelay(now:Long,due:Long)=maxOf(0,due-now)
class TaskRepository(private val dao:TaskDao,private val context:Context){val tasks=dao.all();private val work=WorkManager.getInstance(context);suspend fun save(task:Task):Long{val id=dao.save(task);schedule(task.copy(id=id));return id};suspend fun toggle(task:Task){val next=task.copy(done=!task.done);dao.save(next);if(next.done)cancel(next.id)else schedule(next)};suspend fun delete(task:Task){dao.delete(task);cancel(task.id)};private fun schedule(t:Task){cancel(t.id);val due=t.dueAtMillis?:return;if(!t.remind||t.done)return;val request=OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(reminderDelay(System.currentTimeMillis(),due),TimeUnit.MILLISECONDS).setInputData(workDataOf("title" to t.title,"id" to t.id)).build();work.enqueueUniqueWork("task-${t.id}",ExistingWorkPolicy.REPLACE,request)};private fun cancel(id:Long)=work.cancelUniqueWork("task-$id")}
