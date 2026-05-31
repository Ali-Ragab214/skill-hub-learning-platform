# Phase 2 - نظام الأمان والمصادقة (Security & Authentication)

## نظرة عامة

تم تنفيذ نظام المصادقة والتفويض الكامل باستخدام **JWT (JSON Web Token)** و **Spring Security** مع الالتزام بتوزيع المسؤوليات حسب **Clean Architecture**.

---

## 1. توزيع الكلاسات على الطبقات

### Domain Layer
| الكلاس | المسار | الوظيفة |
|--------|--------|---------|
| `User` | `domain/models/User.java` | implements `UserDetails` - إضافة دوال Spring Security إلى الـ Entity |
| `Role` | `domain/enums/Role.java` | الأدوار: `Student`, `Instructor`, `Admin` |

### Application Layer
| الكلاس | المسار | الوظيفة |
|--------|--------|---------|
| `JwtService` (interface) | `application/security/JwtService.java` | واجهة لخدمة JWT (استخراج البيانات، توليد التوكن، التحقق) |
| `CustomUserDetailsService` | `application/security/CustomUserDetailsService.java` | تحميل المستخدم من قاعدة البيانات بواسطة الإيميل |
| `RegisterRequest` | `application/dto/request/RegisterRequest.java` | DTO للطلب - تسجيل مستخدم جديد |
| `LoginRequest` | `application/dto/request/LoginRequest.java` | DTO للطلب - تسجيل الدخول |
| `AuthResponse` | `application/dto/response/AuthResponse.java` | DTO للاستجابة - التوكن + بيانات المستخدم |
| `UserResponse` | `application/dto/response/UserResponse.java` | DTO لبيانات المستخدم (id, name, email, role) |
| `UserMapper` | `application/mapper/UserMapper.java` | تحويل User Entity إلى UserResponse DTO |
| `AuthService` (interface) | `application/services/auth/AuthService.java` | واجهة لخدمة المصادقة |
| `AuthServiceImpl` | `application/services/auth/AuthServiceImpl.java` | تنفيذ المصادقة: تسجيل (مع تشفير الباسورد) وتسجيل الدخول (مع إرجاع JWT) |
| `GlobalExceptionHandler` | `application/exceptions/GlobalExceptionHandler.java` | معالجة الأخطاء بشكل موحد |

### Infrastructure Layer
| الكلاس | المسار | الوظيفة |
|--------|--------|---------|
| `SecurityConfig` | `infrastructure/security/SecurityConfig.java` | تكوين SecurityFilterChain، قفل/فتح الـ Endpoints، تعريف Beans (PasswordEncoder, AuthenticationManager, AuthenticationProvider) |
| `JwtAuthFilter` | `infrastructure/security/JwtAuthFilter.java` | فلتر يقرأ Bearer Token من الـ Header ويتحقق منه ويضعه في SecurityContext |
| `JwtServiceImpl` | `infrastructure/security/JwtServiceImpl.java` | تنفيذ JwtService باستخدام مكتبة jjwt 0.12.6 |

### API Layer
| الكلاس | المسار | الوظيفة |
|--------|--------|---------|
| `AuthController` | `api/controller/AuthController.java` | REST Controller مع endpoint: POST `/api/v1/auth/register` و POST `/api/v1/auth/login` |

---

## 2. كيف يعمل مسار Authentication و Authorization

### تدفق Authentication (المصادقة)

```
تسجيل الدخول:
  POST /api/v1/auth/login
  Body: { "email": "...", "password": "..." }
  
  1. AuthController يستقبل الطلب ويرسله إلى AuthService.login()
  2. AuthServiceImpl يستخدم AuthenticationManager.authenticate()
  3. AuthenticationManager -> DaoAuthenticationProvider -> CustomUserDetailsService.loadUserByUsername()
  4. يتم جلب User من قاعدة البيانات والتحقق من كلمة المرور المشفرة (BCrypt)
  5. إذا نجح التحقق -> AuthServiceImpl يطلب JwtServiceImpl.generateToken(user)
  6. يتم إرجاع AuthResponse { token, user }

التسجيل:
  POST /api/v1/auth/register
  Body: { "name": "...", "email": "...", "password": "...", "role": "Student" }
  
  1. AuthController يستقبل الطلب ويرسله إلى AuthService.register()
  2. AuthServiceImpl يتحقق من عدم وجود البريد الإلكتروني مسبقاً
  3. تشفير كلمة المرور باستخدام BCryptPasswordEncoder
  4. حفظ المستخدم في قاعدة البيانات
  5. توليد JWT Token للمستخدم الجديد
  6. إرجاع AuthResponse { token, user }
```

### تدفق Authorization (التفويض)

