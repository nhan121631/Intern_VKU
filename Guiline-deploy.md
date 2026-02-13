# DEPLOYMENT GUIDELINE

## 1. DEPLOYMENT GUIDELINE: REACT (TYPESCRIPT) ON VERCEL

### 1.1. Yêu cầu hệ thống (Prerequisites)

Trước khi bắt đầu, đảm bảo máy của bạn và dự án đáp ứng các yêu cầu sau:

- **Node.js**: Phiên bản v23.6.0
- **Package Manager**: npm, yarn hoặc pnpm
- **Source Code**: Đã được push lên GitHub
- **Vercel Account**: Đã có tài khoản tại vercel.com

### 1.2. Setup Môi trường Local

#### 1.2.1. Chạy dự án

Chạy lệnh `pnpm run dev` và truy cập http://localhost:5173 để kiểm tra.

#### 1.2.2. Kiểm tra Build

Trước khi push code, phải chạy lệnh build để kiểm tra lỗi TypeScript:

```bash
npm run build
```

Nếu lệnh này báo lỗi (TypeScript error, Lint error), quá trình deploy trên Vercel sẽ thất bại. Hãy fix hết lỗi trước khi push.

#### 1.2.3. Thêm file cấu hình Vercel

Tạo file `vercel.json` và thêm cấu hình để không bị lỗi 404:

