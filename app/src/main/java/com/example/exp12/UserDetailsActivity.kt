package com.example.exp12

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar

class UserDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER = "extra_user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_details)

        val detailsLayout = findViewById<View>(R.id.detailsLayout)
        ViewCompat.setOnApplyWindowInsetsListener(detailsLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDetails)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_USER, User::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_USER) as? User
        }

        user?.let { displayUserDetails(it) }
    }

    private fun displayUserDetails(user: User) {
        findViewById<TextView>(R.id.txtDetailIdBadge).text = user.id.toString()
        findViewById<TextView>(R.id.txtDetailName).text = user.name
        findViewById<TextView>(R.id.txtDetailUsername).text = "@${user.username}"
        findViewById<TextView>(R.id.txtDetailEmail).text = user.email
        findViewById<TextView>(R.id.txtDetailPhone).text = user.phone
        findViewById<TextView>(R.id.txtDetailWebsite).text = user.website
        findViewById<TextView>(R.id.txtDetailAddress).text = user.address.fullAddress
        findViewById<TextView>(R.id.txtDetailCompanyName).text = user.company.name
        findViewById<TextView>(R.id.txtDetailCompanyCatchPhrase).text = user.company.catchPhrase
        findViewById<TextView>(R.id.txtDetailCompanyBs).text = user.company.bs
    }
}