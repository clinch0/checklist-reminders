package dev.portfolio.checklist
import org.junit.Assert.assertEquals
import org.junit.Test
class TaskLogicTest{@Test fun futureDelay()=assertEquals(500,reminderDelay(1000,1500));@Test fun pastDelayIsZero()=assertEquals(0,reminderDelay(2000,1500));@Test fun filters(){val all=listOf(Task(1,"a",false),Task(2,"b",true));assertEquals(2,filtered(all,TaskFilter.ALL).size);assertEquals(listOf("a"),filtered(all,TaskFilter.ACTIVE).map{it.title});assertEquals(listOf("b"),filtered(all,TaskFilter.DONE).map{it.title})}}
