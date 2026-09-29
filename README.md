# 🌱 AgriFoodsHelper - Ứng Dụng Hỗ Trợ Giải Cứu Nông Sản


**AgriFoodsHelper** là đồ án cuối kỳ môn **Phát triển Ứng dụng Di động**, được xây dựng theo mô hình **Social Commerce (Mạng xã hội + Tin tức + Thương mại điện tử)**. Ứng dụng giúp kết nối trực tiếp nông dân đang có nông sản cần tiêu thụ khẩn cấp với cộng đồng người tiêu dùng, đồng thời tích hợp công cụ điều phối chiến dịch cho Quản trị viên (Admin) ngay trên giao diện bảng tin di động.

---

## 🌟 Điểm Nhấn Kỹ Thuật Trọng Tâm `[*]`

Dự án đáp ứng đầy đủ các tiêu chuẩn kỹ thuật cốt lõi của học phần:

1. **Xác thực & Khai thác API bên thứ ba (`Google Sign-In API` & `Firebase Auth`):**
   * Hỗ trợ đăng nhập nhanh một chạm bằng tài khoản Google và tự động phân quyền giao diện theo vai trò (`user` / `admin`).
2. **Cơ sở dữ liệu Đám mây thời gian thực (`Cloud Firestore` & `Firebase Storage`):**
   * Lưu trữ và đồng bộ tức thời bài đăng giải cứu, hình ảnh nông sản, bình luận, thông báo và trạng thái đơn hàng.
3. **Đồng bộ Dữ liệu Cục bộ / Hoạt động Ngoại tuyến (`SQLite - Room Database`):**
   * Áp dụng kiến trúc **Offline-First** thông qua `Repository Pattern`: Khi có mạng, dữ liệu từ Firestore tự động được lưu đệm (cache) xuống các bảng SQLite (`local_posts`, `saved_posts`, `local_cart`). Khi ngắt kết nối Internet, ứng dụng tự động chuyển sang đọc dữ liệu từ SQLite, cho phép người dùng tiếp tục xem các bài đăng đã tải, danh sách bài đã lưu và giỏ hàng.
4. **Thiết kế Giao diện Thích ứng (`Responsive UI` khi xoay màn hình):**
   * Hỗ trợ đa cấu hình màn hình (`res/layout` và `res/layout-land`). Khi xoay ngang thiết bị, Bảng tin tự động chuyển từ danh sách 1 cột (`LinearLayoutManager`) sang lưới 2 cột (`GridLayoutManager`), màn hình Giỏ hàng tách thành 2 khung song song và giữ nguyên dữ liệu nhờ `ViewModel`.
5. **Quản trị viên thao tác trực tiếp trên Mobile (In-Feed Admin Moderation):**
   * Admin sử dụng chung giao diện bảng tin di động với người dùng thường nhưng được mở khóa menu thao tác nhanh (`BottomSheetDialog`) ngay trên từng bài viết để *Duyệt bài, Ghim chiến dịch khẩn cấp, Ẩn/Xóa bài vi phạm* hoặc *Cập nhật trạng thái chiến dịch* tương tự trải nghiệm trên Facebook.

---

## 🧩 Phân Rã Module & Danh Sách 36 Chức Năng (FR01 – FR36)

Hệ thống gồm **36 chức năng** (19 chức năng Người dùng, 11 chức năng Quản trị viên, 6 chức năng Mở rộng) được chia thành **4 Module độc lập**:

### 🔹 Module 1: Tài khoản, Hồ sơ, Thông báo & Nhật ký Hoạt động
| Mã FR | Tên chức năng | Mô tả tóm tắt |
| :--- | :--- | :--- |
| **FR01** | Đăng ký tài khoản | Tạo tài khoản mới (Họ tên, Email, SĐT, Mật khẩu) kèm kiểm tra hợp lệ. |
| **FR02** | Đăng nhập | Đăng nhập bằng Email/Mật khẩu hoặc **Google Sign-In API `[*]`**. |
| **FR03** | Đăng xuất | Kết thúc phiên làm việc, xóa trạng thái tạm và quay về màn hình đăng nhập. |
| **FR04** | Quản lý thông tin cá nhân | Xem và cập nhật Họ tên, Ảnh đại diện, SĐT và Địa chỉ nhận hàng. |
| **FR19** | Nhận thông báo | Nhận thông báo về trạng thái duyệt bài, bình luận mới và cập nhật đơn hàng. |
| **FR20** | Truy cập chức năng quản trị | Nhận diện tài khoản có `role = "admin"` để mở khóa các công cụ điều phối trên Mobile. |
| **FR31** | Lưu bài đăng yêu thích *(Mở rộng)* | Lưu bài đăng quan tâm vào danh sách riêng (**SQLite `saved_posts`**) để xem lại cả khi Offline. |
| **FR34** | Xem lịch sử hoạt động *(Mở rộng)* | Xem lại lịch sử bài đăng đã tạo, bình luận đã gửi và đơn hàng đã đặt. |
| **FR35** | Thông báo chiến dịch mới *(Mở rộng)* | Tự động gửi thông báo khi có chiến dịch giải cứu nông sản mới được phê duyệt. |

