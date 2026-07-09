package com.pab.digitallearning.data.model

data class StudentClassroom(
    val id: Long? = null,
    val student_id: Long,
    val class_id: Long,
    val student: StudentProfile? = null,
    val classroom: Classroom? = null
)

data class PlotingRequest(
    val class_id: Long,
    val student_ids: List<Long>
)