```
كل طلب محمي:
  1. JwtAuthFilter (extends OncePerRequestFilter) يشتغل على كل طلب
  2. يقرأ Authorization Header: "Bearer <token>"
  3. يستخرج الإيميل من التوكن باستخدام JwtService.extractUsername()
  4. يحمل UserDetails عبر CustomUserDetailsService
  5. يتحقق من صحة التوكن (التوقيع، تاريخ الصلاحية)
  6. ينشئ UsernamePasswordAuthenticationToken ويضعه في SecurityContextHolder
  7. Spring Security يستخدم الـ Authority (ROLE_STUDENT, ROLE_INSTRUCTOR, ROLE_ADMIN)
     للتحقق من الصلاحيات بناءً على التكوين في SecurityConfig

مثال: طلب إلى /api/v1/admin/**
  - SecurityConfig يستخدم .hasRole("ADMIN")
  - هذا يتحقق من وجود "ROLE_ADMIN" في GrantedAuthorities
  - الـ Role "Admin" يتحول إلى "ROLE_ADMIN" تلقائياً عبر User.getAuthorities()
```

### قواعد حماية الـ Endpoints في SecurityConfig

| الـ Endpoint | الوصول |
|-------------|--------|
| `/api/v1/auth/**` | عام (permitAll) |
| `/api/v1/admin/**` | ADMIN فقط (hasRole) |
| `/api/v1/instructor/**` | INSTRUCTOR أو ADMIN (hasAnyRole) |
| أي مسار آخر | مصادق عليه (authenticated) |

### الصلاحيات (Roles)

الـ Role `Student` -> يتحول إلى `ROLE_STUDENT` في `getAuthorities()`.
الـ Role `Instructor` -> يتحول إلى `ROLE_INSTRUCTOR`.
الـ Role `Admin` -> يتحول إلى `ROLE_ADMIN`.

يتم التحقق باستخدام دوال مثل:
- `hasRole("ADMIN")` -> يتأكد من وجود `ROLE_ADMIN`
- `hasAnyRole("INSTRUCTOR", "ADMIN")` -> يتأكد من وجود `ROLE_INSTRUCTOR` أو `ROLE_ADMIN`

### هيكل JWT Token

```
Header: { "alg": "HS256" }
Payload: {
  "sub": "user@example.com",      // subject = email
  "iat": 1716512000,               // issued at
  "exp": 1716598400                // expiration (24 ساعة)
}
Signature: HMAC-SHA256(Base64(Header) + "." + Base64(Payload), SecretKey)
```

---

## 3. دليل اختبار الـ Endpoints باستخدام Postman

### 3.1. تشغيل التطبيق

```bash
./mvnw spring-boot:run
```

### 3.2. اختبار Register (تسجيل مستخدم جديد)

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/v1/auth/register`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**

```json
{
  "name": "Ahmed Ali",
  "email": "ahmed@example.com",
  "password": "12345678",
  "role": "Student"
}
```

> **ملاحظة:** حقل `role` اختياري. إذا لم يتم إرساله، يتم تعيين `Student` تلقائياً. القيم المقبولة: `Student`, `Instructor`, `Admin`.

**Response (201 Created):**

```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaG1lZEBleGFtcGxlLmNvbSIsImlhdCI6MTcxNjUxMjAwMCwiZXhwIjoxNzE2NTk4NDAwfQ.example_signature",
    "user": {
      "id": 1,
      "name": "Ahmed Ali",
      "email": "ahmed@example.com",
      "role": "Student"
    }
  },
  "timestamp": "2026-05-24T15:30:00.000",
  "statusCode": 201
}
```

**حالة الخطأ (409) - البريد موجود مسبقاً:**

```json
{
  "success": false,
  "message": "Email already exists",
  "data": null,
  "timestamp": "2026-05-24T15:30:00.000",
  "statusCode": 400
}
```

**حالة الخطأ (400) - Validation Error:**

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "must be a well-formed email address",
    "password": "size must be between 6 and 100"
  },
  "timestamp": "2026-05-24T15:30:00.000",
  "statusCode": 400
}
```