### 🔹 Module 2: Bảng tin Giải cứu MXH, Tìm kiếm, Tương tác & Offline SQLite
| Mã FR | Tên chức năng | Mô tả tóm tắt |
| :--- | :--- | :--- |
| **FR05** | Xem bảng tin | Hiển thị danh sách chiến dịch giải cứu đã duyệt (ưu tiên bài ghim lên đầu) & hỗ trợ đọc **Offline từ SQLite `[*]`**. |
| **FR06** | Tìm kiếm nông sản | Tìm kiếm nhanh bài đăng theo tên nông sản hoặc từ khóa địa phương. |
| **FR07** | Lọc và sắp xếp bài đăng | Lọc theo loại nông sản, mức giá, trạng thái chiến dịch; sắp xếp theo thời gian hoặc giá. |
| **FR08** | Xem chi tiết bài đăng | Xem đầy đủ nội dung câu chuyện, hình ảnh, sản lượng còn lại, đơn giá, địa điểm và liên hệ. |
| **FR11** | Yêu thích bài đăng | Thả tim / Bỏ yêu thích chiến dịch giải cứu và cập nhật số lượt tim thời gian thực. |
| **FR12** | Bình luận bài đăng | Gửi bình luận trao đổi, xem danh sách bình luận và sửa/xóa bình luận cá nhân. |
| **FR32** | Chia sẻ bài đăng *(Mở rộng)* | Chia sẻ thông tin chiến dịch giải cứu qua các ứng dụng khác trên điện thoại (`Intent.ACTION_SEND`). |
| **FR33** | Báo cáo bài đăng *(Mở rộng)* | Gửi báo cáo bài viết có dấu hiệu sai sự thật hoặc vi phạm quy định cộng đồng cho Admin. |
| **FR36** | Thống kê kết quả giải cứu *(Mở rộng)* | Hiển thị thanh tiến độ (`ProgressBar`) số Kg nông sản đã được giải cứu / tổng sản lượng ngay trên bài đăng. |

### 🔹 Module 3: Đăng Chiến dịch & Kiểm duyệt Admin kiểu Facebook
| Mã FR | Tên chức năng | Mô tả tóm tắt |
| :--- | :--- | :--- |
| **FR09** | Đăng bài giải cứu nông sản | Nông dân tạo bài kêu gọi giải cứu kèm ảnh, sản lượng (kg), đơn giá, địa điểm (trạng thái `pending`). |
| **FR10** | Quản lý bài đăng cá nhân | Xem danh sách bài do mình tạo, chỉnh sửa thông tin hoặc xóa bài đăng. |
| **FR21** | Xem bài đăng chờ duyệt *(Admin)* | Xem danh sách các bài đăng mới tạo đang chờ kiểm duyệt (`status = "pending"`). |
| **FR22** | Duyệt bài đăng *(Admin)* | Phê duyệt bài viết hợp lệ để hiển thị công khai lên Bảng tin và phát thông báo **FR35**. |
| **FR23** | Từ chối bài đăng *(Admin)* | Từ chối bài không đạt yêu cầu kèm lý do cụ thể để người đăng chỉnh sửa. |
| **FR24** | Ghim bài đăng *(Admin)* | Ghim chiến dịch giải cứu khẩn cấp lên đầu Bảng tin qua menu `...` ngay trên bài viết. |
| **FR25** | Ẩn bài đăng *(Admin)* | Ẩn tạm thời bài đăng khỏi Bảng tin công khai để kiểm tra lại. |
| **FR26** | Xóa bài đăng vi phạm *(Admin)* | Xóa vĩnh viễn bài viết sai sự thật hoặc vi phạm tiêu chuẩn cộng đồng. |
| **FR27** | Quản lý bình luận vi phạm *(Admin)* | Xem xét và xóa trực tiếp các bình luận không phù hợp ngay trong chi tiết bài viết. |
| **FR28** | Quản lý trạng thái chiến dịch *(Admin)* | Cập nhật trạng thái chiến dịch: *Đang diễn ra*, *Đã giải cứu đủ sản lượng*, hoặc *Đã kết thúc*. |

### 🔹 Module 4: Thương mại điện tử, Thống kê Hệ thống & Giao diện Responsive
| Mã FR | Tên chức năng | Mô tả tóm tắt |
| :--- | :--- | :--- |
| **FR13** | Thêm nông sản vào giỏ hàng | Chọn số lượng Kg cần mua ủng hộ ngay từ bài đăng và lưu vào **SQLite `local_cart`**. |
| **FR14** | Quản lý giỏ hàng | Xem giỏ hàng (hỗ trợ Offline), tăng/giảm số Kg, xóa sản phẩm và tự động tính tổng tiền. |
| **FR15** | Đặt mua nông sản | Nhập thông tin người nhận, SĐT, địa chỉ, phương thức nhận hàng và tạo đơn hàng. |
| **FR16** | Xem lịch sử đơn hàng | Xem danh sách các đơn đã đặt (mã đơn, ngày đặt, sản phẩm, số lượng, tổng tiền). |
| **FR17** | Theo dõi trạng thái đơn hàng | Theo dõi đơn qua 5 trạng thái: *Chờ xác nhận → Đã xác nhận → Đang giao → Hoàn thành / Đã hủy*. |
| **FR18** | Hủy đơn hàng | Gửi yêu cầu hủy đơn khi đơn hàng đang ở trạng thái *Chờ xác nhận*. |
| **FR29** | Theo dõi hoạt động đơn hàng *(Admin)* | Admin xem toàn bộ đơn hàng hệ thống và cập nhật trạng thái điều phối đơn. |
| **FR30** | Thống kê hoạt động hệ thống *(Admin)* | Dashboard tổng quan: Tổng số bài đăng, chiến dịch đang chạy, tổng đơn hàng và tổng sản lượng đã giải cứu (**FR36**). |
---
<img width="432" height="810" alt="image" src="https://github.com/user-attachments/assets/f9bec1fb-8a42-4994-8ef8-5a18cdc2fbb1" />