```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

### 1.3. Setup Project trên Vercel

1. Đăng nhập **Vercel Dashboard**: vercel.com/dashboard
2. Chọn **Add New...** > **Project**
3. Kết nối với tài khoản Git và Import Repository tương ứng

#### 1.3.1. Cấu hình Build

Tại màn hình setup:

- **Framework Preset**: Chọn Vite
- **Root Directory**: ./frontend-job
- **Build Settings**: (Vercel thường tự điền đúng, nhưng hãy kiểm tra lại)
  - Build Command: `npm run build`
  - Output Directory: `dist`
  - Install Command: `npm install`

#### 1.3.2. Cấu hình Biến môi trường

Mở phần **Environment Variables** và import các key tương ứng từ file `.env` của dự án.

Setup cho cả 3 môi trường: Production, Preview (Staging), Development.

Nhấn **Deploy** để bắt đầu quá trình deploy.

---

## 2. DEPLOYMENT GUIDELINE: DOCKER SPRING BOOT ON AWS EC2

### 2.1. Kiến trúc & công nghệ sử dụng

- **Mã nguồn**: Java 21 (Spring Boot 4.0.1)
- **Đóng gói (Containerization)**: Docker & Docker Compose
- **Tự động hóa (CI/CD)**: GitHub Actions
- **Máy chủ (Server)**: AWS EC2 chạy Linux (Amazon Linux 2023)
- **Cơ sở dữ liệu**: MySQL latest (Chạy trong Docker)

### 2.2. Chuẩn bị Dockerfile

Chia làm 2 giai đoạn:

- **Stage 1 (Build)**: Dùng image gradle để biên dịch code thành file .jar
- **Stage 2 (Run)**: Sử dụng image Amazon Corretto JDK 21 để chạy ứng dụng tạo ra image cuối cùng để deploy (production)

#### Stage 1 - Build

- Sử dụng image Gradle với JDK 21 để build ứng dụng. Đặt tên stage là build:
  ```dockerfile
  FROM gradle:9.3.0-jdk21 AS build
  ```
- Đặt thư mục làm việc trong container là `/app`:
  ```dockerfile
  WORKDIR /app
  ```
- Copy toàn bộ mã nguồn từ máy host vào thư mục `/app` trong container:
  ```dockerfile
  COPY . .
  ```
- Chạy lệnh Gradle để build file jar của ứng dụng Spring Boot:
  ```dockerfile
  RUN gradle clean bootJar --no-daemon
  ```

#### Stage 2 - Run

- Sử dụng image Amazon Corretto JDK 21 để chạy ứng dụng:
  ```dockerfile
  FROM amazoncorretto:21.0.8
  ```
- Đặt lại thư mục làm việc là `/app` cho stage chạy:
  ```dockerfile
  WORKDIR /app
  ```
- Copy file jar đã build từ stage build sang stage chạy, đổi tên thành app.jar:
  ```dockerfile
  COPY --from=build /app/build/libs/*.jar app.jar
  ```
- Copy file `.env` vào thư mục `/app` trong container:
  ```dockerfile
  COPY .env /app/.env
  ```
- Mở port 8080 để ứng dụng lắng nghe (thường là port của Spring Boot 8080):
  ```dockerfile
  EXPOSE 8080
  ```
- Thiết lập lệnh khởi chạy ứng dụng khi container start:
  ```dockerfile
  ENTRYPOINT ["java","-jar","app.jar"]
  ```

### 2.3. Thiết lập quy trình tự động (GitHub Actions)

#### 2.3.1. File Cấu Hình `.github/workflows/deploy.yml`

```yaml
# Tên của workflow - sẽ hiển thị trong tab Actions của GitHub
name: Build and Push Docker Image

# Điều kiện kích hoạt workflow
on:
  push:
    # branches:
    #   - main
    # paths:
    #   - 'spring-ants-ktc/**'
    tags:
      # Chỉ chạy khi tạo tag bắt đầu bằng 'deployment'
      # Ví dụ: deployment-v1.0.0, deployment-prod, deployment-staging
      - "deployment*"

# Biến môi trường dùng chung cho toàn bộ workflow
env:
  # Tên Docker image sẽ được push lên Docker Hub
  DOCKER_IMAGE: nhan12163/tasks-management
  # Phiên bản Java sử dụng trong project
  JAVA_VERSION: "21"

# Định nghĩa các job (công việc) cần thực hiện
jobs:
  # Tên job: build-and-push
  build-and-push:
    # Chạy trên máy ảo Ubuntu phiên bản mới nhất
    runs-on: ubuntu-latest

    # Các bước thực hiện trong job
    steps:
      # Bước 1: Tải source code từ repository về máy ảo
      - name: Checkout code
        uses: actions/checkout@v4

      # Bước 2: Cài đặt Java Development Kit (JDK) phiên bản 21
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          # Sử dụng Amazon Corretto distribution của OpenJDK
          distribution: "corretto"

      # Bước 3: Cache (lưu trữ tạm) các gói Gradle để tăng tốc build
      - name: Cache Gradle packages
        uses: actions/cache@v4
        with:
          # Đường dẫn các thư mục cần cache
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          # Key để định danh cache dựa trên OS và hash của file gradle
          key: ${{ runner.os }}-gradle-${{ hashFiles('job/**/*.gradle*', 'job/**/gradle-wrapper.properties') }}
          # Fallback keys nếu không tìm thấy cache chính xác
          restore-keys: |
            ${{ runner.os }}-gradle-

      # Bước 4: Cấp quyền thực thi cho file gradlew (Gradle Wrapper)
      - name: Make gradlew executable
        run: chmod +x ./job/gradlew

      # Bước 5: Tạo file .env từ GitHub Secrets
      - name: Create .env file from secrets
        run: |
          echo "BREVO_API_KEY=${{ secrets.BREVO_API_KEY }}" >> ./job/.env
          echo "BREVO_API_URL=${{ secrets.BREVO_API_URL }}" >> ./job/.env

      # Bước 6: Chạy unit tests (hiện tại đang bị comment)
      # Nên bỏ comment để đảm bảo code quality trước khi deploy
      - name: Run tests
        run: cd job && ./gradlew test

      # Bước 7: Build ứng dụng Spring Boot thành file JAR
      - name: Build with Gradle
        run: cd job && ./gradlew clean bootJar --no-daemon
        # clean: xóa thư mục build cũ
        # bootJar: tạo file JAR có thể chạy độc lập (executable JAR)
        # --no-daemon: không sử dụng Gradle daemon để tránh memory leak

      # Bước 8: Thiết lập Docker Buildx (công cụ build Docker nâng cao)
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3
        # Buildx hỗ trợ build multi-platform và các tính năng nâng cao

      # Bước 9: Đăng nhập vào Docker Hub
      - name: Log in to Docker Hub
        uses: docker/login-action@v3
        with:
          # Sử dụng secrets được cấu hình trong GitHub repository settings
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      # Bước 10: Tạo metadata cho Docker image (tags và labels)
      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.DOCKER_IMAGE }}
          # Quy tắc tạo tags cho Docker image:
          tags: |
            # Tag theo tên branch (nếu push từ branch)
            type=ref,event=branch
            # Tag theo tên tag (nếu push từ tag)
            type=ref,event=tag
            # Tag 'latest' nếu là branch mặc định (main)
            type=raw,value=latest,enable={{is_default_branch}}

      # Bước 11: Build và push Docker image lên Docker Hub
      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          # Thư mục chứa Dockerfile và source code
          context: ./job
          # Đường dẫn đến Dockerfile
          file: ./job/Dockerfile
          # Có push image lên registry hay không
          push: true
          # Sử dụng tags được tạo ở bước trước
          tags: ${{ steps.meta.outputs.tags }}
          # Sử dụng labels được tạo ở bước trước
          labels: ${{ steps.meta.outputs.labels }}
          # Cache từ GitHub Actions cache
          cache-from: type=gha
          cache-to: type=gha,mode=max
          # Build cho cả 2 kiến trúc CPU (Intel/AMD và ARM)
          platforms: linux/amd64,linux/arm64

      # Bước 12: In ra digest của image vừa build (để tracking)
      - name: Image digest
        run: echo ${{ steps.build.outputs.digest }}
