# SMD - Hệ thống Quản lý và Số hóa Giáo trình

## Giới thiệu

**SMD (Syllabus Management and Digitalization)** là hệ thống quản lý và số hóa giáo trình dành cho các trường đại học. Hệ thống hỗ trợ quy trình xây dựng, phê duyệt và công bố giáo trình theo nhiều cấp, từ giảng viên đến ban giám hiệu.

### Tính năng chính

- **Quản lý giáo trình** — Giảng viên tạo, chỉnh sửa và nộp giáo trình; hỗ trợ versioning (nhiều phiên bản)
- **Quy trình phê duyệt 2 cấp** — Trưởng khoa (HoD) duyệt cấp 1, Phòng Đào tạo (AA) duyệt cấp 2
- **Công bố giáo trình** — Admin/Hiệu trưởng xuất bản giáo trình để sinh viên tra cứu
- **Tìm kiếm công khai** — Sinh viên và khách truy cập tìm kiếm giáo trình đã công bố
- **Theo dõi & Phản hồi** — Sinh viên follow giáo trình và gửi phản hồi
- **Hệ thống thông báo** — Thông báo real-time khi có cập nhật hoặc phê duyệt
- **Bình luận cộng tác** — Giảng viên và quản lý trao đổi trực tiếp trên giáo trình
- **AI Summary** — Tóm tắt giáo trình tự động bằng tiếng Việt
- **AI So sánh phiên bản** — So sánh 2 phiên bản giáo trình bằng Gemini AI
- **Ma trận CLO-PLO** — Xây dựng và hiển thị ma trận ánh xạ chuẩn đầu ra
- **Sơ đồ lộ trình môn học** — Trực quan hóa môn tiên quyết và môn kế tiếp

### Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 17, Spring Boot 3.2.5, Spring Security (JWT) |
| Database | MySQL 8 |
| Frontend | ReactJS, React Router, Vite |
| AI | Google Gemini API (gemini-1.5-flash) |

---

## Yêu cầu cài đặt

### Java 17

**macOS:**
```bash
brew install openjdk@17
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
java -version
```

**Windows:**
1. Tải JDK 17 tại [https://adoptium.net](https://adoptium.net)
2. Chạy file `.msi`, chọn **Add to PATH** trong quá trình cài
3. Kiểm tra: mở Command Prompt → `java -version`

---

### Maven

**macOS:**
```bash
brew install maven
mvn -version
```

**Windows:**
1. Tải Maven tại [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) (chọn Binary zip)
2. Giải nén vào `C:\Program Files\Maven`
3. Thêm `C:\Program Files\Maven\bin` vào biến môi trường `PATH`
4. Kiểm tra: `mvn -version`

---

### MySQL 8

**macOS:**
```bash
brew install mysql
brew services start mysql
mysql_secure_installation   # Đặt password cho root
```

**Windows:**
1. Tải MySQL Installer tại [https://dev.mysql.com/downloads/installer](https://dev.mysql.com/downloads/installer)
2. Chọn **MySQL Server 8.x** và cài đặt
3. Đặt password cho tài khoản `root` trong quá trình cài
4. MySQL sẽ tự chạy dưới dạng Windows Service

---

### Node.js 18+

**macOS:**
```bash
brew install node
node -v   # Kiểm tra >= 18
npm -v
```

**Windows:**
1. Tải Node.js LTS tại [https://nodejs.org](https://nodejs.org)
2. Chạy file `.msi` và cài đặt (tự thêm vào PATH)
3. Kiểm tra: `node -v` và `npm -v`

---

## Hướng dẫn chạy

### Bước 1: Clone project

```bash
git clone https://github.com/Sozy142/smd-project.git
cd smd-project
```

---

### Bước 2: Cấu hình Database

1. Mở MySQL Workbench hoặc terminal, tạo database:

```sql
CREATE DATABASE smd_db;
```

2. Mở file `backend/src/main/resources/application.properties`

3. Sửa dòng password thành password MySQL của máy bạn:

```properties
spring.datasource.password=your_mysql_password
```

---

### Bước 3: Chạy Backend

```bash
cd backend
mvn spring-boot:run
```

- Server chạy tại **http://localhost:8080**
- Lần đầu chạy, Hibernate sẽ **tự động tạo toàn bộ bảng** trong `smd_db`
- `DataSeeder` sẽ **tự động tạo tài khoản demo** nếu database trống

---

### Bước 4: Chạy Frontend

Mở terminal mới (giữ nguyên terminal backend):

```bash
cd frontend
npm install
npm run dev
```

- Mở trình duyệt tại **http://localhost:3000**

---

## Tài khoản demo

| Email | Password | Vai trò |
|---|---|---|
| admin@smd.edu | admin123 | Admin |
| lecturer@smd.edu | lecturer123 | Lecturer (Giảng viên) |
| hod@smd.edu | hod123 | Head of Department (Trưởng khoa) |
| aa@smd.edu | aa123 | Academic Affairs (Phòng Đào tạo) |
| principal@smd.edu | principal123 | Principal (Hiệu trưởng) |
| student@smd.edu | student123 | Student (Sinh viên) |

---

## Luồng demo chính

```
1. Đăng nhập Lecturer  →  Tạo giáo trình  →  Submit
        ↓
2. Đăng nhập HoD       →  Duyệt cấp 1 (Approve)
        ↓
3. Đăng nhập AA        →  Duyệt cấp 2 (Level 2 Approve)
        ↓
4. Đăng nhập Admin     →  Publish giáo trình
        ↓
5. Đăng nhập Student   →  Tìm kiếm  →  Xem chi tiết  →  Follow  →  Feedback
```

---

## Lưu ý

- **Backend phải chạy trước** khi chạy Frontend
- Nếu đổi password MySQL thì cập nhật lại `spring.datasource.password` trong `application.properties` và khởi động lại backend
- **Gemini AI key** cần được cập nhật trong `application.properties` để dùng tính năng AI So sánh phiên bản:
  ```properties
  gemini.api.key=YOUR_GEMINI_API_KEY_HERE
  ```
  Lấy API key miễn phí tại [https://aistudio.google.com](https://aistudio.google.com)
- Tính năng **AI Summary** hoạt động offline (không cần Gemini key), chỉ tính năng **AI So sánh phiên bản** mới cần key
