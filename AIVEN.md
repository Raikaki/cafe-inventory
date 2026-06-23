# Deploy với MySQL FREE trên Aiven

Aiven cung cấp gói **MySQL Free** (1 CPU / 1GB RAM / 5GB) — đủ cho quán cafe nhỏ và demo.
Kết hợp: **DB trên Aiven** + **App trên Render** (cũng free). Tổng chi phí: 0đ.

---

## Bước 1 — Tạo MySQL free trên Aiven

1. Vào https://aiven.io → **Sign up** (Google/GitHub).
2. **Create service** → chọn **MySQL**.
3. Plan: chọn **Free** (Startup-1 / Free plan). Cloud & region: chọn gần bạn (vd. `google-asia-southeast1`).
4. Bấm **Create service**, đợi ~2–3 phút tới khi trạng thái **Running**.
5. Mở service → tab **Overview / Connection information**, ghi lại:

   | Thông tin | Ví dụ |
   |-----------|-------|
   | Host | `mysql-xxxxx.aivencloud.com` |
   | Port | `12345` (Aiven cấp ngẫu nhiên, **không phải 3306**) |
   | User | `avnadmin` |
   | Password | (bấm *show*) |
   | Database name | `defaultdb` |

> Aiven **bắt buộc SSL** và `defaultdb` đã tồn tại sẵn — không cần tạo database thủ công.

---

## Bước 2 — Chuỗi kết nối (JDBC)

Ghép theo mẫu (thay HOST và PORT):

```
DB_URL=jdbc:mysql://<HOST>:<PORT>/defaultdb?sslMode=REQUIRED&serverTimezone=UTC
DB_USERNAME=avnadmin
DB_PASSWORD=<password Aiven>
DB_POOL_SIZE=3
```

- `sslMode=REQUIRED` → mã hoá SSL nhưng không cần import chứng chỉ CA (đơn giản nhất, chạy ngay).
- `DB_POOL_SIZE=3` → tránh vượt giới hạn kết nối của gói free.

Flyway sẽ **tự tạo toàn bộ bảng + seed dữ liệu** vào `defaultdb` ở lần khởi động đầu tiên.

---

## Bước 3 — Đưa app lên Render (build Docker, trỏ vào Aiven)

1. Đẩy code lên GitHub:
   ```bash
   cd D:/webproject
   git init && git add . && git commit -m "Cafe Inventory"
   gh auth login
   gh repo create cafe-inventory --public --source=. --push
   ```
2. https://render.com → **New +** → **Blueprint** → chọn repo (đọc `render.yaml`).
3. Khi Render hỏi biến môi trường, dán:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (từ Bước 2)
   - `ADMIN_PASSWORD` = mật khẩu admin bạn muốn
   - `JWT_SECRET` để Render tự sinh
4. **Apply** → đợi build (~5–8 phút) → mở URL `https://cafe-inventory-xxxx.onrender.com` → đăng nhập `admin`.

---

## (Tuỳ chọn) Chạy thử local trỏ vào Aiven

Nếu sau này bạn cài JDK 21 + Maven, có thể chạy local trỏ thẳng Aiven:

```bash
# PowerShell
$env:DB_URL="jdbc:mysql://<HOST>:<PORT>/defaultdb?sslMode=REQUIRED&serverTimezone=UTC"
$env:DB_USERNAME="avnadmin"
$env:DB_PASSWORD="<password>"
mvn spring-boot:run
```

Hoặc dùng Docker:
```bash
docker build -t cafe-inventory .
docker run -p 8080:8080 -e DB_URL=... -e DB_USERNAME=avnadmin -e DB_PASSWORD=... cafe-inventory
```

---

## Khắc phục sự cố

| Lỗi | Nguyên nhân / cách xử lý |
|-----|--------------------------|
| `Communications link failure` | Sai HOST/PORT, hoặc service Aiven chưa Running |
| `Access denied for user` | Sai password / user (phải là `avnadmin`) |
| `SSL connection required` | Thiếu `sslMode=REQUIRED` trong DB_URL |
| `Unknown database 'cafe_inventory'` | Aiven dùng `defaultdb`, sửa lại tên DB trong URL |
| `Too many connections` | Đặt `DB_POOL_SIZE=2` hoặc `3` |
| Flyway báo `validate` lỗi | Xoá dữ liệu cũ: trong Aiven console mở SQL, `DROP DATABASE defaultdb; CREATE DATABASE defaultdb;` rồi deploy lại |
