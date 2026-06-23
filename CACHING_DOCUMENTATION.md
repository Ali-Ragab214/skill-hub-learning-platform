# توثيق نظام Caching في Skill Hub Learning Platform

## 📌 مقدمة

نظام caching بيستخدم عشان نخزن مؤقتاً نتائج الكويرات المتكررة في Redis، فبدل ما نضرب الداتابيز كل مره، بنجيب البيانات من Redis اللي أسرع بكتير. ده بيحسن أداء الـ API وبيقلل الضغط على PostgreSQL.

استخدمنا **Spring Cache Abstraction** مع **Redis** كـ cache provider.

---

## 1️⃣ تفعيل Caching (`@EnableCaching`)

**الملف:** `SkillHubLearningPlatformApplication.java:8`

```java
@EnableCaching
public class SkillHubLearningPlatformApplication { ... }
```

`@EnableCaching` بتشغل نظام Spring Cache عشان يقدر يتعامل مع كل annotations ال caching اللي هنحطها في ال services.

---

## 2️⃣ إعدادات Redis

**الملف:** `application.properties:17-20`

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis
```

- بنحدد إن cache provider هو Redis (بديل عن الـ in-memory cache الافتراضي)
- السيرفر شغال على `localhost:6379`

---

## 3️⃣ تهيئة Redis Cache Manager (`RedisConfig.java`)

**الملف:** `RedisConfig.java`

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(...)
                .serializeValuesWith(...);

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }
}
```

### شرح الكلاس:

| الجزء | الشرح |
|-------|--------|
| `ObjectMapper` | بيجهز الـ JSON serializer عشان يقدر يتعامل مع أنواع معينة |
| `JavaTimeModule` | عشان يدعم `LocalDateTime` و `LocalDate` و غيره من تواريخ Java 8 |
| `WRITE_DATES_AS_TIMESTAMPS` | بنخلي التواريخ تخرج بصيغة ISO-8601 readable مش رقم timestamp |
| `activateDefaultTyping(EVERYTHING)` | الأهم ⭐ — بيحط نوع الكلاس (`@class`) جوه JSON عشان لما نجيب الداتا من Redis يرجعها تاني كـ `CourseResponse` مش `LinkedHashMap` |
| `entryTtl(10 minutes)` | كل cache entry يعيش لمدة 10 دقايق. بعد كده بنحذفه تلقائياً (TTL) |
| `StringRedisSerializer` | Serialize للمفاتيح (keys) عشان تكون plain strings |
| `GenericJackson2JsonRedisSerializer` | Serialize للقيم (values) لـ JSON |

> **ليه `DefaultTyping.EVERYTHING` مهم؟**  
> من غيرها، لما نجيب object من Redis مش هيعرف يرجع نوعه الأصلي و هيرميه كـ `LinkedHashMap` و ده هيسبب ClassCastException. بفضل `@class` المضمنة في JSON، Spring بيعرف يعيد بناء الـ object لنوعه الصحيح.

---

## 4️⃣ Cache Regions — مناطق التخزين المؤقت

عندنا **8 مناطق** كلها خاصه بالكورسات:

| الثابت | اسم Cache | المفتاح | فين مستخدم | بيخزن إيه |
|--------|-----------|---------|------------|-----------|
| `CACHE_COURSE` | `courses` | `#id` | `getCourseById` | كورس واحد معين (فقط لو PUBLISHED) |
| `CACHE_ALL` | `courses:all` | default (واحد) | `getAllCourses` | كل الكورسات (Admin فقط) |
| `CACHE_PUBLISHED` | `courses:published` | default (واحد) | `getAllPublishedCourses` | كل الكورسات المنشورة |
| `CACHE_MY` | `courses:my` | `#email` | `getMyCourses` | كورسات مدرب معين |
| `CACHE_PUB_INSTRUCTOR` | `courses:published:instructor` | `#instructorId` | `getPublishedCoursesByInstructorId` | كورسات منشورة لمدرب معين |
| `CACHE_LEVEL` | `courses:level` | `#level.name()` | `getCoursesByLevel` | كورسات حسب المستوى |
| `CACHE_PUB_LEVEL` | `courses:published:level` | `#level.name()` | `getPublishedCoursesByLevel` | كورسات منشورة حسب المستوى |
| `CACHE_STATUS` | `courses:status` | `#status.name() + ':' + #email` | `getCoursesByStatus` | كورسات حسب الحالة + الإيميل |

