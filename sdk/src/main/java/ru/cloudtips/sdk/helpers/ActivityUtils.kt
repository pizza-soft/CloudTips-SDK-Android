package ru.cloudtips.sdk.helpers

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int, tag: String? = null) {
    supportFragmentManager.inTransaction { add(frameId, fragment, tag) }
}

fun AppCompatActivity.addFragmentToBackStack(fragment: Fragment, frameId: Int, tag: String? = null) {
    supportFragmentManager.inTransaction { add(frameId, fragment, tag).addToBackStack(null) }
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int, tag: String? = null) {
    supportFragmentManager.inTransaction { replace(frameId, fragment, tag) }
}

fun AppCompatActivity.addFragmentToBackStackWithTag(fragment: Fragment, frameId: Int, tag: String) {
    supportFragmentManager.inTransaction { add(frameId, fragment, tag).addToBackStack(null) }
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction { replace(frameId, fragment) }
}

fun AppCompatActivity.replaceFragmentWithBackStack(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction { replace(frameId, fragment).addToBackStack(null) }
}

fun AppCompatActivity.cleanBackStack() {
    val fm = supportFragmentManager
    for (i in 0 until fm.backStackEntryCount) {
        fm.popBackStack()
    }
}