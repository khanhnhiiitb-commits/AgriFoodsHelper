
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
        if (idToken == null || idToken.isEmpty()) {
            errorMessage.setValue("Không lấy được Google ID token.");
            return;
        }

        loading.setValue(true);
        errorMessage.setValue(null);

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        firebaseManager.getAuth()
                .signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        loading.setValue(false);
                        errorMessage.setValue(
                                "Đăng nhập thất bại. Vui lòng thử lại."
                        );
                        return;
                    }

                    FirebaseUser firebaseUser =
                            firebaseManager.getAuth().getCurrentUser();

                    if (firebaseUser == null) {
                        loading.setValue(false);
                        errorMessage.setValue(
                                "Không tìm thấy tài khoản đăng nhập."
                        );
                        return;
                    }

                    loadUserRole(firebaseUser);
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
}
