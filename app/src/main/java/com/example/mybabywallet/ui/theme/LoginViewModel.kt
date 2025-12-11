package com.example.mybabywallet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mybabywallet.data.AppDatabase
import com.example.mybabywallet.data.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).usuarioDao()

    val loginExitoso = MutableLiveData<Int>()
    val registroExitoso = MutableLiveData<Boolean>()
    val errorMensaje = MutableLiveData<String?>()

    fun login(user: String, pass: String) {
        if (user.isBlank() || pass.isBlank()) {
            errorMensaje.postValue("Debes llenar todos los campos")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val usuarioEncontrado = dao.login(user, pass)
            if (usuarioEncontrado != null) {
                loginExitoso.postValue(usuarioEncontrado.id) // <--- Devolvemos el ID
            } else {
                errorMensaje.postValue("Usuario o contraseña incorrectos")
            }
        }
    }

    fun registrar(nombre: String, user: String, pass: String) {
        if (nombre.isBlank() || user.isBlank() || pass.length < 4) {
            errorMensaje.postValue("Datos inválidos (pass min 4 caracteres)")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val existe = dao.checkUsuarioExiste(user)
            if (existe == null) {
                dao.insertar(Usuario(nombre = nombre, username = user, password = pass))
                registroExitoso.postValue(true)
            } else {
                errorMensaje.postValue("El usuario ya existe")
            }
        }
    }

    fun limpiarErrores() { errorMensaje.value = null }
}