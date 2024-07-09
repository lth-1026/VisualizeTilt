package de.fra_uas.fb2.mobiledevices.visualizetilt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.fra_uas.fb2.mobiledevices.visualizetilt.graph.GraphFragment
import de.fra_uas.fb2.mobiledevices.visualizetilt.tilt.TiltFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_tilt-> {
                    val homeFragment = TiltFragment()
                    openFragment(homeFragment)
                    true
                }
                R.id.navigation_graph -> {
                    val dashboardFragment = GraphFragment()
                    openFragment(dashboardFragment)
                    true
                }
                else -> false
            }
        }

        // 기본으로 첫 번째 항목을 선택된 상태로 설정
        bottomNavigationView.selectedItemId = R.id.navigation_tilt
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}