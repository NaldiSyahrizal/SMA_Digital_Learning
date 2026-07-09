package com.pab.digitallearning.core

import com.pab.digitallearning.data.model.*
import com.pab.digitallearning.data.model.DashboardResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("login")
    @FormUrlEncoded
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @POST("forgot-password/request")
    @FormUrlEncoded
    fun forgotPasswordRequest(
        @Field("email") email: String
    ): Call<BasicResponse>

    @POST("forgot-password/verify")
    @FormUrlEncoded
    fun forgotPasswordVerify(
        @Field("email") email: String,
        @Field("otp") otp: String
    ): Call<ResetTokenResponse>

    @POST("forgot-password/reset")
    @FormUrlEncoded
    fun forgotPasswordReset(
        @Field("email") email: String,
        @Field("token") token: String,
        @Field("password") password: String
    ): Call<BasicResponse>

    @GET("user")
    fun getLoggedUser(@Header("Authorization") token: String): Call<UserResponse>
    
    @GET("dashboard")
    fun getDashboardStats(): Call<DashboardResponse>

    // --- MASTER TINGKATAN ---
    @GET("tingkatans")
    fun getTingkatans(): Call<List<Tingkatan>>

    // --- MANAJEMEN GURU ---
    @GET("teachers-manage")
    fun getTeachersManage(): Call<List<TeacherProfile>>

    @POST("teachers-manage")
    @FormUrlEncoded
    fun addTeacher(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("nama_lengkap") nama: String,
        @Field("nip") nip: String,
        @Field("jenis_kelamin") jk: String,
        @Field("no_telp") telp: String,
        @Field("password") password: String
    ): Call<BasicResponse>

    @PUT("teachers-manage/{id}")
    @FormUrlEncoded
    fun updateTeacher(
        @Path("id") id: Long,
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("nama_lengkap") nama: String,
        @Field("nip") nip: String,
        @Field("jenis_kelamin") jk: String,
        @Field("no_telp") telp: String,
        @Field("password") password: String? = null
    ): Call<BasicResponse>

    @DELETE("teachers-manage/{id}")
    fun deleteTeacher(@Path("id") id: Long): Call<BasicResponse>

    // --- MANAJEMEN SISWA ---
    @GET("students-manage")
    fun getStudentsManage(): Call<List<StudentProfile>>

    @POST("students-manage")
    @FormUrlEncoded
    fun addStudent(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("nama_lengkap") nama: String,
        @Field("nis") nis: String,
        @Field("jenis_kelamin") jk: String,
        @Field("no_telp") telp: String,
        @Field("password") password: String
    ): Call<BasicResponse>

    @PUT("students-manage/{id}")
    @FormUrlEncoded
    fun updateStudent(
        @Path("id") id: Long,
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("nama_lengkap") nama: String,
        @Field("nis") nis: String,
        @Field("jenis_kelamin") jk: String,
        @Field("no_telp") telp: String,
        @Field("password") password: String? = null
    ): Call<BasicResponse>

    @DELETE("students-manage/{id}")
    fun deleteStudent(@Path("id") id: Long, @Query("deactivation_reason") reason: String): Call<BasicResponse>

    // --- MANAJEMEN KELAS ---
    @GET("classrooms")
    fun getClassrooms(): Call<List<Classroom>>

    @POST("classrooms")
    @FormUrlEncoded
    fun addClassroom(
        @Field("nama_kelas") nama: String,
        @Field("tingkatan_id") tingkatanId: Long,
        @Field("wali_kelas_id") waliId: Long,
        @Field("package_id") packageId: Long?
    ): Call<BasicResponse>

    @PUT("classrooms/{id}")
    @FormUrlEncoded
    fun updateClassroom(
        @Path("id") id: Long,
        @Field("nama_kelas") nama: String,
        @Field("tingkatan_id") tingkatanId: Long,
        @Field("wali_kelas_id") waliId: Long,
        @Field("package_id") packageId: Long?
    ): Call<BasicResponse>

    @DELETE("classrooms/{id}")
    fun deleteClassroom(@Path("id") id: Long): Call<ResponseBody>
    
    // --- MANAJEMEN MAPEL ---
    @GET("subjects")
    fun getSubjects(): Call<List<Subject>>

    @POST("subjects")
    @FormUrlEncoded
    fun addSubject(
        @Field("nama") nama: String,
        @Field("kode_mapel") kode: String,
        @Field("kategori") kategori: String,
        @Field("tingkatan_id") tingkatanId: Long,
        @Field("package_ids[]") packageIds: List<Long>,
        @Field("jam_pelajaran") jamPelajaran: Int
    ): Call<okhttp3.ResponseBody>

    @PUT("subjects/{id}")
    @FormUrlEncoded
    fun updateSubject(
        @Path("id") id: Long,
        @Field("nama") nama: String,
        @Field("kode_mapel") kode: String,
        @Field("kategori") kategori: String,
        @Field("tingkatan_id") tingkatanId: Long,
        @Field("package_ids[]") packageIds: List<Long>,
        @Field("jam_pelajaran") jamPelajaran: Int
    ): Call<okhttp3.ResponseBody>

    @DELETE("subjects/{id}")
    fun deleteSubject(@Path("id") id: Long): Call<okhttp3.ResponseBody>

    // --- PLOTING SISWA ---
    @GET("student-classrooms")
    fun getStudentClassrooms(): Call<List<StudentClassroom>>

    @POST("student-classrooms")
    @FormUrlEncoded
    fun addStudentClassroom(
        @Field("class_id") classId: Long,
        @Field("student_ids[]") studentIds: List<Long>
    ): Call<BasicResponse>

    @PUT("student-classrooms/{id}")
    @FormUrlEncoded
    fun updateStudentClassroom(
        @Path("id") id: Long,
        @Field("class_id") classId: Long
    ): Call<BasicResponse>
    fun getContentDetail(@Path("id") contentId: Int): Call<StudentContentDetailResponse>

    @POST("contents/{id}/submit")
    @Multipart
    fun submitTask(
        @Path("id") contentId: Int,
        @Part file: okhttp3.MultipartBody.Part?,
        @Part("jawaban_teks") jawabanTeks: okhttp3.RequestBody?
    ): Call<BasicResponse>

    // --- FORUM DISKUSI ---
    @GET("contents/{id}/comments")
    fun getComments(
        @Header("Authorization") token: String,
        @Path("id") contentId: Int
    ): Call<com.pab.digitallearning.data.model.CommentResponse>

    @POST("contents/{id}/comments")
    @Multipart
    fun addComment(
        @Header("Authorization") token: String,
        @Path("id") contentId: Int,
        @Part("komentar") komentar: okhttp3.RequestBody,
        @Part image: okhttp3.MultipartBody.Part?
    ): Call<com.pab.digitallearning.data.model.AddCommentResponse>

    @PUT("contents/{id}/comments/{commentId}")
    @FormUrlEncoded
    fun editComment(
        @Header("Authorization") token: String,
        @Path("id") contentId: Int,
        @Path("commentId") commentId: Int,
        @Field("komentar") komentar: String
    ): Call<BasicResponse>

    @DELETE("contents/{id}/comments/{commentId}")
    fun deleteComment(
        @Header("Authorization") token: String,
        @Path("id") contentId: Int,
        @Path("commentId") commentId: Int
    ): Call<BasicResponse>

    @DELETE("student-classrooms/{id}")
    fun deleteStudentClassroom(@Path("id") id: Long): Call<ResponseBody>

    @GET("unassigned-students")
    fun getUnassignedStudents(): Call<List<StudentProfile>>

    // --- PLOTING GURU ---
    @GET("teaching-assignments")
    fun getTeachingAssignments(): Call<List<TeachingAssignment>>

    @POST("teaching-assignments")
    @FormUrlEncoded
    fun addTeachingAssignment(
        @Field("teacher_id") teacherId: Long,
        @Field("class_id") classId: Long,
        @Field("subject_id") subjectId: Long
    ): Call<BasicResponse>

    @PUT("teaching-assignments/{id}")
    @FormUrlEncoded
    fun updateTeachingAssignment(
        @Path("id") id: Long,
        @Field("teacher_id") teacherId: Long,
        @Field("class_id") classId: Long,
        @Field("subject_id") subjectId: Long
    ): Call<BasicResponse>

    @DELETE("teaching-assignments/{id}")
    fun deleteTeachingAssignment(@Path("id") id: Long): Call<ResponseBody>

    // --- MANAJEMEN PAKET ---
    @GET("packages")
    fun getPackages(): Call<List<Package>>

    @POST("packages")
    @FormUrlEncoded
    fun addPackage(
        @Field("nama_paket") nama: String,
        @Field("jurusan") jurusan: String,
        @Field("tingkatan_id") tingkatanId: Long,
        @Field("deskripsi") deskripsi: String
    ): Call<BasicResponse>

    @PUT("packages/{id}")
    @FormUrlEncoded
    fun updatePackage(
        @Path("id") id: Long,
        @Field("nama_paket") nama: String,
        @Field("jurusan") jurusan: String,
        @Field("tingkatan_id") tingkatanId: Long,
        @Field("deskripsi") deskripsi: String
    ): Call<BasicResponse>

    @DELETE("packages/{id}")
    fun deletePackage(@Path("id") id: Long): Call<ResponseBody>

    // --- PROFIL GURU ---
    @GET("teacher/profile")
    fun getTeacherProfile(@Header("Authorization") token: String): Call<TeacherProfile>

    @PUT("teacher/profile")
    @FormUrlEncoded
    fun updateTeacherProfile(
        @Header("Authorization") token: String,
        @Field("username") username: String,
        @Field("no_telp") noTelp: String,
        @Field("password") password: String? = null
    ): Call<BasicResponse>

    @Multipart
    @POST("teacher/profile/photo")
    fun uploadProfilePicture(
        @Header("Authorization") token: String,
        @Part photo: okhttp3.MultipartBody.Part
    ): Call<BasicResponse>

    // --- KELOLA KONTEN & KELAS GURU ---
    @GET("teacher/classes")
    fun getTeacherClassrooms(
        @Header("Authorization") token: String
    ): Call<TeacherClassroomResponse>

    @GET("teacher/dashboard")
    fun getTeacherDashboardStats(
        @Header("Authorization") token: String
    ): Call<TeacherDashboardResponse>

    @GET("teacher/classes/{classId}/students")
    fun getClassroomStudents(
        @Header("Authorization") token: String,
        @Path("classId") classId: Long
    ): Call<ClassStudentsResponse>

    @Streaming
    @GET("teacher/classes/{classId}/subjects/{subjectId}/export-grades")
    fun exportClassroomGrades(
        @Header("Authorization") token: String,
        @Path("classId") classId: Long,
        @Path("subjectId") subjectId: Long
    ): Call<okhttp3.ResponseBody>

    @GET("teacher/contents")
    fun getClassroomContents(
        @Header("Authorization") token: String,
        @Query("class_id") classId: Long,
        @Query("subject_id") subjectId: Long
    ): Call<ClassroomContentResponse>

    @Multipart
    @POST("teacher/contents")
    fun createClassroomContent(
        @Header("Authorization") token: String,
        @Part("class_id") classId: okhttp3.RequestBody,
        @Part("class_ids[]") classIds: List<@JvmSuppressWildcards okhttp3.RequestBody>? = null,
        @Part("subject_id") subjectId: okhttp3.RequestBody,
        @Part("tipe") tipe: okhttp3.RequestBody,
        @Part("judul") judul: okhttp3.RequestBody,
        @Part("deskripsi") deskripsi: okhttp3.RequestBody,
        @Part("due_date") dueDate: okhttp3.RequestBody? = null,
        @Part("is_closed") isClosed: okhttp3.RequestBody? = null,
        @Part("close_automatically") closeAutomatically: okhttp3.RequestBody? = null,
        @Part("questions") questions: okhttp3.RequestBody? = null,
        @Part("difficulty") difficulty: okhttp3.RequestBody? = null,
        @Part("weight") weight: okhttp3.RequestBody? = null,
        @Part("estimated_duration") estimatedDuration: okhttp3.RequestBody? = null,
        @Part("quiz_duration_minutes") quizDurationMinutes: okhttp3.RequestBody? = null,
        @Part("quiz_max_attempts") quizMaxAttempts: okhttp3.RequestBody? = null,
        @Part("allowed_file_types") allowedFileTypes: okhttp3.RequestBody? = null,
        @Part file: okhttp3.MultipartBody.Part? = null,
        @Part questionImages: List<okhttp3.MultipartBody.Part>? = null
    ): Call<BasicResponse>

    @DELETE("teacher/contents/{id}")
    fun deleteClassroomContent(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Call<BasicResponse>

    @GET("teacher/contents/{id}")
    fun getClassroomContentDetail(
        @Header("Authorization") token: String,
        @Path("id") contentId: Long
    ): Call<ClassroomContentDetailResponse>

    @POST("teacher/contents/{id}/toggle-close")
    fun toggleCloseClassroomContent(
        @Header("Authorization") token: String,
        @Path("id") contentId: Long
    ): Call<BasicResponse>

    @Multipart
    @POST("teacher/contents/{id}")
    fun updateClassroomContent(
        @Header("Authorization") token: String,
        @Path("id") contentId: Long,
        @Part("_method") method: okhttp3.RequestBody, // Should contain "PUT"
        @Part("judul") judul: okhttp3.RequestBody,
        @Part("deskripsi") deskripsi: okhttp3.RequestBody,
        @Part("due_date") dueDate: okhttp3.RequestBody? = null,
        @Part("is_closed") isClosed: okhttp3.RequestBody? = null,
        @Part("close_automatically") closeAutomatically: okhttp3.RequestBody? = null,
        @Part("questions") questions: okhttp3.RequestBody? = null,
        @Part("class_ids[]") classIds: List<@JvmSuppressWildcards okhttp3.RequestBody>? = null,
        @Part("difficulty") difficulty: okhttp3.RequestBody? = null,
        @Part("weight") weight: okhttp3.RequestBody? = null,
        @Part("estimated_duration") estimatedDuration: okhttp3.RequestBody? = null,
        @Part("quiz_duration_minutes") quizDurationMinutes: okhttp3.RequestBody? = null,
        @Part("quiz_max_attempts") quizMaxAttempts: okhttp3.RequestBody? = null,
        @Part("allowed_file_types") allowedFileTypes: okhttp3.RequestBody? = null,
        @Part file: okhttp3.MultipartBody.Part? = null,
        @Part questionImages: List<okhttp3.MultipartBody.Part>? = null
    ): Call<BasicResponse>

    @GET("teacher/contents/{id}/submissions")
    fun getStudentSubmissions(
        @Header("Authorization") token: String,
        @Path("id") contentId: Long
    ): Call<StudentSubmissionResponse>

    @POST("teacher/submissions/{id}/grade")
    @FormUrlEncoded
    fun gradeStudentSubmission(
        @Header("Authorization") token: String,
        @Path("id") submissionId: Long,
        @Field("nilai") nilai: Int,
        @Field("catatan") catatan: String? = null
    ): Call<BasicResponse>

    // --- TEACER NOTIFICATIONS ---
    @GET("teacher/notifications")
    fun getNotifications(
        @Header("Authorization") token: String
    ): Call<NotificationListResponse>

    @POST("teacher/notifications/{id}/read")
    fun markNotificationRead(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Call<BasicResponse>

    @POST("teacher/notifications/mark-all-read")
    fun markAllNotificationsRead(
        @Header("Authorization") token: String
    ): Call<BasicResponse>

    @POST("teacher/notifications/device-token")
    @FormUrlEncoded
    fun updateDeviceToken(
        @Header("Authorization") token: String,
        @Field("device_token") deviceToken: String
    ): Call<BasicResponse>

    @DELETE("teacher/notifications/{id}")
    fun deleteNotification(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Call<BasicResponse>

    @POST("teacher/notifications/delete-multiple")
    @FormUrlEncoded
    fun deleteMultipleNotifications(
        @Header("Authorization") token: String,
        @Field("ids[]") ids: List<Long>
    ): Call<BasicResponse>

    @POST("student/submissions/simulate")
    @FormUrlEncoded
    fun simulateStudentSubmission(
        @Header("Authorization") token: String,
        @Field("content_id") contentId: Long,
        @Field("student_id") studentId: Long,
        @Field("submission_text") text: String? = null
    ): Call<BasicResponse>

    // --- STUDENT MOBILE APP APIS ---
    @GET("student/dashboard")
    fun getStudentDashboard(
        @Header("Authorization") token: String
    ): Call<StudentDashboardResponse>

    @GET("student/subjects")
    fun getStudentSubjects(
        @Header("Authorization") token: String
    ): Call<StudentSubjectResponse>

    @POST("student/subjects/{id}/interest")
    @FormUrlEncoded
    fun saveStudentSubjectInterest(
        @Header("Authorization") token: String,
        @Path("id") subjectId: Long,
        @Field("interest_score") interestScore: Int
    ): Call<BasicResponse>

    @GET("student/contents")
    fun getStudentContents(
        @Header("Authorization") token: String,
        @Query("subject_id") subjectId: Long
    ): Call<StudentContentsResponse>

    @GET("student/contents/{id}")
    fun getStudentContentDetail(
        @Header("Authorization") token: String,
        @Path("id") contentId: Long
    ): Call<StudentContentDetailResponse>

    @Multipart
    @POST("student/submissions")
    fun submitStudentTask(
        @Header("Authorization") token: String,
        @Part("content_id") contentId: okhttp3.RequestBody,
        @Part("submission_text") submissionText: okhttp3.RequestBody? = null,
        @Part("clear_file") clearFile: okhttp3.RequestBody? = null,
        @Part file: okhttp3.MultipartBody.Part? = null
    ): Call<BasicResponse>

    @Multipart
    @POST("student/submissions")
    fun submitStudentQuiz(
        @Header("Authorization") token: String,
        @Part("content_id") contentId: okhttp3.RequestBody,
        @Part("answers") answersJson: okhttp3.RequestBody,
        @Part("exit_count") exitCount: okhttp3.RequestBody? = null,
        @Part("exit_logs") exitLogsJson: okhttp3.RequestBody? = null
    ): Call<StudentQuizSubmitResponse>

    @GET("student/profile")
    fun getStudentProfile(
        @Header("Authorization") token: String
    ): Call<StudentProfileResponse>

    @PUT("student/profile")
    @FormUrlEncoded
    fun updateStudentProfile(
        @Header("Authorization") token: String,
        @Field("username") username: String,
        @Field("no_telp") noTelp: String,
        @Field("password") password: String? = null
    ): Call<BasicResponse>

    @Multipart
    @POST("student/profile/photo")
    fun uploadStudentProfilePicture(
        @Header("Authorization") token: String,
        @Part photo: okhttp3.MultipartBody.Part
    ): Call<BasicResponse>

    @GET("student/notifications")
    fun getStudentNotifications(
        @Header("Authorization") token: String
    ): Call<StudentNotificationListResponse>

    @DELETE("student/submissions/{id}")
    fun deleteStudentSubmission(
        @Header("Authorization") token: String,
        @Path("id") submissionId: Long
    ): Call<BasicResponse>

    // --- PRINCIPAL APIs ---
    @GET("principal/dashboard")
    fun getPrincipalDashboard(
        @Header("Authorization") token: String
    ): Call<PrincipalDashboardResponse>
}