```

#### 2.3.2. Thêm Secrets and variables trên GitHub

Phải vào **Settings** > **Secrets and variables** > **Actions** trên GitHub Repository và thêm 4 biến sau:

- `DOCKERHUB_USERNAME`: Tên tài khoản Docker Hub của bạn
- `DOCKERHUB_TOKEN`: Access Token (Tạo trên Docker Hub)
- `BREVO_API_KEY`: Key gửi mail
- `BREVO_API_URL`: Link API gửi mail

#### 2.3.3. Kích hoạt CI/CD đóng gói và push Docker Hub

Workflow này chỉ chạy khi có một tag bắt đầu bằng `deployment` được đẩy lên GitHub.

Ví dụ:
```bash
git tag deployment-v1.0
git push origin deployment-v1.0
```

### 2.4. Chuẩn bị Máy chủ AWS EC2 Setup

#### Bước 1: Chọn Khu vực (Region)
Chọn **Asia Pacific (Singapore)** `ap-southeast-1`

#### Bước 2: Bắt đầu khởi tạo
- Tìm và chọn dịch vụ **EC2**
- Bấm nút **Launch instance**

#### Bước 3: Đặt tên và chọn Hệ điều hành (OS)
1. **Name**: My Backend App
2. **Application and OS Images (AMI)**:
   - Chọn logo **Amazon Linux**
   - Chọn phiên bản mặc định: **Amazon Linux 2023 AMI** (Free tier eligible)

#### Bước 4: Chọn cấu hình máy (Instance Type)
Chọn **t3.micro** (Thông số: 2 vCPU, 1 GiB Memory)

#### Bước 5: Tạo chìa khóa đăng nhập (Key pair)
- Bấm vào dòng **Create new key pair**
- **Key pair name**: my-backend-key
- **Key pair type**: Chọn **RSA**
- **Private key file format**: Chọn **.pem**
- Bấm **Create key pair**

#### Bước 6: Cấu hình Mạng & Tường lửa (Network settings)
- **Auto-assign Public IP**: Chọn **Enable** (Để máy có IP public truy cập từ internet)
- **Firewall (security groups)**: Chọn **Create security group**
- **Inbound security groups rules** (Luật cho phép truy cập):
  - **Dòng 1** (Quản trị Server):
    - Type: SSH
    - Port range: 22
    - Source type: My IP hoặc Anywhere (0.0.0.0/0)
  - **Dòng 2** (Người dùng truy cập Web):
    - Type: HTTP
    - Port range: 80
    - Source type: Anywhere (0.0.0.0/0)
  - **Dòng 3** (Trang quản lý Nginx Proxy Manager):
    - Type: Custom TCP
    - Port range: 81
    - Source type: Anywhere (0.0.0.0/0)
  - **Dòng 4** (Dùng cho HTTPS):
    - Type: HTTPS
    - Port range: 443
    - Source type: Anywhere (0.0.0.0/0)

#### Bước 7: Cấu hình ổ cứng (Storage)
Chọn **20 GiB**

#### Bước 8: Hoàn tất
1. Kiểm tra lại tóm tắt bên tay phải (**Summary**)
2. Bấm nút màu cam **Launch instance**
3. Chờ màn hình báo "Success", bấm vào mã ID của instance

### 2.5. Cài đặt Docker & Docker Compose

#### Bước 1: Kết nối từ máy Windows vào EC2 (SSH)
- Mở **CMD** hoặc **PowerShell** trên máy tính
- Di chuyển đến thư mục chứa file chìa khóa (.pem)
- Chạy lệnh:
  ```bash
  ssh -i my-backend-key.pem ec2-user@18.143.194.168
  ```
- Nếu máy hỏi "Are you sure you want to continue connecting?", gõ `yes` rồi Enter
- Khi màn hình hiện ra dòng chữ kiểu `[ec2-user@18.143.194.168]` nghĩa là đang đứng bên trong máy chủ

#### Bước 2: Cài đặt Docker & Docker Compose

```bash
# Cập nhật hệ thống
sudo yum update -y

