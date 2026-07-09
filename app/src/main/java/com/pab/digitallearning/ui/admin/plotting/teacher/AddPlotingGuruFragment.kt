package com.pab.digitallearning.ui.admin.plotting.teacher

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.data.model.Classroom
import com.pab.digitallearning.data.model.Subject
import com.pab.digitallearning.data.model.TeacherProfile
import com.pab.digitallearning.databinding.DialogSearchableListBinding
import com.pab.digitallearning.databinding.FragmentTambahPlotingGuruBinding
import com.pab.digitallearning.util.DialogUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPlotingGuruFragment : Fragment() {

    private var _binding: FragmentTambahPlotingGuruBinding? = null
    private val binding get() = _binding!!

    private var teachers = listOf<TeacherProfile>()
    private var subjects = listOf<Subject>()
    private var classrooms = listOf<Classroom>()
    private var existingAssignments = listOf<com.pab.digitallearning.data.model.TeachingAssignment>()

    private var selectedTeacherId: Long? = null
    private var selectedSubjectId: Long? = null
    private var selectedClassIds: MutableList<Long> = mutableListOf()
    private var assignmentId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahPlotingGuruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        assignmentId = arguments?.getLong("assignmentId") ?: 0L
        if (assignmentId != 0L) {
            binding.tvTitle.text = "Edit Ploting Guru"
            binding.btnSave.text = "Perbarui Ploting"
            
            selectedTeacherId = arguments?.getLong("teacherId")
            selectedSubjectId = arguments?.getLong("subjectId")
            arguments?.getLong("classId")?.let {
                if (it != 0L) selectedClassIds.add(it)
            }
            
            binding.etTeacher.setText(arguments?.getString("teacherName"))
            binding.etSubject.setText(arguments?.getString("subjectName"))
            binding.etClass.setText(arguments?.getString("className"))
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnSave.setOnClickListener { saveAssignment() }

        setupClickListeners()
        fetchData()
    }

    private fun setupClickListeners() {
        binding.etTeacher.setOnClickListener {
            val items = teachers.map { SearchableItem(it.id ?: 0L, it.namaLengkap ?: "-", it.id == selectedTeacherId) }
            showSearchableDialog("Pilih Guru", items, isMultiSelect = false) { selectedItems ->
                if (selectedItems.isNotEmpty()) {
                    selectedTeacherId = selectedItems.first().id
                    binding.etTeacher.setText(selectedItems.first().name)
                }
            }
        }

        binding.etSubject.setOnClickListener {
            if (selectedClassIds.isEmpty()) {
                DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Pilih kelas terlebih dahulu!")
                return@setOnClickListener
            }

            val selectedClasses = selectedClassIds.mapNotNull { id -> classrooms.find { it.id == id } }
            val classTingkatanId = selectedClasses.firstOrNull()?.tingkatanId

            val distinctPackageIds = selectedClasses.map { it.packageId }.distinct()
            val classPackageId = if (distinctPackageIds.size == 1) distinctPackageIds.first() else null

            // Compute the list of already-plotted subjects in any of the selected classes from `existingAssignments`:
            val plottedSubjectIdsInClass = existingAssignments
                .filter { selectedClassIds.contains(it.classId) && (assignmentId == 0L || it.id != assignmentId) }
                .map { it.subjectId }

            val filteredSubjects = subjects.filter { subject ->
                val matchesPackageOrTingkatan = if (classPackageId != null) {
                    subject.packageIds?.contains(classPackageId) == true
                } else {
                    subject.tingkatanId == classTingkatanId
                }
                val isNotPlotted = !plottedSubjectIdsInClass.contains(subject.id)
                matchesPackageOrTingkatan && isNotPlotted
            }

            if (filteredSubjects.isEmpty()) {
                DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Tidak ada mata pelajaran tersedia (atau semua sudah diploting) untuk tingkatan/paket kelas ini!")
                return@setOnClickListener
            }

            val items = filteredSubjects.map { subject ->
                val label = if (!subject.kodeMapel.isNullOrEmpty()) {
                    "${subject.nama} (${subject.kodeMapel})"
                } else {
                    subject.nama ?: "-"
                }
                SearchableItem(subject.id ?: 0L, label, subject.id == selectedSubjectId)
            }

            showSearchableDialog("Pilih Mata Pelajaran", items, isMultiSelect = false) { selectedItems ->
                if (selectedItems.isNotEmpty()) {
                    val clickedId = selectedItems.first().id
                    selectedSubjectId = clickedId
                    val rawSubject = filteredSubjects.find { it.id == clickedId }
                    val displayValue = if (rawSubject != null && !rawSubject.kodeMapel.isNullOrEmpty()) {
                        "${rawSubject.nama} (${rawSubject.kodeMapel})"
                    } else {
                        rawSubject?.nama ?: selectedItems.first().name
                    }
                    binding.etSubject.setText(displayValue)
                }
            }
        }

        binding.etClass.setOnClickListener {
            val isMulti = assignmentId == 0L
            val items = classrooms.map { SearchableItem(it.id ?: 0L, it.namaKelas ?: "-", selectedClassIds.contains(it.id)) }
            showSearchableDialog("Pilih Kelas", items, isMultiSelect = isMulti) { selectedItems ->
                if (isMulti) {
                    if (selectedItems.isNotEmpty()) {
                        // Check if all selected classes have the same tingkatan
                        val selectedClassObjects = selectedItems.mapNotNull { item -> classrooms.find { it.id == item.id } }
                        val distinctTingkatans = selectedClassObjects.map { it.tingkatanId }.distinct()

                        if (distinctTingkatans.size > 1) {
                            DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Pilih kelas dengan tingkatan yang sama!")
                            return@showSearchableDialog
                        }
                    }

                    val oldTingkatanId = selectedClassIds.firstOrNull()?.let { id -> classrooms.find { it.id == id }?.tingkatanId }

                    selectedClassIds.clear()
                    selectedClassIds.addAll(selectedItems.map { it.id })
                    
                    val text = if (selectedItems.isEmpty()) "" else selectedItems.joinToString(", ") { it.name }
                    binding.etClass.setText(text)

                    val newTingkatanId = selectedClassIds.firstOrNull()?.let { id -> classrooms.find { it.id == id }?.tingkatanId }
                    if (oldTingkatanId != newTingkatanId) {
                        selectedSubjectId = null
                        binding.etSubject.setText("")
                    }
                } else {
                    if (selectedItems.isNotEmpty()) {
                        val oldTingkatanId = selectedClassIds.firstOrNull()?.let { id -> classrooms.find { it.id == id }?.tingkatanId }

                        selectedClassIds.clear()
                        selectedClassIds.add(selectedItems.first().id)
                        binding.etClass.setText(selectedItems.first().name)

                        val newTingkatanId = selectedClassIds.firstOrNull()?.let { id -> classrooms.find { it.id == id }?.tingkatanId }
                        if (oldTingkatanId != newTingkatanId) {
                            selectedSubjectId = null
                            binding.etSubject.setText("")
                        }
                    }
                }
            }
        }
    }

    private fun showSearchableDialog(
        title: String,
        items: List<SearchableItem>,
        isMultiSelect: Boolean,
        onResult: (List<SearchableItem>) -> Unit
    ) {
        val dialogBinding = DialogSearchableListBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext(), com.google.android.material.R.style.Theme_Design_BottomSheetDialog)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvDialogTitle.text = title

        val adapter = SearchableListAdapter(isMultiSelect) { selectedItem ->
            if (!isMultiSelect) {
                onResult(listOf(selectedItem))
                dialog.dismiss()
            }
        }
        
        dialogBinding.rvDialogItems.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvDialogItems.adapter = adapter
        adapter.submitList(items)

        if (isMultiSelect) {
            dialogBinding.btnDialogDone.visibility = View.VISIBLE
            dialogBinding.btnDialogDone.setOnClickListener {
                onResult(adapter.getSelectedItems())
                dialog.dismiss()
            }
        } else {
            dialogBinding.btnDialogDone.visibility = View.GONE
        }

        dialogBinding.etDialogSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.lowercase() ?: ""
                val filtered = items.filter { it.name.lowercase().contains(query) }
                adapter.submitList(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        dialog.show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return
        if (isLoading) {
            binding.loadingOverlay.visibility = View.VISIBLE
            (binding.ivLoadingSpinner.drawable as? Animatable)?.start()
        } else {
            binding.loadingOverlay.visibility = View.GONE
            (binding.ivLoadingSpinner.drawable as? Animatable)?.stop()
        }
    }

    private fun fetchData() {
        var fetchCount = 0
        showLoading(true)

        val checkLoading = {
            fetchCount++
            if (fetchCount == 4) showLoading(false)
        }

        ApiClient.apiService.getTeachersManage().enqueue(object : Callback<List<TeacherProfile>> {
            override fun onResponse(call: Call<List<TeacherProfile>>, response: Response<List<TeacherProfile>>) {
                if (response.isSuccessful) teachers = response.body() ?: emptyList()
                checkLoading()
            }
            override fun onFailure(call: Call<List<TeacherProfile>>, t: Throwable) { checkLoading() }
        })

        ApiClient.apiService.getSubjects().enqueue(object : Callback<List<Subject>> {
            override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                if (response.isSuccessful) {
                    subjects = response.body() ?: emptyList()
                    if (assignmentId != 0L && selectedSubjectId != null) {
                        val matchingSubject = subjects.find { it.id == selectedSubjectId }
                        if (matchingSubject != null) {
                            val displayValue = if (!matchingSubject.kodeMapel.isNullOrEmpty()) {
                                "${matchingSubject.nama} (${matchingSubject.kodeMapel})"
                            } else {
                                matchingSubject.nama ?: "-"
                            }
                            binding.etSubject.setText(displayValue)
                        }
                    }
                }
                checkLoading()
            }
            override fun onFailure(call: Call<List<Subject>>, t: Throwable) { checkLoading() }
        })

        ApiClient.apiService.getClassrooms().enqueue(object : Callback<List<Classroom>> {
            override fun onResponse(call: Call<List<Classroom>>, response: Response<List<Classroom>>) {
                if (response.isSuccessful) classrooms = response.body() ?: emptyList()
                checkLoading()
            }
            override fun onFailure(call: Call<List<Classroom>>, t: Throwable) { checkLoading() }
        })

        ApiClient.apiService.getTeachingAssignments().enqueue(object : Callback<List<com.pab.digitallearning.data.model.TeachingAssignment>> {
            override fun onResponse(call: Call<List<com.pab.digitallearning.data.model.TeachingAssignment>>, response: Response<List<com.pab.digitallearning.data.model.TeachingAssignment>>) {
                if (response.isSuccessful) existingAssignments = response.body() ?: emptyList()
                checkLoading()
            }
            override fun onFailure(call: Call<List<com.pab.digitallearning.data.model.TeachingAssignment>>, t: Throwable) { checkLoading() }
        })
    }

    private fun saveAssignment() {
        val tId = selectedTeacherId
        val sId = selectedSubjectId

        if (tId == null || sId == null || selectedClassIds.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Harap lengkapi semua data")
            return
        }

        binding.btnSave.isEnabled = false
        showLoading(true)
        
        var successCount = 0
        var failCount = 0
        val totalRequests = selectedClassIds.size

        val checkCompletion = {
            if (successCount + failCount == totalRequests) {
                binding.btnSave.isEnabled = true
                showLoading(false)
                if (failCount == 0) {
                    DialogUtils.showSuccessDialog(requireContext(), message = "Ploting guru berhasil disimpan") {
                        findNavController().popBackStack()
                    }
                } else {
                    DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Berhasil menyimpan $successCount ploting. Gagal: $failCount")
                }
            }
        }

        selectedClassIds.forEach { cId ->
            val call = if (assignmentId == 0L) {
                ApiClient.apiService.addTeachingAssignment(tId, cId, sId)
            } else {
                ApiClient.apiService.updateTeachingAssignment(assignmentId, tId, cId, sId)
            }

            call.enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful) successCount++ else failCount++
                    checkCompletion()
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    failCount++
                    checkCompletion()
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
