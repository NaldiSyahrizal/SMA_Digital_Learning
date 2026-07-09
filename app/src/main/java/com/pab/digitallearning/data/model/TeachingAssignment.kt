package com.pab.digitallearning.data.model

import com.google.gson.annotations.SerializedName

data class TeachingAssignment(
    val id: Long? = null,
    @SerializedName("teacher_id")
    val teacherId: Long,
    @SerializedName("class_id")
    val classId: Long,
    @SerializedName("subject_id")
    val subjectId: Long,
    @SerializedName("teacher_name")
    val teacherName: String? = null,
    @SerializedName("class_name")
    val className: String? = null,
    @SerializedName("subject_name")
    val subjectName: String? = null
)
