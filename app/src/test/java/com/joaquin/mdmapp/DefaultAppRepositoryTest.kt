package com.joaquin.mdmapp

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.joaquin.mdmapp.data.DefaultAppRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class DefaultAppRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var packageManager: PackageManager

    @Mock
    lateinit var drawable: Drawable

    private lateinit var repository: DefaultAppRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        repository = DefaultAppRepository()
        whenever(context.packageManager).thenReturn(packageManager)
    }

    @Test
    fun `getInstalledApps returns list of AppInfo`() = runTest {

        val app1 = mock(ApplicationInfo::class.java).apply {
            packageName = "com.example.app1"
            `when`(loadLabel(packageManager)).thenReturn("App One")
            `when`(loadIcon(packageManager)).thenReturn(drawable)
        }

        val app2 = mock(ApplicationInfo::class.java).apply {
            packageName = "com.example.app2"
            `when`(loadLabel(packageManager)).thenReturn("App Two")
            `when`(loadIcon(packageManager)).thenReturn(drawable)
        }

        val apps = listOf(app1, app2)

        whenever(packageManager.getInstalledApplications(PackageManager.GET_META_DATA)).thenReturn(apps)

        whenever(packageManager.getPackageInfo("com.example.app1", 0)).thenReturn(
            PackageInfo().apply { versionName = "1.0" }
        )
        whenever(packageManager.getPackageInfo("com.example.app2", 0)).thenReturn(
            PackageInfo().apply { versionName = "2.0" }
        )

        val result = repository.getInstalledApps(context)

        assertEquals(2, result.size)
        assertEquals("App One", result[0].name)
        assertEquals("1.0", result[0].versionName)
        assertEquals("App Two", result[1].name)
        assertEquals("2.0", result[1].versionName)
    }
}



