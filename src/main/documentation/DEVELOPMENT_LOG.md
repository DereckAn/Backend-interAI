# Backend-interAI - Development Log

## Project Overview
Backend for InterviewAI platform built with **Quarkus** (Java), **PostgreSQL**, and **MinIO** for file storage.

---

## Session Log: Frontend-Backend Integration Fix (August 18, 2025)

### 🔍 Initial Problem
**Issue**: Frontend form submission was failing - no data reaching backend database, no bucket created in MinIO.

**Symptoms**:
- Button click appeared to do nothing
- No database records created
- No MinIO bucket creation
- No visible errors in frontend

### 🚀 Investigation Process

#### 1. **Form Data Store Analysis**
**File**: `src/store/formDataStore.ts`
- ✅ Found `submitFormData` function correctly implemented
- ✅ FormData creation working
- ❌ **Issue discovered**: Sending topic/language names instead of UUIDs

**Original problematic code**:
```javascript
const jsonData = {
  userId: state.userId,
  programmingLanguage: state.programmingLanguage, // ❌ Sending "Java" instead of UUID
  selectedTopic: state.selectedTopic, // ❌ Sending "Backend" instead of UUID
  difficultyLevel: state.difficultyLevel
};
```

#### 2. **Backend Endpoint Analysis**
**File**: `src/main/java/dereck/angeles/controller/InterviewController.java`
- ✅ Endpoint `/interview/create` properly configured
- ❌ **Issue found**: Expects `selectedTopicId` and `programmingLanguageId` (UUIDs)
- ✅ File upload handling implemented

**Backend expected format**:
```java
public static class InterviewCreateRequest {
    public String userId;
    public String selectedTopicId;    // ❌ Frontend sending selectedTopic
    public String programmingLanguageId; // ❌ Frontend sending programmingLanguage
    public String difficultyLevel;
    public String jobDescription;
    public Integer yearsOfExperience;
}
```

#### 3. **API Testing**
**Tested endpoints**:
- ✅ `GET /topics` - Returns all topics with UUIDs
- ✅ `GET /languages` - Returns all languages with UUIDs
- ❌ `POST /interview/create` - Failed due to data mapping issues

**Test user credentials**:
- Email: `dereck@example.com`
- Password: `Unicornio5`
- User UUID: `179ec5b1-f56c-4c90-a0e1-03b8f4dd664a`

---

### 🔧 Solutions Implemented

#### 1. **Fixed Frontend Data Mapping**
**File**: `Frontend-interAI/src/store/formDataStore.ts`

**Problem**: Frontend was sending names, backend expected UUIDs.

**Solution**: Added API calls to fetch topics/languages and map names to IDs:

```javascript
// NEW: Fetch topics and languages to map names to IDs
const [topicsResponse, languagesResponse] = await Promise.all([
  fetch(`${process.env.NEXT_PUBLIC_API_URL}/topics`),
  fetch(`${process.env.NEXT_PUBLIC_API_URL}/languages`)
]);

const topics = await topicsResponse.json();
const languages = await languagesResponse.json();

// Find the IDs for the selected topic and language
const selectedTopicObj = topics.find((topic: any) => topic.name === state.selectedTopic);
const selectedLanguageObj = languages.find((lang: any) => lang.name === state.programmingLanguage);

// Corrected data format
const jsonData = {
  userId: state.userId,
  jobDescription: state.jobDescription,
  difficultyLevel: state.difficultyLevel,
  yearsOfExperience: state.yearsOfExperience,
  programmingLanguageId: selectedLanguageObj.id, // ✅ Now sending UUID
  selectedTopicId: selectedTopicObj.id, // ✅ Now sending UUID
};
```

#### 2. **Fixed Difficulty Level Database Query**
**File**: `src/main/java/dereck/angeles/repository/DifficultyRepository.java`

**Problem**: "Mid-Level" difficulty lookup failing.

**Original code**:
```java
// ❌ Complex enum mapping causing issues
switch (level) {
    case "Mid-Level":
        difficultyLevel = Difficulty.DifficultyLevel.MidLevel;
        break;
}
```

**Solution**: Simplified query to match database strings directly:
```java
public Difficulty findByLevel(String level) {
    List<Difficulty> difficulties = getEntityManager()
            .createQuery("SELECT d FROM Difficulty d WHERE CAST(d.level AS STRING) = :level", Difficulty.class)
            .setParameter("level", level)
            .getResultList();
    return difficulties.isEmpty() ? null : difficulties.get(0);
}
```

#### 3. **Fixed User Model Role Configuration**
**File**: `src/main/java/dereck/angeles/model/User.java`

**Problem**: Hibernate kept dropping/recreating role column causing startup failures.

**Issue**: 
```
ERROR: column "role" of relation "users" contains null values
```

**Solution**: Changed from String enum to Ordinal enum:
```java
// BEFORE: String-based enum causing conflicts
@Enumerated(EnumType.STRING)
@JdbcTypeCode(SqlTypes.NAMED_ENUM)
@ColumnDefault("'USER'")

// AFTER: Ordinal-based enum with numeric default
@Enumerated(EnumType.ORDINAL)
@ColumnDefault("0")  // 0 = USER, 1 = ADMIN
@Column(name = "role")
private AuthRole role;
```

