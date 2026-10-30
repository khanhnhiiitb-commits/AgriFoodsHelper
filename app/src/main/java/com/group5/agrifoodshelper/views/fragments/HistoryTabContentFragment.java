package com.group5.agrifoodshelper.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.group5.agrifoodshelper.R;
import com.group5.agrifoodshelper.models.Campaign;
import com.group5.agrifoodshelper.models.Contribution;
import com.group5.agrifoodshelper.models.Mobilization;
import com.group5.agrifoodshelper.viewmodels.JobBoardViewModel;
import com.group5.agrifoodshelper.views.adapters.AcceptedJobAdapter;
import com.group5.agrifoodshelper.views.adapters.CampaignAdapter;
import com.group5.agrifoodshelper.views.adapters.ContributionAdapter;
import com.group5.agrifoodshelper.views.adapters.JobAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryTabContentFragment extends Fragment {

    private static final String ARG_TAB_POSITION = "tab_position";
    private static final String TAG = "HistoryDebug";

    private int tabPosition;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FirebaseFirestore db;
    private String currentUserId;

    public static HistoryTabContentFragment newInstance(int position) {
        HistoryTabContentFragment fragment = new HistoryTabContentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabPosition = getArguments().getInt(ARG_TAB_POSITION);
        }
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "";
        }
        Log.d(TAG, "Current User ID: " + currentUserId + " | Tab position: " + tabPosition);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_tab_content, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewHistory);
        tvEmpty = view.findViewById(R.id.tvEmptyHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDataForTab();

        return view;
    }

    private void showEmpty(boolean isEmpty) {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void loadDataForTab() {
        if (currentUserId.isEmpty()) {
            Log.d(TAG, "User ID is empty!");
            showEmpty(true);
            return;
        }

        switch (tabPosition) {
            case 0:
                // TAB 1: Chiến dịch của tôi (chiến dịch được tạo sau khi admin duyệt đơn)
                CampaignAdapter campaignAdapter = new CampaignAdapter();
                recyclerView.setAdapter(campaignAdapter);

                db.collection("Campaigns")
                        .whereEqualTo("authorId", currentUserId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            Log.d(TAG, "Tab 0 loaded campaigns count: " + queryDocumentSnapshots.size());
                            List<Campaign> list = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                list.add(doc.toObject(Campaign.class));
                            }
                            campaignAdapter.setCampaignList(list);
                            showEmpty(list.isEmpty());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Tab 0 error: " + e.getMessage());
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Lỗi tải chiến dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                break;

            case 1:
                // TAB 2: Việc đã nhận (tài xế đã nhận chuyến trong "mobilizations")
                AcceptedJobAdapter acceptedAdapter = new AcceptedJobAdapter();
                recyclerView.setAdapter(acceptedAdapter);

                db.collection("mobilizations")
                        .whereEqualTo("driverId", currentUserId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            Log.d(TAG, "Tab 1 loaded mobilizations count: " + queryDocumentSnapshots.size());

                            List<Mobilization> mobilizations = new ArrayList<>();
                            List<Task<DocumentSnapshot>> campaignTasks = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Mobilization mobilization = doc.toObject(Mobilization.class);
                                if (mobilization.getMobilizationId() == null) {
                                    mobilization.setMobilizationId(doc.getId());
                                }
                                mobilizations.add(mobilization);

                                String campaignId = mobilization.getCampaignId();
                                if (campaignId == null || campaignId.isEmpty()) {
                                    campaignTasks.add(Tasks.<DocumentSnapshot>forResult(null));
                                } else {
                                    campaignTasks.add(db.collection("Campaigns").document(campaignId).get());
                                }
                            }

                            if (mobilizations.isEmpty()) {
                                acceptedAdapter.setItems(new ArrayList<>());
                                showEmpty(true);
                                return;
                            }

                            // Chờ tất cả lượt lấy chiến dịch xong (kể cả lượt lỗi) rồi mới hiển thị
                            Tasks.whenAllComplete(campaignTasks).addOnSuccessListener(done -> {
                                if (!isAdded()) return;

                                List<JobBoardViewModel.JobBoardItem> items = new ArrayList<>();
                                for (int i = 0; i < mobilizations.size(); i++) {
                                    Task<DocumentSnapshot> task = campaignTasks.get(i);
                                    Campaign campaign = null;
                                    if (task.isSuccessful() && task.getResult() != null
                                            && task.getResult().exists()) {
                                        campaign = task.getResult().toObject(Campaign.class);
                                    }
                                    items.add(new JobBoardViewModel.JobBoardItem(mobilizations.get(i), campaign));
                                }
                                acceptedAdapter.setItems(items);
                                showEmpty(items.isEmpty());
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Tab 1 error: " + e.getMessage());
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Lỗi tải việc đã nhận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                break;

            case 2:
                // TAB 3: Đơn đã góp (các lượt bấm góp trong Campaigns/{id}/Contributions)
                ContributionAdapter contributionAdapter = new ContributionAdapter();
                recyclerView.setAdapter(contributionAdapter);

                db.collectionGroup("Contributions")
                        .whereEqualTo("userId", currentUserId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            Log.d(TAG, "Tab 2 loaded contributions count: " + queryDocumentSnapshots.size());

                            List<Contribution> contributions = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                contributions.add(doc.toObject(Contribution.class));
                            }

                            if (contributions.isEmpty()) {
                                contributionAdapter.setItems(new ArrayList<>());
                                showEmpty(true);
                                return;
                            }

                            // Mới nhất lên đầu
                            Collections.sort(contributions,
                                    (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                            // Lấy tên chiến dịch cho từng lượt góp
                            List<Task<DocumentSnapshot>> campaignTasks = new ArrayList<>();
                            for (Contribution c : contributions) {
                                String campaignId = c.getCampaignId();
                                if (campaignId == null || campaignId.isEmpty()) {
                                    campaignTasks.add(Tasks.<DocumentSnapshot>forResult(null));
                                } else {
                                    campaignTasks.add(db.collection("Campaigns").document(campaignId).get());
                                }
                            }

                            Tasks.whenAllComplete(campaignTasks).addOnSuccessListener(done -> {
                                if (!isAdded()) return;

                                List<ContributionAdapter.Item> items = new ArrayList<>();
                                for (int i = 0; i < contributions.size(); i++) {
                                    Task<DocumentSnapshot> task = campaignTasks.get(i);
                                    String title = null;
                                    if (task.isSuccessful() && task.getResult() != null
                                            && task.getResult().exists()) {
                                        title = task.getResult().getString("title");
                                    }
                                    items.add(new ContributionAdapter.Item(contributions.get(i), title));
                                }
                                contributionAdapter.setItems(items);
                                showEmpty(items.isEmpty());
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Tab 2 error: " + e.getMessage());
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Lỗi tải đơn đã góp: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            showEmpty(true);
                        });
                break;
        }
    }
}