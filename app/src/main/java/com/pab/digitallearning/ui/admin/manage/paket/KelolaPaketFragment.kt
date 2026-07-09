package com.pab.digitallearning.ui.admin.manage.paket

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.Package
import com.pab.digitallearning.databinding.FragmentKelolaPaketBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KelolaPaketFragment : Fragment() {

    private var _binding: FragmentKelolaPaketBinding? = null
    private val binding get() = _binding!!
    private var packageList = listOf<Package>()
    private lateinit var adapter: PackageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaPaketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        fetchPackages()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI() {
        adapter = PackageAdapter { pkg -> showActionBottomSheet(pkg) }

        binding.rvPackages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPackages.adapter = adapter

        val today = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
        binding.tvLastUpdate.text = today

        binding.btnBack.setGemoyClick { findNavController().popBackStack() }
        binding.btnTambahPaket.setGemoyClick {
            findNavController().navigate(R.id.action_kelolaPaketFragment_to_addPaketFragment)
        }

        binding.swipeRefresh.setOnRefreshListener { fetchPackages() }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = if (query.isEmpty()) {
                    packageList
                } else {
                    packageList.filter { it.namaPaket?.lowercase()?.contains(query) == true }
                }
                adapter.submitList(filtered)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun fetchPackages() {
        if (_binding == null) return
        showLoading(true)

        ApiClient.apiService.getPackages().enqueue(object : Callback<List<Package>> {
            override fun onResponse(call: Call<List<Package>>, response: Response<List<Package>>) {
                if (_binding != null) {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        val packages = response.body() ?: emptyList()
                        packageList = packages
                        adapter.submitList(packages)
                        binding.tvTotalPaket.text = packages.size.toString()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Gagal mengambil data paket")
                    }
                }
            }
            override fun onFailure(call: Call<List<Package>>, t: Throwable) {
                if (_binding != null) {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan jaringan: ${t.message}")
                }
            }
        })
    }

    private fun showLoading(show: Boolean) {
        if (_binding == null) return
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            val spinner = binding.ivLoadingSpinner
            val avd = spinner.drawable as? android.graphics.drawable.AnimatedVectorDrawable
            avd?.start()
        }
    }

    fun showActionBottomSheet(pkg: Package) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_package_actions, null)

        view.findViewById<TextView>(R.id.tvPackageName).text = pkg.namaPaket ?: "Paket"

        val btnEdit = view.findViewById<LinearLayout>(R.id.btnEdit)
        val btnDelete = view.findViewById<LinearLayout>(R.id.btnDelete)

        btnEdit.setGemoyClick {
            dialog.dismiss()
            editPackage(pkg)
        }

        btnDelete.setGemoyClick {
            dialog.dismiss()
            confirmDelete(pkg)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun editPackage(pkg: Package) {
        val bundle = Bundle().apply {
            putLong("id", pkg.id ?: 0L)
            putString("nama", pkg.namaPaket)
            putString("jurusan", pkg.jurusan)
            putLong("tingkatan_id", pkg.tingkatanId ?: 0L)
            putString("tingkatan_name", pkg.tingkatanName)
            putString("deskripsi", pkg.deskripsi)
        }
        findNavController().navigate(R.id.action_kelolaPaketFragment_to_editPaketFragment, bundle)
    }

    private fun confirmDelete(pkg: Package) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Paket")
            .setMessage("Yakin ingin menghapus \"${pkg.namaPaket}\"?")
            .setPositiveButton("Hapus") { _, _ -> deletePackage(pkg.id ?: 0L) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deletePackage(id: Long) {
        showLoading(true)
        ApiClient.apiService.deletePackage(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (_binding != null) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Paket berhasil dihapus") {
                            fetchPackages()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal menghapus paket")
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (_binding != null) {
                    showLoading(false)
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
