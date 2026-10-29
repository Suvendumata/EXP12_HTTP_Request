package com.example.exp12

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var initialStateContainer: LinearLayout
    private lateinit var loadingStateContainer: LinearLayout
    private lateinit var successStateContainer: LinearLayout
    private lateinit var errorStateContainer: LinearLayout
    private lateinit var txtErrorMessage: TextView
    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val mainLayout = findViewById<View>(R.id.mainLayout)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI containers and controls
        initialStateContainer = findViewById(R.id.initialStateContainer)
        loadingStateContainer = findViewById(R.id.loadingStateContainer)
        successStateContainer = findViewById(R.id.successStateContainer)
        errorStateContainer = findViewById(R.id.errorStateContainer)
        txtErrorMessage = findViewById(R.id.txtErrorMessage)
        rvUsers = findViewById(R.id.rvUsers)

        val btnFetch = findViewById<Button>(R.id.btnFetch)
        val btnRetry = findViewById<Button>(R.id.btnRetry)
        val btnBackToInitial = findViewById<Button>(R.id.btnBackToInitial)

        // Set up RecyclerView
        rvUsers.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(emptyList()) { selectedUser ->
            val intent = Intent(this, UserDetailsActivity::class.java).apply {
                putExtra(UserDetailsActivity.EXTRA_USER, selectedUser)
            }
            startActivity(intent)
        }
        rvUsers.adapter = userAdapter

        // Button click listeners
        btnFetch.setOnClickListener {
            fetchUsersFromApi()
        }

        btnRetry.setOnClickListener {
            fetchUsersFromApi()
        }

        btnBackToInitial.setOnClickListener {
            showInitialState()
        }

        showInitialState()
    }

    private fun showInitialState() {
        initialStateContainer.visibility = View.VISIBLE
        loadingStateContainer.visibility = View.GONE
        successStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.GONE
    }

    private fun showLoadingState() {
        initialStateContainer.visibility = View.GONE
        loadingStateContainer.visibility = View.VISIBLE
        successStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.GONE
    }

    private fun showSuccessState() {
        initialStateContainer.visibility = View.GONE
        loadingStateContainer.visibility = View.GONE
        successStateContainer.visibility = View.VISIBLE
        errorStateContainer.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        initialStateContainer.visibility = View.GONE
        loadingStateContainer.visibility = View.GONE
        successStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.VISIBLE
        txtErrorMessage.text = message
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetwork
            if (activeNetwork != null) {
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (capabilities != null) {
                    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }
            }
        }
        return false
    }

    private fun fetchUsersFromApi() {
        if (!isNetworkConnected()) {
            showErrorState("No Internet Connection.\n\nPlease connect your device or emulator to the Internet and tap Retry.")
            return
        }

        showLoadingState()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jsonString = makeHttpGetRequest("https://jsonplaceholder.typicode.com/users")
                val users = parseUsersJson(jsonString)

                withContext(Dispatchers.Main) {
                    userAdapter.updateUsers(users)
                    showSuccessState()
                }
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showErrorState("Unable to resolve host \"jsonplaceholder.typicode.com\".\n\nPlease check your internet connection or emulator DNS settings, then tap Retry.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = e.localizedMessage ?: "Failed to connect to the server."
                withContext(Dispatchers.Main) {
                    showErrorState("Network Error: $errorMsg\n\nPlease check your internet connection and try again.")
                }
            }
        }
    }

    private fun makeHttpGetRequest(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 12000
        connection.readTimeout = 12000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
        connection.setRequestProperty("Accept", "application/json")

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
            connection.disconnect()
            return response.toString()
        } else {
            connection.disconnect()
            throw Exception("HTTP Server returned response code $responseCode")
        }
    }

    private fun parseUsersJson(jsonString: String): List<User> {
        val userList = mutableListOf<User>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val id = obj.getInt("id")
            val name = obj.getString("name")
            val username = obj.getString("username")
            val email = obj.getString("email")
            val phone = obj.optString("phone", "N/A")
            val website = obj.optString("website", "N/A")

            val addressObj = obj.getJSONObject("address")
            val address = Address(
                street = addressObj.optString("street", ""),
                suite = addressObj.optString("suite", ""),
                city = addressObj.optString("city", ""),
                zipcode = addressObj.optString("zipcode", "")
            )

            val companyObj = obj.getJSONObject("company")
            val company = Company(
                name = companyObj.optString("name", ""),
                catchPhrase = companyObj.optString("catchPhrase", ""),
                bs = companyObj.optString("bs", "")
            )

            userList.add(
                User(
                    id = id,
                    name = name,
                    username = username,
                    email = email,
                    phone = phone,
                    website = website,
                    address = address,
                    company = company
                )
            )
        }

        return userList
    }
}