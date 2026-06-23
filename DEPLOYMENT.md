# Hướng dẫn Deploy lên môi trường web FREE

Mục tiêu: đưa ứng dụng lên Internet **miễn phí**, dùng **Render** (chạy app từ Dockerfile) + **MySQL free** (freedb.tech hoặc Railway).

> ⚠️ Mọi nền tảng đều yêu cầu **bạn đăng nhập tài khoản của bạn** — đây là bước xác thực bắt buộc, không thể tự động hoàn toàn. Các bước dưới đây chỉ mất ~10 phút.

---

## Bước 0 — Đẩy code lên GitHub (nền tảng cho auto-deploy)

```bash
cd D:/webproject
git init
git add .
git commit -m "Cafe Inventory Management System"
gh auth login           # đăng nhập GitHub 1 lần
gh repo create cafe-inventory --public --source=. --push
```

> Mỗi lần `git push`, Render sẽ **tự build & deploy lại** (auto-deploy).

---

## Bước 1 — Tạo MySQL free

### Cách A: freedb.tech (đơn giản nhất)
1. Vào https://freedb.tech → đăng ký → tạo database.
2. Ghi lại: **host**, **database name**, **username**, **password** (cổng 3306).
3. Chuỗi `DB_URL` sẽ là:
   ```
   jdbc:mysql://<host>:3306/<dbname>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   ```

### Cách B: Railway MySQL
1. https://railway.app → New Project → **Provision MySQL**.
2. Tab **Variables** lấy host/port/user/password/database → ghép vào `DB_URL` tương tự.

---

## Bước 2 — Deploy app lên Render

1. Vào https://render.com → đăng nhập (GitHub).
2. **New +** → **Blueprint** → chọn repo `cafe-inventory`.
   Render đọc file [`render.yaml`](render.yaml) sẵn có.
3. Điền các biến môi trường khi được hỏi:
   | Biến | Giá trị |
   |------|---------|
   | `DB_URL` | chuỗi JDBC ở Bước 1 |
   | `DB_USERNAME` | user MySQL |
   | `DB_PASSWORD` | password MySQL |
   | `ADMIN_PASSWORD` | mật khẩu admin bạn muốn |
   | `JWT_SECRET` | để Render tự sinh (generateValue) |
4. **Apply** → Render build Docker image và chạy. Lần đầu mất ~5–8 phút.
5. Xong, app chạy tại `https://cafe-inventory-xxxx.onrender.com` → đăng nhập `admin`.

> Flyway sẽ tự tạo bảng + seed trên MySQL free khi app khởi động lần đầu. Không cần thao tác SQL thủ công.

---

## Bước 3 — Kiểm tra

- Mở URL Render → đăng nhập.
- Vào **Bán hàng** → nhập `Coffee Milk = 100` → xem nguyên liệu bị trừ.
- `/.../swagger-ui.html` để xem API.

---

## Lưu ý gói free
- **Render free** "ngủ" sau ~15 phút không truy cập; request đầu tiên sau đó khởi động lại (~30–50s). Đây là giới hạn của gói miễn phí.
- **freedb.tech** giới hạn dung lượng/kết nối — đủ cho demo/quán nhỏ.
- Đặt `DB_POOL_SIZE=2` (biến môi trường) nếu MySQL free giới hạn số kết nối.

---

## Phương án không dùng GitHub
Render vẫn cần 1 nguồn: hoặc Git repo (GitHub/GitLab/Bitbucket), hoặc một **Docker image công khai**. Nếu không muốn GitHub:
1. Cài Docker, build & push image lên Docker Hub:
   ```bash
   docker build -t <dockerhubuser>/cafe-inventory:1.0 .
   docker push <dockerhubuser>/cafe-inventory:1.0
   ```
2. Render → New → **Web Service** → **Deploy an existing image** → nhập tên image → set biến `DB_*`.

Dù cách nào, bạn vẫn phải đăng nhập tài khoản Render/Docker Hub của mình.