كل منطقة بتخزن `CourseResponse` أو `List<CourseResponse>`.

---

## 5️⃣ `@Cacheable` — قراءة من الـ Cache

بتستخدم في **read methods** عشان:
1. أول مره تتنده الميثود، تشتغل عادي وتخزن النتيجة في Redis.
2. المرات اللي بعد كده، ترد النتيجة من Redis من غير ما تشتغل الميثود ولا تضرب الداتابيز.

### 5.1 `getCourseById` — `CourseServiceImpl.java:157-158`

```java
@Cacheable(cacheNames = CACHE_COURSE, key = "#id",
           unless = "#result.status.name() != 'PUBLISHED'")
public CourseResponse getCourseById(Long id, String userEmail) { ... }
```

- **cacheNames:** `"courses"`
- **key:** `#id` (رقم الكورس)
- **unless:** لو الكورس مش PUBLISHED، **مينفعش نخزنه في الـ cache**. ليه؟ لأن المستخدم العادي مينفعش يشوف DRAFT ولا ARCHIVED. ولو خزناه، أي حد تاني ممكن يجيب الكورس من الـ cache حتى لو مش مالكه.

> يعني: الـ cache منطقي للكورسات العامة (PUBLISHED) بس. الكورسات اللي لسه مسودة أو مؤرشفة خاصة بصاحبها فمينفعش نشاركها في cache عام.

### 5.2 `getAllCourses` — `CourseServiceImpl.java:179`

```java
@Cacheable(cacheNames = CACHE_ALL)
public List<CourseResponse> getAllCourses() { ... }
```

- **cacheNames:** `"courses:all"`
- **key:** مفيش (افتراضي — كل اللي ينادي هينفذ الميثود مره واحده وياخد نفس النتيجة)
- بيجيب كل الكورسات (للاستخدام الاداري)

### 5.3 `getAllPublishedCourses` — `CourseServiceImpl.java:190`

```java
@Cacheable(cacheNames = CACHE_PUBLISHED)
public List<CourseResponse> getAllPublishedCourses() { ... }
```

- **cacheNames:** `"courses:published"`
- ده أكتر endpoint يتنادي (hottest read path)، فـ caching هنا بيفرق جداً

### 5.4 `getMyCourses` — `CourseServiceImpl.java:201`

```java
@Cacheable(cacheNames = CACHE_MY, key = "#email")
public List<CourseResponse> getMyCourses(String email) { ... }
```

- **key:** `#email` — عشان كل مدرب يشوف الكورسات بتاعته هو بس

### 5.5 `getPublishedCoursesByInstructorId` — `CourseServiceImpl.java:215`

```java
@Cacheable(cacheNames = CACHE_PUB_INSTRUCTOR, key = "#instructorId")
public List<CourseResponse> getPublishedCoursesByInstructorId(Long instructorId) { ... }
```

- **key:** `#instructorId` — بيخزن النتيجة لكل Instructor ID لوحده

### 5.6 `getCoursesByLevel` — `CourseServiceImpl.java:230`

```java
@Cacheable(cacheNames = CACHE_LEVEL, key = "#level.name()")
public List<CourseResponse> getCoursesByLevel(CourseLevel level) { ... }
```

- **key:** `#level.name()` — مثلاً `"BEGINNER"` أو `"INTERMEDIATE"`

### 5.7 `getPublishedCoursesByLevel` — `CourseServiceImpl.java:241`

```java
@Cacheable(cacheNames = CACHE_PUB_LEVEL, key = "#level.name()")
public List<CourseResponse> getPublishedCoursesByLevel(CourseLevel level) { ... }
```

- نفس فكرة اللي فوقه بس للمنشور بس

### 5.8 `getCoursesByStatus` — `CourseServiceImpl.java:255`

```java
@Cacheable(cacheNames = CACHE_STATUS, key = "#status.name() + ':' + #email")
public List<CourseResponse> getCoursesByStatus(CourseStatus status, String email) { ... }
```

