# BÀI TẬP TUẦN 10: XÁC THỰC PHÂN QUYỀN JWT VỚI THƯ VIỆN NIMBUS JOSE + JWT

- **Họ và tên:** Bùi Thanh Phúc
- **Mã số sinh viên (MSSV):** 24133045
- **Môn học:** Lập trình Java / Công nghệ Java Web
- **Đề tài:** Triển khai bài tập ví dụ trong bài giảng `04_JWT.pdf` (trang 15 – 34), thay thế hoàn toàn thư viện JJWT (`io.jsonwebtoken`) bằng **Nimbus JOSE + JWT** (phiên bản `10.10`).
- **Kho lưu trữ GitHub:** [https://github.com/Phucsbinz/24133045_BuiThanhPhuc_BTap10.git](https://github.com/Phucsbinz/24133045_BuiThanhPhuc_BTap10.git)

---

## 1. Giới thiệu tổng quan

Dự án hiện thực trọn vẹn mô hình xác thực và phân quyền bằng JSON Web Token (JWT) theo kiến trúc RESTful kết hợp giao diện Web (Thymeleaf + AJAX):
- **Thay thế JJWT bằng Nimbus JOSE + JWT:** Sử dụng bộ API chuẩn `JWTClaimsSet.Builder`, `SignedJWT`, `MACSigner` (ký HMAC-SHA256) và `MACVerifier` (kiểm tra chữ ký, thuật toán và claims).
- **Kiến trúc phi trạng thái (Stateless):** Cấu hình Spring Security 7 với `SessionCreationPolicy.STATELESS`, không sử dụng session cookie (JSESSIONID). Xác thực hoàn toàn thông qua header `Authorization: Bearer <token>`.
- **Bảo mật mật khẩu:** Mã hóa BCrypt trước khi lưu MySQL, sử dụng DTO `UserResponse` trả về client để không làm lộ trường hash mật khẩu.
- **Giao diện người dùng trực quan:** Trang đăng nhập, đăng ký mở rộng, trang hồ sơ cá nhân và bảng danh sách người dùng được tải động bằng jQuery AJAX.
- **Quản lý cấu hình an toàn:** File `.env` chứa thông tin nhạy cảm (mật khẩu DB, JWT secret) được bảo vệ bằng `.gitignore`, cung cấp sẵn `.env.example` làm mẫu cấu hình.

---

## 2. Yêu cầu môi trường (Prerequisites)

Trước khi chạy dự án, máy tính cần cài đặt sẵn:
- **Java Development Kit (JDK):** Java 21 hoặc mới hơn (dự án được phát triển và kiểm thử trên Java 26 và Java 21).
- **MySQL Server:** Phiên bản 8.0 trở lên đang hoạt động tại cổng mặc định `3306`.
- **Git:** Để tải mã nguồn về máy.

---

## 3. Hướng dẫn tải về và khởi chạy (Dành cho Giảng viên / Người chấm)

### Cách 1: Khởi chạy nhanh bằng script PowerShell (Khuyến nghị trên Windows)

1. **Mở terminal và clone mã nguồn về máy:**
   ```powershell
   git clone https://github.com/Phucsbinz/24133045_BuiThanhPhuc_BTap10.git
   cd 24133045_BuiThanhPhuc_BTap10
   ```

2. **Chạy script chuẩn bị môi trường:**
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\setup.ps1
   ```
   *Script sẽ tự động tạo file `.env` với khóa bí mật HMAC 32-byte ngẫu nhiên và tạo sẵn các database `tuan10_jwt_nimbus`, `tuan10_jwt_nimbus_test` trên MySQL.*
   *(Nếu mật khẩu MySQL của bạn khác `root`, bạn có thể mở file `.env` vừa sinh ra để cập nhật `DB_PASSWORD`).*

3. **Chạy ứng dụng:**
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\run.ps1
   ```
   *Ứng dụng sẽ build và khởi động tại: **`http://localhost:8005/login`**.*

---

### Cách 2: Khởi chạy thủ công từng bước (Dành cho mọi hệ điều hành: Windows, Linux, macOS)

1. **Khởi tạo cơ sở dữ liệu trên MySQL:**
   - Mở MySQL Workbench, DBeaver hoặc MySQL CLI.
   - Mở và thực thi toàn bộ nội dung file [database/schema.sql](database/schema.sql).
   - Thao tác này tạo ra 2 database: `tuan10_jwt_nimbus` (dùng cho ứng dụng) và `tuan10_jwt_nimbus_test` (dùng cho kiểm thử tự động).

2. **Tạo file cấu hình môi trường `.env`:**
   - Sao chép file `.env.example` thành file `.env`:
     ```bash
     cp .env.example .env
     ```
   - Mở file `.env` và kiểm tra lại thông tin kết nối MySQL:
     ```properties
     SERVER_PORT=8005
     DB_URL=jdbc:mysql://localhost:3306/tuan10_jwt_nimbus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
     DB_USERNAME=root
     DB_PASSWORD=mật_khẩu_mysql_của_bạn
     JWT_SECRET=4q82vUQp3bXZ3Z4f2lO9kX7jM5qW1rT8yU6iO4pA2sD=
     JWT_EXPIRATION=3600000
     ```

3. **Build và chạy ứng dụng:**
   - **Trên Windows:**
     ```cmd
     .\mvnw.cmd spring-boot:run
     ```
   - **Trên Linux / macOS:**
     ```bash
     chmod +x mvnw
     ./mvnw spring-boot:run
     ```
   - **Hoặc đóng gói file JAR:**
     ```bash
     ./mvnw clean package -DskipTests
     java -jar target/24133045_BuiThanhPhuc_JWT_Nimbus-1.0.0.jar
     ```

---

## 4. Hướng dẫn kiểm thử và trải nghiệm ứng dụng

Sau khi khởi chạy ứng dụng thành công, truy cập trình duyệt tại:
👉 **[http://localhost:8005/login](http://localhost:8005/login)**

### Kịch bản kiểm thử giao diện trực quan (UI E2E Flow):

1. **Đăng ký tài khoản mới:**
   - Tại trang đăng nhập, nhấp vào mục **"Tạo tài khoản mới"** để mở rộng form đăng ký.
   - Nhập thông tin:
     - Họ tên: `Nguyễn Văn A`
     - Email: `nguyenvana@example.com`
     - Mật khẩu: `123456` (tối thiểu 6 ký tự).
   - Nhấn **"Đăng ký"**.
   - **Kết quả:** Hiển thị thông báo màu xanh *"Đăng ký thành công. Bạn có thể đăng nhập."*, đồng thời email được tự động điền sẵn vào ô đăng nhập.

2. **Đăng nhập và nhận JWT:**
   - Nhập mật khẩu `123456` và nhấn **"Đăng nhập"**.
   - **Kết quả:** Ứng dụng gửi request đến `/auth/login`, nhận token JWT, lưu vào `localStorage` với khóa `tuan10.jwt.token` và chuyển hướng đến trang `/user/profile`.

3. **Xem thông tin hồ sơ cá nhân (`/users/me`):**
   - Trang hồ sơ tự động gọi API `/users/me` với header `Authorization: Bearer <token>`.
   - **Kết quả:** Hiển thị tên `Nguyễn Văn A`, email, ảnh đại diện mặc định và chi tiết JSON trả về từ REST API.

4. **Xem danh sách người dùng (`/users`):**
   - Nhấn nút **"Xem danh sách người dùng"**.
   - **Kết quả:** Bảng người dùng hiển thị danh sách tất cả các tài khoản đã đăng ký trong hệ thống.

5. **Đăng xuất:**
   - Nhấn nút **"Đăng xuất"**.
   - **Kết quả:** Token JWT trong `localStorage` được xóa sạch, người dùng được điều hướng về trang đăng nhập `/login`.

6. **Kiểm tra cơ chế bảo vệ đường dẫn (Route Guard):**
   - Khi đã đăng xuất, thử truy cập trực tiếp vào URL: `http://localhost:8005/user/profile`.
   - **Kết quả:** Giao diện tự động phát hiện không có token hợp lệ và chuyển hướng ngay lập tức về trang `/login`.

---

## 5. Danh sách REST API và Kiểm thử Postman

File cấu hình Postman có sẵn tại: `postman/JWT_Nimbus.postman_collection.json`.

| STT | Phương thức | Đường dẫn URL | Quyền truy cập | Mô tả & Header |
| :---: | :---: | :--- | :---: | :--- |
| **1** | `POST` | `/auth/signup` | Public | Đăng ký tài khoản mới.<br>Body JSON: `{"fullName", "email", "password"}` |
| **2** | `POST` | `/auth/login` | Public | Đăng nhập lấy JWT Token.<br>Body JSON: `{"email", "password"}`<br>Trả về: `{"token": "...", "expiresIn": 3600000}` |
| **3** | `GET` | `/users/me` | Bearer Token | Lấy thông tin tài khoản hiện tại.<br>Header: `Authorization: Bearer <token>` |
| **4** | `GET` | `/users` | Bearer Token | Lấy danh sách tất cả người dùng.<br>Header: `Authorization: Bearer <token>` |
| **5** | `GET` | `/login` | Public | Trang giao diện đăng nhập và đăng ký (HTML) |
| **6** | `GET` | `/user/profile`| Public HTML | Trang giao diện xem hồ sơ và người dùng (HTML) |

---

## 6. Chạy bộ kiểm thử tự động (Unit & Integration Tests)

Dự án xây dựng bộ kiểm thử tích hợp toàn diện `JwtFlowIntegrationTest` bao phủ toàn bộ các trường hợp nghiệp vụ và bảo mật:
- Đăng ký tài khoản mới và kiểm tra mã hóa mật khẩu BCrypt.
- Đăng nhập, cấp token Nimbus hợp lệ, kiểm tra thời hạn token.
- Gọi API `/users/me` và `/users` với Bearer token (kiểm tra không sinh `JSESSIONID`).
- Chặn các yêu cầu không có token hoặc token sai định dạng (HTTP 401).
- Chặn token hết hạn, token sai chữ ký số HMAC, token sai thuật toán, token thiếu subject (HTTP 401).
- Xử lý trùng lặp email (HTTP 409 Conflict) và dữ liệu không hợp lệ (HTTP 400 Bad Request).
- Xử lý tài khoản bị khóa (HTTP 403 Forbidden).

**Lệnh chạy kiểm thử:**
```powershell
.\mvnw.cmd clean test
```
*(Kết quả: Tất cả các bài kiểm thử đều đạt - BUILD SUCCESS).*

---

## 7. Đối chiếu với nội dung bài giảng `04_JWT.pdf`

| Trang Slide | Nội dung trong bài giảng | Hiện thực trong dự án (Nimbus) |
| :---: | :--- | :--- |
| **15** | Khai báo Dependencies | `pom.xml`: Thay thế 3 module JJWT bằng `com.nimbusds:nimbus-jose-jwt:10.10` |
| **16** | Entity User implements UserDetails | `vn.iotstar.entity.User` |
| **17** | Các Model DTO | `RegisterUserModel`, `LoginUserModel`, `LoginResponse`, `UserResponse` |
| **18 – 19** | Repository & Services | `UserRepository`, `UserService`, `AuthenticationService` |
| **20 – 21** | JWT Service & Cấu hình DB | `JwtService` (dùng Nimbus `JWTClaimsSet`, `SignedJWT`, `MACSigner`, `MACVerifier`) |
| **22** | ApplicationConfiguration | `configs.ApplicationConfiguration` (Bean `BCryptPasswordEncoder`, `DaoAuthenticationProvider`) |
| **23** | JWT Authentication Filter | `filter.JwtAuthenticationFilter` (Trích xuất Bearer token, thẩm thực và đặt vào `SecurityContext`) |
| **24 – 25** | Security & CORS Configuration | `configs.SecurityConfiguration` (`STATELESS`, CSRF disable, CORS cho localhost:8005) |
| **26 – 27** | REST Controllers | `controllers.AuthenticationController`, `controllers.UserController` |
| **28 – 29** | Kiểm thử API Postman | `postman/JWT_Nimbus.postman_collection.json` & `JwtFlowIntegrationTest` |
| **30 – 31** | Global Exception Handling | `controllers.GlobalExceptionHandler` (Chuẩn hóa lỗi `ProblemDetail` RFC 7807) |
| **32 – 34** | Giao diện HTML & jQuery AJAX | `templates/login.html`, `templates/profile.html`, `static/js/mainjs.js` |

---

## 8. Cấu trúc thư mục dự án

```text
24133045_BuiThanhPhuc_JWT_Nimbus/
├── .env.example                               # File mẫu cấu hình biến môi trường
├── .gitignore                                 # Bảo mật thông tin nhạy cảm (.env, target, keys)
├── database/
│   └── schema.sql                             # DDL khởi tạo cơ sở dữ liệu MySQL
├── pom.xml                                    # Cấu hình Maven & Nimbus JOSE + JWT 10.10
├── postman/
│   └── JWT_Nimbus.postman_collection.json     # Bộ test Postman API
├── run.ps1                                    # Script chạy nhanh ứng dụng
├── setup.ps1                                  # Script tự động cấu hình môi trường
├── src/
│   ├── main/
│   │   ├── java/vn/iotstar/
│   │   │   ├── Application.java
│   │   │   ├── configs/                       # Cấu hình Security, CORS, Authentication
│   │   │   ├── controllers/                   # REST API & MVC Controllers, Exception Handler
│   │   │   ├── entity/                        # User Entity implements UserDetails
│   │   │   ├── filter/                        # JwtAuthenticationFilter
│   │   │   ├── models/                        # Request/Response DTOs
│   │   │   ├── repository/                    # UserRepository (JPA)
│   │   │   └── services/                      # JwtService (Nimbus), AuthenticationService, UserService
│   │   └── resources/
│   │       ├── application.properties         # Cấu hình Spring Boot
│   │       ├── static/                        # CSS, Avatar SVG, JavaScript (jQuery AJAX)
│   │       └── templates/                     # Thymeleaf HTML (login.html, profile.html)
│   └── test/
│       ├── java/vn/iotstar/
│       │   └── JwtFlowIntegrationTest.java    # Bộ kiểm thử tích hợp toàn diện
│       └── resources/
│           └── application-test.properties    # Cấu hình môi trường test
└── README.md                                  # Hướng dẫn chi tiết dự án
```

---

## 9. Chính sách bảo mật (.gitignore)

Dự án cam kết tuân thủ các nguyên tắc an toàn thông tin:
- Mọi file chứa bí mật hệ thống như `.env`, `.env.*` (ngoại trừ `.env.example`), các chứng chỉ số `*.key`, `*.pem`, `*.jks` đều được đưa vào `.gitignore`.
- Thư mục sinh mã `target/`, các tệp tạm thời của IDE (`.idea/`, `.vscode/`, `*.iml`) hoàn toàn không được commit lên kho chứa GitHub.
