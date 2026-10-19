package com.group5.agrifoodshelper.repositories;

import androidx.lifecycle.MutableLiveData;
import com.group5.agrifoodshelper.models.User;
import com.group5.agrifoodshelper.utils.Constants;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {
    private FirebaseFirestore firestore;

    public AuthRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<User> checkAndSaveUser(FirebaseUser firebaseUser) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        if (firebaseUser == null) return userLiveData;

        String uid = firebaseUser.getUid();

        firestore.collection(Constants.COLL_USERS).document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            // User đã tồn tại, lấy thông tin về
                            User user = doc.toObject(User.class);
                            userLiveData.postValue(user);
                        } else {
                            // User mới lần đầu đăng nhập, tạo mới với quyền SUPPORTER
                            User newUser = new User(
                                    uid,
                                    firebaseUser.getDisplayName(),
                                    firebaseUser.getPhoneNumber() != null ? firebaseUser.getPhoneNumber() : "Đang cập nhật",
                                    Constants.ROLE_SUPPORTER,
                                    100.0  // BỔ SUNG THÊM THAM SỐ NÀY CHO ĐỦ 5
                            );

                            firestore.collection(Constants.COLL_USERS).document(uid)
                                    .set(newUser)
                                    .addOnSuccessListener(aVoid -> userLiveData.postValue(newUser));
                        }
                    }
                });

        return userLiveData;
    }
}