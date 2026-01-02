# API 명세서 변경 사항 요약

## 🔄 주요 변경 이유

**질문**: "월간 분석은 Spring Boot와 통신해야 하지 않아?"

**답변**: 맞습니다! 월간 분석은 MySQL에 저장된 한 달치 데이터가 필요하므로, Spring Boot를 통해야 합니다.

---

## ✅ 수정된 아키텍처

### 이전 (잘못된 구조)

```
월간 분석:
프론트 → Spring Boot (데이터 조회)
  ↓
프론트 → FastAPI (AI 분석)
  ↓
프론트 → Spring Boot (결과 저장)
```

**문제점:**

- 프론트가 데이터를 중계함 (보안 취약)
- 네트워크 요청 3번 (비효율적)
- 프론트 로직이 복잡함
- 트랜잭션 관리가 어려움

### 현재 (수정된 구조)

```
월간 분석:
프론트 → Spring Boot (요청)
          ↓
    Spring Boot → MySQL (데이터 조회)
          ↓
    Spring Boot → FastAPI (AI 분석)
          ↓
    Spring Boot → MySQL (결과 저장)
          ↓
프론트 ← Spring Boot (응답)
```

**장점:**

- 프론트는 1번의 API 호출만 (단순함)
- 데이터가 서버 간에만 이동 (보안 강화)
- Spring Boot가 전체 프로세스 관리 (트랜잭션 일관성)
- 에러 핸들링이 명확함

---

## 📝 변경된 API 명세

### 1. 시스템 아키텍처 다이어그램 수정

```
[일일 운세] : 프론트 → FastAPI (직접 통신) ✅
[이미지 생성] : 프론트 → FastAPI (직접 통신) ✅
[월간 분석] : 프론트 → Spring Boot → FastAPI (서버 간 통신) ⭐ NEW
```

### 2. Spring Boot API 수정

#### `/api/analysis/monthly` (POST) - 변경됨

**이전:**

- 프론트가 `monthlyReport`를 직접 전달
- 단순 저장만 수행

**현재:**

- 프론트는 `year`, `month`만 전달
- Spring Boot가 다음을 수행:
  1. MySQL에서 해당 월 데이터 조회
  2. FastAPI 호출하여 AI 리포트 생성
  3. 결과를 MySQL에 저장
  4. 완료된 리포트를 프론트에 반환

**Request:**

```json
{
  "year": 2025,
  "month": 11
}
```

**Response:**

```json
{
  "analysisId": 1,
  "year": 2025,
  "month": 11,
  "dreamCount": 15,
  "avgEmotionScore": 3.5,
  "monthlyReport": "AI가 생성한 월간 리포트 전문 (마크다운)",
  "message": "월별 AI 리포트가 생성되었습니다."
}
```

### 3. FastAPI API 설명 수정

#### `/api/v1/fortune/monthly-analysis` (POST)

**이전:**

- **호출자**: 프론트엔드 (직접 통신)

**현재:**

- **호출자**: Spring Boot (서버 간 통신) ⭐
- 프론트엔드는 이 API를 직접 호출하지 않음

### 4. 프론트엔드 통신 플로우 변경

**시나리오 3: 월간 리포트 생성**

**이전 (8단계):**

```
1. 프론트 → Spring Boot (꿈 일기 조회)
2. 프론트 → Spring Boot (각 AI 결과 조회)
3. 프론트 (daily_data 배열 구성)
4. 프론트 → FastAPI (AI 분석 요청)
5. 프론트 ← FastAPI (리포트 수신)
6. 프론트 → Spring Boot (리포트 저장)
```

**현재 (2단계):**

```
1. 프론트 → Spring Boot (월간 분석 요청)
   Spring Boot 내부에서:
   - MySQL 데이터 조회
   - FastAPI 호출
   - 결과 저장

2. 프론트 ← Spring Boot (완료 응답)
```

---

## 🔧 추가된 설정

### Spring Boot에서 FastAPI 호출 설정

**application.properties 추가:**

```properties
# FastAPI 연동 설정
fastapi.url=http://localhost:8000
fastapi.timeout=60000
```

**RestTemplate 또는 WebClient 설정 필요:**

```java
@Configuration
public class WebClientConfig {
    @Value("${fastapi.url}")
    private String fastApiUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl(fastApiUrl)
            .build();
    }
}
```

---

## 🎯 프론트엔드 개발자를 위한 가이드

### API 호출 규칙

| 기능               | API 호출 대상      | 비고                                     |
| ------------------ | ------------------ | ---------------------------------------- |
| 로그인/회원가입    | Spring Boot        | JWT 토큰 받기                            |
| 꿈 일기 CRUD       | Spring Boot        | 인증 필요                                |
| **일일 운세 분석** | **FastAPI (직접)** | 인증 불필요 ✅                           |
| **이미지 생성**    | **FastAPI (직접)** | 인증 불필요 ✅                           |
| AI 결과 저장       | Spring Boot        | 인증 필요                                |
| **월간 분석**      | **Spring Boot**    | 인증 필요, Spring Boot가 FastAPI 호출 ⭐ |
| 월별 메모          | Spring Boot        | 인증 필요                                |

### 코드 예시 (프론트엔드)

#### ❌ 잘못된 방법 (이전)