#### 4. **Standardized Repository Pattern**
**File**: `src/main/java/dereck/angeles/repository/DifficultyRepository.java`

**Problem**: Inconsistent repository implementation causing transaction issues.

**Solution**: Made all repositories implement `PanacheRepositoryBase`:
```java
// BEFORE: Custom EntityManager injection
@ApplicationScoped
public class DifficultyRepository {
    @Inject
    EntityManager entityManager;
    
    public void persist(Difficulty difficulty) {
        entityManager.persist(difficulty); // ❌ Transaction issues
    }
}

// AFTER: Consistent Panache pattern
@ApplicationScoped
public class DifficultyRepository implements PanacheRepositoryBase<Difficulty, UUID> {
    // ✅ Inherits persist(), findById(), etc. automatically
}
```

#### 5. **Fixed Entity ID Generation**
**File**: `src/main/java/dereck/angeles/model/Difficulty.java`

**Problem**: 
```
Identifier of entity 'Difficulty' must be manually assigned before calling 'persist()'
```

**Solution**: Added missing `@GeneratedValue` annotation:
```java
// BEFORE: No auto-generation
@Id
@ColumnDefault("uuid_generate_v4()")
@Column(name = "id", nullable = false)
private UUID id;

// AFTER: Auto-generated UUIDs
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
@ColumnDefault("uuid_generate_v4()")
@Column(name = "id", nullable = false)
private UUID id;
```

#### 6. **Created Automatic Data Initialization Service**
**File**: `src/main/java/dereck/angeles/service/DataInitializationService.java`

**Purpose**: Automatically populate database with initial data on startup.

**Features**:
- ✅ Runs on application startup (`@Observes StartupEvent`)
- ✅ Checks if data exists before inserting (prevents duplicates)
- ✅ Initializes topics, languages, and difficulties
- ✅ All descriptions in English
- ✅ Logs what data is created

**Key method**:
```java
@Transactional
void onStart(@Observes StartupEvent ev) {
    initializeTopics();
    initializeLanguages();
    initializeDifficulties();
}
```

---

### 🗃️ Database Configuration

**Current setup** (`application.properties`):
```properties
# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=user
quarkus.datasource.password=password
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/interview_preparation

# Schema management
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.database.generation.create-schemas=true
quarkus.hibernate-orm.sql-load-script=no-file

# MinIO Configuration
quarkus.s3.endpoint-override=http://localhost:9000
quarkus.s3.aws.credentials.static-provider.access-key-id=minioadmin
quarkus.s3.aws.credentials.static-provider.secret-access-key=minioadmin123
minio.bucket-name=interview-files
```

---

### 🧪 Testing Results

#### ✅ **Working Endpoints**:
- `GET /topics` - Returns 17 topics with UUIDs
- `GET /languages` - Returns 12 languages with UUIDs
- `POST /interview/create` - Successfully creates interviews
- `POST /api/auth/login` - User authentication works

#### ✅ **Working Difficulties**:
- "Junior" ✅
- "Mid-Level" ✅ (after repository fix)
- "Senior" ✅

#### ❌ **Known Issues**:
- **MinIO credentials**: File uploads fail due to AWS credential configuration
- **OAuth users**: Only credential-based users work (UUID format issue)

---

### 📁 Files Modified

**Core Fixes**:
1. `src/main/java/dereck/angeles/repository/DifficultyRepository.java` - Fixed query logic
2. `src/main/java/dereck/angeles/model/User.java` - Fixed role enum configuration
3. `src/main/java/dereck/angeles/model/Difficulty.java` - Added @GeneratedValue
4. `src/main/resources/application.properties` - Updated database configuration

**New Files**:
1. `src/main/java/dereck/angeles/service/DataInitializationService.java` - Auto data population

---

### 🎯 Current Status

**✅ Working**:
- Form data submission from frontend to backend
- Interview creation in database
- User authentication with credentials
- Automatic data initialization on startup
- Topic/Language/Difficulty lookup

**⚠️ Remaining Issues**:
1. **MinIO file uploads** - Needs AWS credentials configuration
2. **OAuth user support** - Needs UUID format handling
3. **Database migrations** - Consider switching to Flyway for production

---

### 🚀 Next Steps

1. **Fix MinIO configuration** for file uploads
2. **Add OAuth user creation** in backend
3. **Implement proper error handling** in frontend
4. **Add comprehensive logging**
5. **Create API documentation**

---

### 💡 Lessons Learned

1. **Data mapping is critical** - Always verify frontend/backend data formats match
2. **Repository consistency** - Use consistent patterns (Panache) across all repositories
3. **Enum handling** - PostgreSQL enums vs Java enums require careful configuration
4. **Transaction management** - Let Panache handle transactions automatically
5. **Auto-initialization** - Services with `@Observes StartupEvent` are powerful for data setup

---

*Last updated: August 18, 2025*