- **key:** `#status.name() + ':' + #email` — مفتاح مركب من حالة الكورس + إيميل المستخدم  
  مثلاً: `"PUBLISHED:ali@example.com"` و `"DRAFT:ahmed@example.com"` دول نتائج مختلفة.
- ليه الإيميل في المفتاح؟ لأن في حالة DRAFT/ARCHIVED، النتائج بتختلف من مستخدم للتاني (Admin يشوف كل حاجه، المدرب العادي يشوف بتاعته بس). حتى PUBLISHED بنفس الإيميل للتبسيط.

---

## 6️⃣ `@CacheEvict` — حذف من الـ Cache

بتستخدم في **write methods** عشان تمسح entries من الـ cache لما أي حاجه تتغير. ليه؟ عشان الـ cache متبقاش قديمة (stale data).

### types of eviction:

1. **حذف entry واحد:** `@CacheEvict(cacheNames = "courses", key = "#id")` — بيمسح كورس معين
2. **حذف كل المنطقة:** `@CacheEvict(cacheNames = "courses:all", allEntries = true)` — بيمسح كل حاجه في المنطقة دي

---

## 7️⃣ `@CachePut` — تحديث في الـ Cache

بتستخدم في `publishCourse`:

```java
@CachePut(cacheNames = CACHE_COURSE, key = "#id")
```

دي بتخلي الميثود تشتغل (تعدل في الداتابيز) **وبعدين تحط النتيجة الجديدة في الـ cache**. كأنها update للـ cache بدل ما تمسحه وتخلي اللي بعدها يجيبها تاني من الداتابيز.

---

## 8️⃣ `@Caching` — تجميع أكتر من annotation

لما نحتاج نعمل أكتر من عملية cache في نفس الميثود (مثلاً نمسح اكتر من منطقة)، بنستخدم `@Caching` اللي بيخلينا نجمع اكتر من `@CacheEvict` و `@CachePut` في annotation واحده.

---

## 9️⃣ استراتيجية كل write method في CourseServiceImpl

### `createCourse` — `CourseServiceImpl.java:53-61`

```java
@Caching(evict = {
    @CacheEvict(CACHE_ALL,     allEntries = true),
    @CacheEvict(CACHE_PUBLISHED, allEntries = true),
    @CacheEvict(CACHE_MY,      allEntries = true),
    @CacheEvict(CACHE_PUB_INSTRUCTOR, allEntries = true),
    @CacheEvict(CACHE_LEVEL,   allEntries = true),
    @CacheEvict(CACHE_PUB_LEVEL, allEntries = true),
    @CacheEvict(CACHE_STATUS,  allEntries = true)
})
```

- **بيمسح 7 مناطق** (كل list caches) لأن في كورس جديد انضاف، فكل القوائم بقت قديمة.
- **مش بيحط entry فردي في `"courses"`** لأن طريقة `createCourse` بتستخدم `toCreationResponse()` (من غير sections) بينما `getCourseById` بتستخدم `toResponse()` (بالـ sections). فما ينفعش نحط creation response في cache لأن القراءة هتستقبل بيانات ناقصة.

### `updateCourse` — `CourseServiceImpl.java:75-84`

```java
@Caching(evict = {
    @CacheEvict(CACHE_COURSE,  key = "#id"),
    @CacheEvict(CACHE_ALL,     allEntries = true),
    ...7 مناطق تانية...
})
```

- **بيمسح entry الكورس الفردي** (`key = "#id"`) لأن العنوان أو الوصف اتغير
- **بيمسح كل القوائم** الـ 7 التانيين (كل `allEntries = true`)
- **المجموع: 8 مناطق** — عكس `createCourse` اللي مسح 7 بس

### `publishCourse` — `CourseServiceImpl.java:100-111`

```java
@Caching(
    put  = { @CachePut(CACHE_COURSE, key = "#id") },
    evict = {
        @CacheEvict(CACHE_ALL,     allEntries = true),
        ...7 مناطق تانية...
    }
)
```