# Cài đặt Docker
sudo yum install docker -y

# Khởi động Docker Service
sudo service docker start

# Cấp quyền cho user 'ec2-user' dùng Docker
sudo usermod -a -G docker ec2-user

# Cài đặt Docker Compose (Plugin version)
sudo mkdir -p /usr/local/lib/docker/cli-plugins/
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m) -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
```

### 2.6. Tạo các thư mục cần thiết trên Server

```bash
# Về thư mục gốc của user
cd /home/ec2-user

# Tạo các thư mục cho Nginx
mkdir -p data letsencrypt

# Vào thư mục app (nơi chứa docker-compose.yml)
mkdir -p app
cd app

# Tạo thư mục upload cho App
mkdir -p public/uploads
```

### 2.7. Tạo và Chạy file Docker Compose, Cấu hình Nginx Proxy Manager

#### Bước 1: Tạo file và thêm nội dung

Gõ lệnh `nano docker-compose.yml`

```yaml
# docker-compose.yml
services:
  app:
    env_file:
      - .env
    image: nhan12163/tasks-management:latest
    volumes:
      - ./public/uploads:/app/public/uploads
    restart: always
    expose:
      - "8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/job_management
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root_password
      SPRING_WEB_RESOURCES_STATIC_LOCATIONS: file:/app/public/uploads/
      SPRING_SQL_INIT_MODE: always
      SPRING_JPA_DEFER_DATASOURCE_INITIALIZATION: "true"
    deploy:
      resources:
        limits:
          memory: 512M # Spring Boot cần khoảng này để chạy ổn định trên t3.micro
    depends_on:
      db:
        condition: service_healthy

  nginx-proxy:
    image: 'jc21/nginx-proxy-manager:latest'
    restart: always
    ports:
      - '80:80'   # HTTP
      - '443:443' # HTTPS
      - '81:81'   # Cổng quản lý giao diện Nginx
    volumes:
      - /home/ec2-user/data:/data
      - /home/ec2-user/letsencrypt:/etc/letsencrypt

  db:
    image: mysql:latest
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: job_management
    command: --performance-schema=OFF --innodb-buffer-pool-size=64M
    ports:
      - '3306:3306'
    volumes:
      - db_data:/var/lib/mysql
    deploy:
      resources:
        limits:
          memory: 300M # Giới hạn để không chiếm hết RAM của server
    healthcheck:
      test: ['CMD', 'mysqladmin', 'ping', '-h', 'localhost', '-u', 'root', '-proot_password']
      timeout: 20s
      retries: 10
      interval: 10s
      start_period: 60s

volumes:
  db_data:
```

#### Bước 2: Chạy Container

```bash
docker-compose up -d
```

#### Bước 3: Cấu hình Nginx Proxy Manager

**1. Đăng nhập trang quản trị Nginx:**
- Mở trình duyệt truy cập: http://18.143.194.168:81
- Email mặc định: `admin@example.com`
- Password mặc định: `changeme`

**2. Trỏ tên miền (hoặc IP) vào App:**
- Vào mục **Hosts** → **Proxy Hosts** → Bấm **Add Proxy Host**
- Điền thông tin:
  - **Domain Names**: Điền IP Public của server (https://nhantasks.duckdns.org)
  - **Scheme**: http
  - **Forward Hostname / IP**: ec2-user-app-1
  - **Forward Port**: 8080
  - Tích vào: **Block Common Exploits**
- Bấm **Save**