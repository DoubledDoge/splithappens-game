package io.github.doubleddoge.splithappens.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

inline fun <reified VM : ViewModel> viewModelFactoryOf(crossinline create: () -> VM) =
	viewModelFactory { initializer { create() } }
