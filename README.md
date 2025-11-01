app/
 └─ src/main/java/com/example/vitazen/
      ├─ ui/                       # View
      │    ├─ welcome/             
      │    │    └─ WelcomeScreen.kt
      │    ├─ login/
      │    │    └─ LoginScreen.kt
      │    ├─ home/
      │    │    └─ HomeScreen.kt
      │    ├─ reminder/
      │    │    └─ ReminderScreen.kt
      │    ├─ history/
      │    │    └─ HistoryScreen.kt
      │    ├─ profile/
      │    │    └─ ProfileScreen.kt
      │    └─ components/          # Composable dùng chung
      │         └─ AppButton.kt
      ├─ viewmodel/                # ViewModel
      │    ├─ WelcomeViewModel.kt
      │    ├─ LoginViewModel.kt
      │    ├─ HomeViewModel.kt
      │    ├─ ReminderViewModel.kt
      │    ├─ HistoryViewModel.kt
      │    └─ ProfileViewModel.kt
      ├─ model/                    # Model
      │    ├─ repository/          # Xử lý dữ liệu
      │    │    ├─ UserRepository.kt
      │    │    └─ ReminderRepository.kt
      │    ├─ data/                # Entity / data class
      │    │    ├─ User.kt
      │    │    └─ Reminder.kt
      │    └─ network/             # API service (nếu có)
      │         └─ ApiService.kt
      ├─ navigation/               
      │    ├─ AppNavGraph.kt
      │    └─ Routes.kt
      └─ MainActivity.kt