- **`@CachePut`** بيحدث الـ entry الفردي في `"courses"` بالحالة الجديدة (PUBLISHED) بعد ما الميثود تشتغل
- **بيمسح كل القوائم** لأن في كورس جديد اننشر (أو اتغيرت حالته)
- الفرق بين `publishCourse` و `updateCourse`: هنا بنستخدم `@CachePut` بدل `@CacheEvict` للـ entry الفردي عشان نضمن إن الـ cache يتحدث فوراً بالكورس المنشور من غير فراغ (race condition window). المستخدم اللي بينادي `getCourseById` بعد `publishCourse` مباشرة هيجيب النتيجة من الـ cache من غير ما يضرب الداتابيز.

### `deleteCourse` — `CourseServiceImpl.java:130-139`

```java
@Caching(evict = {
    @CacheEvict(CACHE_COURSE,  key = "#id"),
    @CacheEvict(CACHE_ALL,     allEntries = true),
    ...7 مناطق تانية...
})
```

- **بيمسح entry الكورس الفردي** عشان محدش يجيب كورس اتمسح من الـ cache
- **بيمسح كل القوائم** (8 مناطق)

---

## 🔟 استراتيجية SectionServiceImpl و LessonServiceImpl

الاتنين عندهم نفس النمط:

```java
@Caching(evict = {
    @CacheEvict(cacheNames = "courses",            key = "#courseId"),
    @CacheEvict(cacheNames = "courses:all",        allEntries = true),
    @CacheEvict(cacheNames = "courses:published",  allEntries = true),
    @CacheEvict(cacheNames = "courses:my",         allEntries = true),
    @CacheEvict(cacheNames = "courses:published:instructor", allEntries = true),
    @CacheEvict(cacheNames = "courses:level",      allEntries = true),
    @CacheEvict(cacheNames = "courses:published:level", allEntries = true),
    @CacheEvict(cacheNames = "courses:status",     allEntries = true)
})
```

ليه بنستخدم hardcoded strings مش الثوابت؟ ده inconsistency محتاج يتصلح — المفروض نستخدم `CourseServiceImpl.CACHE_*` constants.

**ليه بنمسح cache الكورسات المره دي؟**  
لأن الـ `CourseResponse` بيحتوي على sections و lessons (data من غير Pagination على الأرجح). فلو section أو lesson اتغير (اتضاف/اتعدل/اتمسح)، يبقى الكورس المخزن في الـ cache عنده بيانات قديمة للـ sections والـ lessons. لازم نمسح كل حاجه ونخليها تجيب تاني من الداتابيز.

---

## 📊 ملخص الاستراتيجية

| المبدأ | الشرح |
|--------|-------|
| **Coarse-grained caching** | بنخزن course-level responses بس. الـ sections والـ lessons مش متخزنة بشكل مستقل. |
| **Write-through eviction** | أي change (create/update/delete) في أي حاجه متعلقة بالكورس بيمسح كل الكاش. |
| **Cache-aside (lazy loading)** | الـ cache بيتعمله populate بس أول مره حد ينادي read method بعد ما نمسحه. |
| **TTL = 10 دقايق** | ضمان إن أي data قديمة تتفرمغ تلقائياً حتى لو ما حصلش eviction. |
| **unless for security** | `getCourseById` بيستخدم `unless` لمنع تخزين الكورسات غير المنشورة. |
| **@CachePut for publish** | عشان نتأكد إن الكورس المنشور متاح فوراً في الـ cache من غير دقه. |

---

## ⚠️ حاجات محتاجة تتحسن (Improvements)

1. **استخدام الثوابت بدل hardcoded strings** في `SectionServiceImpl` و `LessonServiceImpl` بدل ما نكرر `"courses"`, `"courses:all"`, إلخ.
2. **اختبارات للـ caching** — مفيش integration tests بتختبر إن الـ cache شغال صح وإن الـ eviction بيشتغل. تقدر تضيف tests باستخدام `@CacheEvict` و تتأكد من cache hits/misses.
3. **Per-cache TTL configuration** — ممكن نحط TTL مختلف للمناطق المختلفة لو عايزين. مثلاً `courses:published` يفضل TTL أقصر.
4. **إضافة `@CacheConfig`** لتقليل التكرار لو بنستخدم نفس cache names في اكتر من مكان.
