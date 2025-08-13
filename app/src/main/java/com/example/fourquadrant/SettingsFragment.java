package com.example.fourquadrant;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.example.fourquadrant.data.DataBackupManager;
import com.example.fourquadrant.database.repository.SettingsRepository;

public class SettingsFragment extends Fragment {

    private Button exportDataButton;
    private Button importDataButton;
    private Button clearDataButton;
    private SettingsRepository settingsRepository;
    private DataBackupManager dataBackupManager;
    private ActivityResultLauncher<Intent> importFileLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        settingsRepository = new SettingsRepository(requireActivity().getApplication());
        dataBackupManager = new DataBackupManager(requireContext());
        
        initViews(view);
        setupImportFileLauncher();

        
        return view;
    }

    private void initViews(View view) {

        exportDataButton = view.findViewById(R.id.exportDataButton);
        importDataButton = view.findViewById(R.id.importDataButton);
        clearDataButton = view.findViewById(R.id.clearDataButton);

        exportDataButton.setOnClickListener(v -> exportData());
        importDataButton.setOnClickListener(v -> importData());
        clearDataButton.setOnClickListener(v -> clearAllData());
    }

    private void setupImportFileLauncher() {
        importFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        // 将Uri转换为文件路径
                        String filePath = getFilePathFromUri(uri);
                        if (filePath != null) {
                            dataBackupManager.importData(filePath, new DataBackupManager.ImportCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(requireContext(), "数据导入成功", Toast.LENGTH_SHORT).show();
                                // 通知MainActivity刷新数据
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).notifyFragmentsUpdate();
                                }
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "导入失败: " + error, Toast.LENGTH_LONG).show();
                            }
                            });
                        } else {
                            Toast.makeText(requireContext(), "无法读取文件", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }



    private void exportData() {
        dataBackupManager.exportData(new DataBackupManager.ExportCallback() {
            @Override
            public void onSuccess(String filePath) {
                // 在主线程更新UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "数据导出成功", Toast.LENGTH_SHORT).show();
                        
                        // 分享文件
                        try {
                            java.io.File file = new java.io.File(filePath);
                            android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                                requireContext(),
                                requireContext().getPackageName() + ".fileprovider",
                                file
                            );
                            
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("application/json");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "四象限任务管理数据备份");
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            
                            Intent chooser = Intent.createChooser(shareIntent, "分享备份文件");
                            startActivity(chooser);
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "分享文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                // 在主线程显示错误信息
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "导出失败: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void importData() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        importFileLauncher.launch(Intent.createChooser(intent, "选择备份文件"));
    }

    private void clearAllData() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("清空所有数据")
            .setMessage("此操作将删除所有任务、提醒和设置数据，且无法恢复。确定要继续吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                dataBackupManager.clearAllData(new DataBackupManager.ClearDataCallback() {
                    @Override
                    public void onSuccess() {
                        // 在主线程更新UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "所有数据已清空", Toast.LENGTH_SHORT).show();
                                
                                // 通知MainActivity刷新数据
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).notifyFragmentsUpdate();
                                }
                            });
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        // 在主线程显示错误信息
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "清空数据失败: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                });
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private String getFilePathFromUri(Uri uri) {
        try {
            // 对于文件选择器返回的Uri，尝试直接获取路径
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
            
            // 对于content://类型的Uri，需要复制到临时文件
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                return copyUriToTempFile(uri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String copyUriToTempFile(Uri uri) {
        try {
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            java.io.File tempFile = new java.io.File(requireContext().getCacheDir(), "temp_backup.json");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            inputStream.close();
            outputStream.close();
            
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}