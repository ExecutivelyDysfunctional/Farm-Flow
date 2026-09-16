package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.CompanionViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `test companion viewmodel initialization`() {
    try {
      val app = ApplicationProvider.getApplicationContext<Application>()
      val vm = CompanionViewModel(app)
      println("CompanionViewModel created successfully")
    } catch (e: Throwable) {
      println("CRASH IN VIEWMODEL: ${e.message}")
      e.printStackTrace()
      throw e
    }
  }

  @Test
  fun `launch main activity`() {
    try {
      val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
      println("MainActivity created successfully")
    } catch (e: Throwable) {
      println("CRASH IN ACTIVITY: ${e.message}")
      e.printStackTrace()
      throw e
    }
  }
}
