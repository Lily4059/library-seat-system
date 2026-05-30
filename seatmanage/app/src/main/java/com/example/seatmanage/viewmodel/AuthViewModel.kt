package com.example.seatmanage.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seatmanage.data.AppConfig
import com.example.seatmanage.data.MockData
import com.example.seatmanage.data.api.*
import com.example.seatmanage.data.model.User
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
    var currentUser by mutableStateOf(User())
    var isLoading by mutableStateOf(false)

    private val localUsers = mutableMapOf("test" to "123456")

    fun login(username: String, password: String, onResult: (Boolean, String, Boolean) -> Unit) {
        if (!AppConfig.USE_API) {
            loginLocal(username, password, onResult)
            return
        }
        isLoading = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body()?.code == 200) {
                    val data = response.body()!!.data!!
                    RetrofitClient.token = data.token
                    currentUser = User(id = data.userInfo.id, username = username, name = data.userInfo.name,
                        studentNo = data.userInfo.studentNo ?: "", creditScore = data.userInfo.credit)
                    isLoggedIn = true
                    isLoading = false
                    onResult(true, "登录成功", data.userInfo.studentNo == null)
                } else {
                    isLoading = false
                    onResult(false, response.body()?.message ?: "登录失败", false)
                }
            } catch (e: Exception) {
                isLoading = false
                onResult(false, "网络错误：${e.message}", false)
            }
        }
    }

    private fun loginLocal(username: String, password: String, onResult: (Boolean, String, Boolean) -> Unit) {
        val savedPwd = localUsers[username]
        if (savedPwd != null && savedPwd == password) {
            currentUser = MockData.testUser.copy(username = username, name = username)
            isLoggedIn = true
            onResult(true, "登录成功（本地数据）", false)
            return
        }
        if (username == "test" && password == "123456") {
            currentUser = MockData.testUser
            isLoggedIn = true
            onResult(true, "登录成功（本地数据）", false)
            return
        }
        onResult(false, "用户名或密码错误", false)
    }

    fun register(username: String, password: String, name: String, studentNo: String, onResult: (Boolean, String) -> Unit) {
        if (!AppConfig.USE_API) {
            registerLocal(username, password, name, studentNo, onResult)
            return
        }
        isLoading = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(username, password, name, studentNo.ifEmpty { null }))
                if (response.isSuccessful && response.body()?.code == 200) {
                    val data = response.body()!!.data!!
                    RetrofitClient.token = data.token
                    currentUser = User(id = data.userInfo.id, username = username, name = name,
                        studentNo = studentNo, creditScore = data.userInfo.credit)
                    isLoggedIn = true
                    isLoading = false
                    onResult(true, "注册成功")
                } else {
                    isLoading = false
                    onResult(false, response.body()?.message ?: "注册失败")
                }
            } catch (e: Exception) {
                isLoading = false
                onResult(false, "网络错误：${e.message}")
            }
        }
    }

    private fun registerLocal(username: String, password: String, name: String, studentNo: String, onResult: (Boolean, String) -> Unit) {
        if (username.length < 3) { onResult(false, "用户名至少3位"); return }
        if (password.length < 6) { onResult(false, "密码至少6位"); return }
        if (localUsers.containsKey(username) || username == "test") {
            onResult(false, "该用户名已被占用"); return
        }
        localUsers[username] = password
        currentUser = User(username = username, name = name, studentNo = studentNo, creditScore = 100)
        isLoggedIn = true
        onResult(true, "注册成功（本地数据）")
    }

    fun fetchProfile(onResult: (Boolean) -> Unit = {}) {
        if (!AppConfig.USE_API) { onResult(true); return }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getProfile()
                if (response.isSuccessful && response.body()?.code == 200) {
                    val p = response.body()!!.data!!
                    currentUser = currentUser.copy(name = p.name, studentNo = p.studentNo ?: "",
                        creditScore = p.creditScore, totalStudyTime = p.totalStudyTime, phone = p.phone ?: "")
                    onResult(true)
                } else onResult(false)
            } catch (_: Exception) { onResult(false) }
        }
    }

    fun bindStudent(name: String, studentNo: String, onResult: (Boolean, String) -> Unit) {
        if (!AppConfig.USE_API) {
            currentUser = currentUser.copy(name = name, studentNo = studentNo)
            onResult(true, "绑定成功")
            return
        }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateProfile(mapOf("name" to name, "studentNo" to studentNo))
                if (response.isSuccessful && response.body()?.code == 200) {
                    currentUser = currentUser.copy(name = name, studentNo = studentNo)
                    onResult(true, "绑定成功")
                } else onResult(false, response.body()?.message ?: "绑定失败")
            } catch (e: Exception) { onResult(false, "网络错误：${e.message}") }
        }
    }

    fun logout() {
        RetrofitClient.token = null
        isLoggedIn = false
        currentUser = User()
        SeatRepository.reset()
        ReservationRepository.reset()
    }
}