### 3.3. اختبار Login (تسجيل الدخول)

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/v1/auth/login`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**

```json
{
  "email": "ahmed@example.com",
  "password": "12345678"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaG1lZEBleGFtcGxlLmNvbSIsImlhdCI6MTcxNjUxMjAwMCwiZXhwIjoxNzE2NTk4NDAwfQ.example_signature",
    "user": {
      "id": 1,
      "name": "Ahmed Ali",
      "email": "ahmed@example.com",
      "role": "Student"
    }
  },
  "timestamp": "2026-05-24T15:30:00.000",
  "statusCode": 200
}
```

**حالة الخطأ (401) - كلمة مرور خاطئة:**

```json
{
  "success": false,
  "message": "Invalid email or password",
  "data": null,
  "timestamp": "2026-05-24T15:30:00.000",
  "statusCode": 401
}
```

### 3.4. اختبار Endpoint محمي (مثال: بعد إنشاء Admin)

افترض أنك سجلت كمستخدم بدور `Admin` وحصلت على التوكن.

1. **سجل مستخدم Admin:**
   ```bash
   POST /api/v1/auth/register
   Body: { "name": "Admin User", "email": "admin@example.com", "password": "12345678", "role": "Admin" }
   ```
2. **خذ التوكن من الاستجابة.**

3. **اختبر الوصول إلى Admin Endpoint:**
   - **Method:** `GET` (أو أي method مسموح به)
   - **URL:** `http://localhost:8080/api/v1/admin/courses` (مثال)
   - **Headers:**
     ```
     Authorization: Bearer <put_your_token_here>
     Content-Type: application/json
     ```

**الاستجابة المتوقعة عند النجاح:**
- يعتمد على الـ Admin Controller الذي سيتم تطويره لاحقاً.

**الاستجابة عند فشل المصادقة (401) - بدون توكن:**

```json
{
  "success": false,
  "message": "An unexpected error occurred",
  "data": null,
  "timestamp": "2026-05-24T15:30:00.000",
  "statusCode": 401
}
```

**الاستجابة عند عدم كفاية الصلاحيات (403) - Forbidden:**

```json
{
  "success": false,
  "message": "An unexpected error occurred",
  "data": null,
  "timestamp": "2026-05-24T15:30:00.000",
  "statusCode": 403
}
```

### 3.5. اختبار Endpoint محمي بدور Instructor

1. **سجل مستخدم Instructor:**
   ```bash
   POST /api/v1/auth/register
   Body: { "name": "Dr. Ahmed", "email": "dr.ahmed@example.com", "password": "12345678", "role": "Instructor" }
   ```
2. **خذ التوكن.**
3. **اختبر الوصول إلى Instructor Endpoint:**
   - **Method:** `GET`
   - **URL:** `http://localhost:8080/api/v1/instructor/courses`
   - **Headers:**
     ```
     Authorization: Bearer <token>
     ```

### 3.6. اختبار الوصول بدون مصادقة

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/courses` (أو أي endpoint محمي)
- **بدون إرسال Authorization Header**

**Response (401 Unauthorized):** سيتم رفض الطلب.

---

## 4. التكوين

### JWT Settings (في application.properties)

```properties
application.security.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
application.security.jwt.expiration=86400000
```

- `secret-key`: مفتاح تشفير بطول 256-bit مشفر بـ Base64
- `expiration`: مدة صلاحية التوكن بالملي ثانية (86400000 = 24 ساعة)

### قاعدة البيانات

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/learning_academy
spring.datasource.username=ali
spring.datasource.password=12345
spring.jpa.hibernate.ddl-auto=update
```

---

## 5. ملخص الكلاسات المنشأة

| الملف | الحالة |
|-------|--------|
| `pom.xml` | تمت إضافة jjwt-api, jjwt-impl, jjwt-jackson (0.12.6) |
| `application.properties` | تمت إضافة JWT settings |
| `domain/models/User.java` | تعديل: implements UserDetails + إضافة دوال getAuthorities, getUsername, isEnabled... |
| `application/security/JwtService.java` | إنشاء: interface |
| `application/security/CustomUserDetailsService.java` | إنشاء: implements UserDetailsService |
| `application/dto/request/RegisterRequest.java` | إنشاء |
| `application/dto/request/LoginRequest.java` | إنشاء |
| `application/dto/response/AuthResponse.java` | إنشاء |
| `application/dto/response/UserResponse.java` | إنشاء |
| `application/mapper/UserMapper.java` | إنشاء |
| `application/services/auth/AuthService.java` | إنشاء: interface |
| `application/services/auth/AuthServiceImpl.java` | إنشاء |
| `application/exceptions/GlobalExceptionHandler.java` | إنشاء |
| `infrastructure/security/SecurityConfig.java` | إنشاء |
| `infrastructure/security/JwtAuthFilter.java` | إنشاء |
| `infrastructure/security/JwtServiceImpl.java` | إنشاء |
| `api/controller/AuthController.java` | إنشاء |

---

## 6. التحقق من عمل المشروع

```bash
# تشغيل التطبيق
./mvnw spring-boot:run

# أو تشغيل الاختبارات فقط
./mvnw test
```

بعد تشغيل التطبيق، يمكنك الوصول إلى Swagger UI لاختبار الـ Endpoints:
```
http://localhost:8080/swagger-ui.html
```