```javascript
// 월간 분석 - 잘못된 방법
async function generateMonthlyReport(year, month) {
  // 1. 꿈 일기 조회
  const dreams = await fetch(`${SPRING_URL}/api/dreams?year=${year}&month=${month}`);

  // 2. 각 AI 결과 조회
  const dailyData = await Promise.all(dreams.map((dream) => fetch(`${SPRING_URL}/api/dreams/${dream.id}/result`)));

  // 3. FastAPI 호출
  const report = await fetch(`${FASTAPI_URL}/api/v1/fortune/monthly-analysis`, {
    method: "POST",
    body: JSON.stringify({ daily_data: dailyData }),
  });

  // 4. Spring Boot에 저장
  await fetch(`${SPRING_URL}/api/analysis/monthly`, {
    method: "POST",
    body: JSON.stringify({ year, month, monthlyReport: report.data }),
  });
}
```

#### ✅ 올바른 방법 (현재)

```javascript
// 월간 분석 - 올바른 방법
async function generateMonthlyReport(year, month, token) {
  const response = await fetch(`${SPRING_URL}/api/analysis/monthly`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ year, month }),
  });

  const data = await response.json();
  // data.monthlyReport에 마크다운 형식 리포트 포함
  return data;
}
```

---

## 🚀 백엔드 개발자를 위한 가이드

### Spring Boot에서 구현해야 할 것

#### 1. FastAPI 호출 서비스

```java
@Service
public class MonthlyAnalysisService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private DreamRepository dreamRepository;

    @Autowired
    private MonthlyAnalysisRepository analysisRepository;

    public MonthlyAnalysisResponse generateMonthlyReport(Long userId, int year, int month) {
        // 1. MySQL에서 해당 월 데이터 조회
        List<Dream> dreams = dreamRepository.findByUserIdAndYearMonth(userId, year, month);

        if (dreams.isEmpty()) {
            throw new BadRequestException("해당 월에 꿈 일기가 없습니다.");
        }

        // 2. FastAPI 요청 데이터 구성
        MonthlyAnalysisRequest request = buildRequest(dreams);

        // 3. FastAPI 호출
        MonthlyAnalysisResponse response = webClient.post()
            .uri("/api/v1/fortune/monthly-analysis")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(MonthlyAnalysisResponse.class)
            .timeout(Duration.ofSeconds(60))
            .onErrorMap(TimeoutException.class, e ->
                new ServiceUnavailableException("AI 서비스 타임아웃"))
            .block();

        // 4. MySQL에 저장
        MonthlyAnalysis analysis = new MonthlyAnalysis();
        analysis.setUserId(userId);
        analysis.setYear(year);
        analysis.setMonth(month);
        analysis.setMonthlyReport(response.getReport());
        analysis.setDreamCount(dreams.size());
        analysis.setAvgEmotionScore(calculateAvgScore(dreams));

        analysisRepository.save(analysis);

        return response;
    }
}
```

#### 2. 컨트롤러

```java
@RestController
@RequestMapping("/api/analysis")
public class MonthlyAnalysisController {

    @Autowired
    private MonthlyAnalysisService analysisService;

    @PostMapping("/monthly")
    public ResponseEntity<MonthlyAnalysisResponse> createMonthlyAnalysis(
            @RequestBody MonthlyAnalysisRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);

        try {
            MonthlyAnalysisResponse response = analysisService
                .generateMonthlyReport(userId, request.getYear(), request.getMonth());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("AI 서비스에 연결할 수 없습니다."));
        }
    }
}
```

---

## 📊 비교표

| 항목                     | 이전 구조          | 현재 구조            |
| ------------------------ | ------------------ | -------------------- |
| **프론트 API 호출 횟수** | 3-4회              | 1회                  |
| **프론트 로직 복잡도**   | 높음               | 낮음                 |
| **데이터 보안**          | 취약 (프론트 경유) | 강화 (서버 간만)     |
| **트랜잭션 관리**        | 어려움             | 용이                 |
| **에러 핸들링**          | 분산됨             | 중앙 집중            |
| **타임아웃 처리**        | 프론트에서 처리    | Spring Boot에서 처리 |
| **FastAPI 직접 노출**    | 있음               | 제한적 (일일 운세만) |

---

## 🔐 보안 고려사항

### 변경 전

- ❌ 프론트가 사용자의 모든 꿈 데이터를 FastAPI로 전송
- ❌ FastAPI가 프론트에 직접 노출됨
- ❌ CORS 설정이 복잡함

### 변경 후

- ✅ 민감한 데이터는 서버 간에만 이동
- ✅ FastAPI는 Spring Boot에서만 접근 (월간 분석)
- ✅ 프론트는 최종 결과만 수신

---

## 🎉 결론

**월간 분석은 반드시 Spring Boot를 통해야 합니다!**

### 이유:

1. 📊 **데이터 접근**: MySQL에 저장된 한 달치 데이터 필요
2. 🔐 **보안**: 민감한 데이터를 프론트가 중계하면 안 됨
3. 🎯 **단순성**: 프론트는 1번의 API 호출만
4. 🔄 **트랜잭션**: Spring Boot가 전체 프로세스 관리
5. 🛡️ **인증**: JWT 토큰으로 사용자 검증 필요

### 예외:

- **일일 운세**: 프론트 → FastAPI 직접 (실시간 분석)
- **이미지 생성**: 프론트 → FastAPI 직접 (실시간 생성)

이렇게 하면 각 기능의 특성에 맞게 최적화된 아키텍처를 구성할 수 있어요! 🚀
