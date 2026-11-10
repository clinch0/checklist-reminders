@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package dev.portfolio.checklist
import android.Manifest
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

data class TasksState(val tasks:List<Task> = emptyList(),val filter:TaskFilter=TaskFilter.ALL)
class TasksViewModel(private val repo:TaskRepository):ViewModel(){private val filter=MutableStateFlow(TaskFilter.ALL);val state=combine(repo.tasks,filter){t,f->TasksState(filtered(t,f),f)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),TasksState());fun filter(v:TaskFilter){filter.value=v};fun save(title:String,due:Long?,remind:Boolean,done:()->Unit)=viewModelScope.launch{repo.save(Task(title=title.trim(),dueAtMillis=due,remind=remind));done()};fun toggle(t:Task)=viewModelScope.launch{repo.toggle(t)};fun delete(t:Task)=viewModelScope.launch{repo.delete(t)}}
class MainActivity:ComponentActivity(){override fun onCreate(s:Bundle?){super.onCreate(s);setContent{val repo=(application as PortfolioApp).repository;val vm:TasksViewModel=viewModel(factory=object:ViewModelProvider.Factory{override fun<T:ViewModel>create(c:Class<T>):T=TasksViewModel(repo)as T});MaterialTheme(colorScheme=if(androidx.compose.foundation.isSystemInDarkTheme())darkColorScheme()else lightColorScheme()){TaskApp(vm)}}}}
@Composable fun TaskApp(vm:TasksViewModel){val st by vm.state.collectAsStateWithLifecycle();var editing by rememberSaveable{mutableStateOf(false)};if(editing)TaskEditor({editing=false}){a,b,c->vm.save(a,b,c){editing=false}}else TaskList(st,vm){editing=true}}
@Composable fun TaskList(st:TasksState,vm:TasksViewModel,add:()->Unit){Scaffold(topBar={TopAppBar(title={Text("Чек-лист")})},floatingActionButton={FloatingActionButton(add){Icon(Icons.Default.Add,"Добавить")}}){pad->Column(Modifier.padding(pad)){SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(12.dp)){listOf(TaskFilter.ALL to "Все",TaskFilter.ACTIVE to "Активные",TaskFilter.DONE to "Выполненные").forEachIndexed{i,(f,label)->SegmentedButton(st.filter==f,{vm.filter(f)},SegmentedButtonDefaults.itemShape(i,3)){Text(label)}}};if(st.tasks.isEmpty())Box(Modifier.fillMaxSize().padding(32.dp)){Text(if(st.filter==TaskFilter.ALL)"Пока нет задач" else "В этом разделе пусто")};LazyColumn{items(st.tasks,key={it.id}){t->ListItem(leadingContent={Checkbox(t.done,{vm.toggle(t)})},headlineContent={Text(t.title)},supportingContent={t.dueAtMillis?.let{Text(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMMM, HH:mm"))) }},trailingContent={IconButton({vm.delete(t)}){Icon(Icons.Default.Delete,"Удалить")}})}}}}}
@Composable fun TaskEditor(back:()->Unit,save:(String,Long?,Boolean)->Unit){var title by rememberSaveable{mutableStateOf("")};var date by rememberSaveable{mutableStateOf(LocalDate.now().plusDays(1).toString())};var time by rememberSaveable{mutableStateOf("09:00")};var hasDue by rememberSaveable{mutableStateOf(false)};var remind by rememberSaveable{mutableStateOf(false)};var error by remember{mutableStateOf("")};val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->if(!granted)remind=false};Scaffold(topBar={TopAppBar(title={Text("Новая задача")},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,"Назад")}})}){pad->Column(Modifier.padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){OutlinedTextField(title,{title=it},label={Text("Название")},modifier=Modifier.fillMaxWidth());Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Указать срок");Switch(hasDue,{hasDue=it;if(!it)remind=false})};if(hasDue){OutlinedTextField(date,{date=it},label={Text("Дата, ГГГГ-ММ-ДД")},modifier=Modifier.fillMaxWidth());OutlinedTextField(time,{time=it},label={Text("Время, ЧЧ:ММ")},modifier=Modifier.fillMaxWidth());Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Напомнить");Switch(remind,{value->if(value&&Build.VERSION.SDK_INT>=33){permission.launch(Manifest.permission.POST_NOTIFICATIONS)};remind=value})}};if(error.isNotEmpty())Text(error,color=MaterialTheme.colorScheme.error);Button({val due=if(hasDue)runCatching{LocalDateTime.of(LocalDate.parse(date),LocalTime.parse(time)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()}.getOrNull()else null;if(title.isBlank()||(hasDue&&due==null))error="Проверьте название, дату и время" else save(title,due,remind)},Modifier.fillMaxWidth()){Text("Сохранить")}}}}
