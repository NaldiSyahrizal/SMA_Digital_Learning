package com.pab.digitallearning.ui.teacher

sealed class TeacherRoute(val route: String, val title: String) {
    object Dashboard : TeacherRoute("dashboard", "Beranda")
    object Content : TeacherRoute("content", "Kelas")
    object Notification : TeacherRoute("notification", "Notifikasi")
    object Profile : TeacherRoute("profile", "Profil")
}
