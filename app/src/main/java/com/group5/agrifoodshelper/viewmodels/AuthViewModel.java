
package com.group5.agrifoodshelper.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.group5.agrifoodshelper.data.remote.FirebaseManager;
import com.group5.agrifoodshelper.models.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;

public class AuthViewModel extends ViewModel {

    private final FirebaseManager firebaseManager;

    private final MutableLiveData<User> currentUser =
            new MutableLiveData<>();

    private final MutableLiveData<String> errorMessage =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    public AuthViewModel() {
        firebaseManager = FirebaseManager.getInstance();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    // Nhận ID token từ Google Sign-In của Activity
    public void loginWithGoogle(String idToken) {
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null);
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        com.google.firebase.auth.FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // 1. Truy vấn Firestore xem hồ sơ đã tồn tại chưa
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("Users") // Lưu ý đổi tên collection thành hằng số của bạn (VD: Constants.COLL_USERS) nếu có
                                    .document(firebaseUser.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            // 2a. Đã có hồ sơ -> Đăng nhập thẳng vào
                                            User user = documentSnapshot.toObject(User.class);
                                            currentUser.postValue(user);
                                        } else {
                                            // 2b. Lần đầu đăng nhập bằng Google -> Tự động tạo hồ sơ mới trên Firestore
                                            String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Người dùng Google";
                                            User newUser = new User(
                                                    firebaseUser.getUid(),
                                                    name,
                                                    "", // Số điện thoại để trống do Google không cấp sẵn
                                                    "SUPPORTER", // Role mặc định, thay bằng Constants.ROLE_SUPPORTER nếu có
                                                    100.0 // Điểm mặc định
                                            );

                                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                    .collection("Users")
                                                    .document(newUser.getUserId())
                                                    .set(newUser)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Báo cho giao diện chuyển vào trang chủ
                                                        currentUser.postValue(newUser);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        errorMessage.postValue("Lỗi tạo hồ sơ Google: " + e.getMessage());
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        errorMessage.postValue("Lỗi tải dữ liệu người dùng: " + e.getMessage());
                                    });
                        }
                    } else {
                        errorMessage.postValue("Đăng nhập Google thất bại.");
                    }
                });
    }

    // Đọc hồ sơ và role từ users/{uid}
    private void loadUserRole(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();

        firebaseManager.getFirestore()
                .collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    loading.setValue(false);

                    if (!task.isSuccessful()) {
                        errorMessage.setValue(
                                "Không thể tải hồ sơ người dùng."
                        );
                        return;
                    }

                    DocumentSnapshot document = task.getResult();

                    if (document == null || !document.exists()) {
                        errorMessage.setValue(
                                "Tài khoản chưa có hồ sơ trong hệ thống."
                        );
                        return;
                    }

                    User user = document.toObject(User.class);

                    if (user == null) {
                        errorMessage.setValue(
                                "Dữ liệu hồ sơ không hợp lệ."
                        );
                        return;
                    }

                    String role = user.getRole();

                    if (role == null ||
                            !(role.equals("FARMER")
                                    || role.equals("SUPPORTER")
                                    || role.equals("DRIVER"))) {
                        errorMessage.setValue(
                                "Vai trò tài khoản không hợp lệ."
                        );
                        return;
                    }

                    if (user.getUserId() == null
                            || user.getUserId().isEmpty()) {
                        user.setUserId(uid);
                    }

                    currentUser.setValue(user);
                });
    }

    public void logout() {
        firebaseManager.getAuth().signOut();
        currentUser.setValue(null);
        errorMessage.setValue(null);
    }

    public void registerWithEmail(String email, String password, String fullName, String phone) {
        com.google.firebase.auth.FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        com.google.firebase.auth.FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // Tạo hồ sơ người dùng mới
                            User newUser = new User(
                                    firebaseUser.getUid(),
                                    fullName,
                                    phone,
                                    com.group5.agrifoodshelper.utils.Constants.ROLE_SUPPORTER,
                                    100.0 // Điểm uy tín mặc định
                            );

                            // Lưu thông tin vào Firestore
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection(com.group5.agrifoodshelper.utils.Constants.COLL_USERS)
                                    .document(newUser.getUserId())
                                    .set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        // Báo cho giao diện biết đã đăng ký và lưu thành công
                                        currentUser.postValue(newUser);
                                    })
                                    .addOnFailureListener(e -> {
                                        errorMessage.postValue("Lỗi lưu hồ sơ: " + e.getMessage());
                                    });
                        }
                    } else {
                        errorMessage.postValue("Đăng ký thất bại: " + task.getException().getMessage());
                    }
                });
    }

    public void loginWithEmail(String email, String password) {
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        com.google.firebase.auth.FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            // Truy vấn Firestore để lấy Role và thông tin chi tiết của User
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection(com.group5.agrifoodshelper.utils.Constants.COLL_USERS)
                                    .document(firebaseUser.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            User user = documentSnapshot.toObject(User.class);
                                            // Đẩy user lên để giao diện tự động chuyển trang
                                            currentUser.postValue(user);
                                        } else {
                                            errorMessage.postValue("Lỗi: Tài khoản chưa được thiết lập dữ liệu hồ sơ.");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        errorMessage.postValue("Lỗi tải dữ liệu người dùng: " + e.getMessage());
                                    });
                        }
                    } else {
                        errorMessage.postValue("Đăng nhập thất bại: Sai email hoặc mật khẩu.");
                    }
                });
    }
}
