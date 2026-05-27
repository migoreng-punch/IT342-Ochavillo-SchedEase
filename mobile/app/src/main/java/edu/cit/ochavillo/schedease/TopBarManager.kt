package edu.cit.ochavillo.schedease

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import edu.cit.ochavillo.schedease.network.SessionManager

class TopBarManager(
    private val activity: AppCompatActivity,
    private val drawerLayout: DrawerLayout
) {
    fun setupTopBar() {
        val toolbar = activity.findViewById<MaterialToolbar>(R.id.topToolbar) ?: return
        val user = SessionManager.currentUser.value

        // 1. Set the Page Title dynamically
        toolbar.title = ""

        // 2. Make the Hamburger Icon open the Sidebar
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

    }
}