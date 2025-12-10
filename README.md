# VitaZen - Ứng dụng theo dõi sức khỏe

VitaZen là một ứng dụng di động được phát triển trên nền tảng Android, giúp người dùng theo dõi và quản lý các chỉ số sức khỏe cá nhân một cách dễ dàng và hiệu quả. Ứng dụng cung cấp các tính năng như nhập liệu sức khỏe, xem thống kê bằng biểu đồ, đặt lời nhắc và lưu trữ lịch sử.

## Tính năng chính

- **Đăng ký/Đăng nhập**: Người dùng có thể tạo tài khoản mới hoặc đăng nhập vào tài khoản hiện có.
- **Nhập dữ liệu sức khỏe**: Cho phép người dùng nhập các chỉ số sức khỏe như cân nặng, chiều cao, huyết áp, nhịp tim, số bước chân, v.v.
- **Thống kê và biểu đồ**: Hiển thị các chỉ số sức khỏe dưới dạng biểu đồ trực quan, giúp người dùng dễ dàng theo dõi tiến trình.
- **Lời nhắc sức khỏe**: Thiết lập các lời nhắc để người dùng không bỏ lỡ việc kiểm tra sức khỏe định kỳ.
- **Lưu trữ lịch sử**: Lưu trữ toàn bộ lịch sử sức khỏe của người dùng, cho phép xem lại và so sánh.

## Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin
- **Kiến trúc**: MVVM (Model-View-ViewModel)
- **Jetpack Compose**: Xây dựng giao diện người dùng hiện đại
- **Room Database**: Lưu trữ dữ liệu cục bộ
- **Navigation Component**: Điều hướng giữa các màn hình
- **Material Design 3**: Thiết kế giao diện theo chuẩn Material Design

## Package Structure 

 com.example.vitazen/
├── 📦 model/
│   ├── data/              # Entities (User, HealthData, Reminder...)
│   ├── database/          # DAOs & Database class
│   └── repository/        # Repository pattern
│
├── 📱 ui/
│   ├── home/             # Home screen
│   ├── reminder/         # Reminder screen
│   ├── history/          # History screen
│   ├── profile/          # Profile screen
│   ├── settings/         # Settings screen
│   ├── login/            # Login screen
│   ├── register/         # Register screen
│   ├── note/             # Note screen
│   ├── components/       # Reusable UI components
│   └── theme/            # Theme & styling
│
├── 🎯 viewmodel/         # ViewModels (Business logic)
│   ├── HomeViewModel.kt
│   ├── ReminderViewModel.kt
│   └── ...
│
├── 🧭 navigation/        # Navigation logic
│   ├── AppNavGraph.kt
│   ├── Routes.kt
│   └── SplashScreen.kt
│
├── 🛠️ util/              # Utilities
│   ├── ReminderNotificationHelper.kt
│   ├── EmailValidator.kt
│   └── Extensions.kt
│
└── MainActivity.kt       # Entry point
## Cài đặt

### Yêu cầu hệ thống

- Android SDK 24 trở lên
- Android Studio Arctic Fox trở lên

### Các bước cài đặt

1. Clone dự án từ repository:
   ```bash
   git clone https://github.com/totomace/VitaZen.git
