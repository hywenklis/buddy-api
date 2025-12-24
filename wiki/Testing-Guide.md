# 🧪 Testing Guide - Guia de Testes

Este guia detalha as estratégias de teste, ferramentas e melhores práticas utilizadas no Buddy API.

## 📋 Visão Geral

O Buddy API adota uma abordagem abrangente de testes que inclui:

- ✅ **Testes Unitários**: Testam componentes isolados
- ✅ **Testes de Integração**: Testam interação entre componentes
- ✅ **Testes de API**: Testam endpoints REST
- ✅ **Testes de Contrato**: Testam integrações com APIs externas
- ✅ **Cobertura de Código**: Monitorada com JaCoCo

## 🛠️ Ferramentas de Teste

### Stack de Testes

```
┌─────────────────────────────────────┐
│          Test Framework             │
│         JUnit 5 (Jupiter)           │
└──────────────┬──────────────────────┘
               │
┌──────────────┴──────────────────────┐
│        Mock Framework               │
│          Mockito                    │
└──────────────┬──────────────────────┘
               │
┌──────────────┴──────────────────────┐
│      Spring Test Support            │
│   @SpringBootTest, @WebMvcTest      │
└──────────────┬──────────────────────┘
               │
┌──────────────┴──────────────────────┐
│     Additional Tools                │
│  WireMock, Awaitility, H2          │
└─────────────────────────────────────┘
```

### Dependências

```gradle
dependencies {
    // Test Framework
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    
    // JUnit 5
    testImplementation 'org.junit.jupiter:junit-jupiter'
    
    // Mockito (incluído no spring-boot-starter-test)
    
    // H2 Database (para testes)
    testImplementation 'com.h2database:h2:2.3.232'
    
    // WireMock (mocking de APIs externas)
    testImplementation 'org.springframework.cloud:spring-cloud-contract-wiremock'
    
    // Awaitility (testes assíncronos)
    testImplementation 'org.awaitility:awaitility:4.3.0'
    
    // Embedded Redis (testes com Redis)
    testImplementation 'com.github.codemonstur:embedded-redis:1.4.3'
    
    // Apache Commons (utilitários)
    testImplementation 'org.apache.commons:commons-lang3:3.17.0'
}
```

## 📁 Estrutura de Testes

```
src/test/java/com/buddy/api/
├── web/                                 # Testes de Controllers
│   ├── accounts/
│   │   └── CreateAccountControllerTest.java
│   ├── pets/
│   │   └── PetControllerTest.java
│   └── authentication/
│       └── AuthControllerTest.java
├── domains/                             # Testes de Domain Services
│   ├── account/
│   │   ├── AccountServiceTest.java
│   │   └── EmailVerificationServiceTest.java
│   ├── pet/
│   │   └── PetServiceTest.java
│   └── adoption/
│       └── AdoptionRequestServiceTest.java
├── integrations/                        # Testes de Integração
│   └── clients/
│       └── manager/
│           └── ManagerClientTest.java
└── commons/                             # Testes de Componentes Comuns
    └── configurations/
        └── SecurityConfigTest.java

src/test/resources/
├── application-test.yml                 # Configuração de teste
├── data.sql                             # Dados de teste (opcional)
└── wiremock/                            # Mocks do WireMock
    └── mappings/
        └── manager-api.json
```

## 🧪 Tipos de Testes

### 1. Testes Unitários

Testam uma única classe isolada de suas dependências.

#### Exemplo: Service Test

```java
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AccountService accountService;
    
    @Test
    @DisplayName("Deve criar conta com sucesso")
    void shouldCreateAccountSuccessfully() {
        // Given
        var accountDto = new AccountDto(
            "test@example.com",
            "11987654321",
            "password123",
            true
        );
        
        var encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(i -> i.getArgument(0));
        
        // When
        var result = accountService.createAccount(accountDto);
        
        // Then
        assertNotNull(result);
        assertEquals(accountDto.email(), result.getEmail());
        verify(accountRepository).save(any(AccountEntity.class));
        verify(passwordEncoder).encode(accountDto.password());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando email já existe")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        var accountDto = new AccountDto("existing@example.com", "11987654321", "password", true);
        when(accountRepository.existsByEmail(anyString())).thenReturn(true);
        
        // When & Then
        assertThrows(EmailAlreadyExistsException.class, 
            () -> accountService.createAccount(accountDto));
        
        verify(accountRepository, never()).save(any());
    }
}
```

#### Convenções para Testes Unitários

- Use `@ExtendWith(MockitoExtension.class)` para testes com Mockito
- Mock todas as dependências com `@Mock`
- Injete a classe sob teste com `@InjectMocks`
- Use `@DisplayName` para descrições legíveis
- Siga o padrão **Given-When-Then** (Arrange-Act-Assert)

### 2. Testes de Integração

Testam a interação entre múltiplos componentes.

#### Exemplo: Repository Integration Test

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @DisplayName("Deve encontrar conta por email")
    void shouldFindAccountByEmail() {
        // Given
        var account = AccountEntity.builder()
            .email("test@example.com")
            .password("encoded")
            .consent(true)
            .build();
        entityManager.persist(account);
        entityManager.flush();
        
        // When
        var found = accountRepository.findByEmail("test@example.com");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }
}
```

### 3. Testes de API (Controller Tests)

Testam endpoints REST.

#### Exemplo: Controller Test com MockMvc

```java
@WebMvcTest(CreateAccountController.class)
class CreateAccountControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AccountService accountService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("POST /api/accounts - Deve criar conta com sucesso")
    void shouldCreateAccountSuccessfully() throws Exception {
        // Given
        var request = new AccountRequest(
            "test@example.com",
            "11987654321",
            "Password123!",
            true
        );
        
        var accountEntity = AccountEntity.builder()
            .id(UUID.randomUUID())
            .email(request.email())
            .build();
        
        when(accountService.createAccount(any())).thenReturn(accountEntity);
        
        // When & Then
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.message").value(containsString("created successfully")));
        
        verify(accountService).createAccount(any());
    }
    
    @Test
    @DisplayName("POST /api/accounts - Deve retornar 400 para dados inválidos")
    void shouldReturn400ForInvalidData() throws Exception {
        // Given
        var invalidRequest = new AccountRequest(
            "invalid-email",  // Email inválido
            "123",            // Telefone inválido
            "weak",           // Senha fraca
            false             // Consent false
        );
        
        // When & Then
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray());
    }
}
```

### 4. Testes End-to-End

Testam o fluxo completo da aplicação.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class AccountCreationE2ETest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Fluxo completo: Criar conta → Verificar email → Login")
    void completeAccountCreationFlow() {
        var baseUrl = "http://localhost:" + port + "/api";
        
        // 1. Criar conta
        var createRequest = new AccountRequest(
            "newuser@example.com",
            "11987654321",
            "SecurePass123!",
            true
        );
        
        var createResponse = restTemplate.postForEntity(
            baseUrl + "/accounts",
            createRequest,
            CreatedSuccessResponse.class
        );
        
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody().id());
        
        // 2. Verificar email (simulado)
        var verifyRequest = new ConfirmEmailRequest(
            "newuser@example.com",
            "123456"  // Código mockado
        );
        
        var verifyResponse = restTemplate.postForEntity(
            baseUrl + "/accounts/verify-email",
            verifyRequest,
            Void.class
        );
        
        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
        
        // 3. Login
        var loginRequest = new AuthRequest(
            "newuser@example.com",
            "SecurePass123!"
        );
        
        var loginResponse = restTemplate.postForEntity(
            baseUrl + "/authentication",
            loginRequest,
            AuthResponse.class
        );
        
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody().token());
    }
}
```

### 5. Testes com WireMock

Mockando APIs externas.

```java
@SpringBootTest
@AutoConfigureMockMvc
class ManagerClientIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ManagerClient managerClient;
    
    private WireMockServer wireMockServer;
    
    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }
    
    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }
    
    @Test
    @DisplayName("Deve sincronizar dados com Manager Service")
    void shouldSyncWithManagerService() {
        // Given - Mock da resposta externa
        stubFor(get(urlEqualTo("/api/data"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"success\"}")));
        
        // When
        var response = managerClient.getData();
        
        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        
        // Verify
        verify(getRequestedFor(urlEqualTo("/api/data")));
    }
}
```

## 📊 Cobertura de Código

### Configuração JaCoCo

```gradle
// build.gradle
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.10"
}

jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
        csv.required = true
    }
    
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/config/**',
                '**/dto/**',
                '**/entity/**',
                '**/*Application.class'
            ])
        }))
    }
}

test {
    finalizedBy jacocoTestReport
}
```

### Gerar Relatório de Cobertura

```bash
# Executar testes e gerar relatório
./gradlew test jacocoTestReport

# Abrir relatório
open build/reports/jacoco/test/html/index.html
```

### Metas de Cobertura

| Tipo | Meta | Atual |
|------|------|-------|
| Line Coverage | ≥ 80% | 85% |
| Branch Coverage | ≥ 70% | 75% |
| Method Coverage | ≥ 80% | 88% |

## 🎯 Boas Práticas

### 1. Nomenclatura de Testes

```java
// ❌ Ruim
@Test
void test1() { ... }

// ✅ Bom
@Test
@DisplayName("Deve criar conta quando dados são válidos")
void shouldCreateAccountWhenDataIsValid() { ... }
```

### 2. Estrutura Given-When-Then

```java
@Test
void shouldCalculateTotalPrice() {
    // Given - Preparação
    var item1 = new Item("Product 1", 10.0);
    var item2 = new Item("Product 2", 20.0);
    var cart = new Cart();
    
    // When - Ação
    cart.add(item1);
    cart.add(item2);
    var total = cart.calculateTotal();
    
    // Then - Verificação
    assertEquals(30.0, total);
}
```

### 3. Um Assert por Teste (quando possível)

```java
// ❌ Evitar múltiplos asserts não relacionados
@Test
void testMultipleThings() {
    assertEquals(1, service.method1());
    assertEquals(2, service.method2());
    assertEquals(3, service.method3());
}

// ✅ Separar em testes distintos
@Test
void shouldReturnOneForMethod1() {
    assertEquals(1, service.method1());
}

@Test
void shouldReturnTwoForMethod2() {
    assertEquals(2, service.method2());
}
```

### 4. Usar AssertJ para Asserções Fluentes

```java
// Com JUnit
assertEquals("John", user.getName());
assertTrue(user.getAge() > 18);

// Com AssertJ (mais legível)
assertThat(user)
    .extracting(User::getName, User::getAge)
    .containsExactly("John", 25);

assertThat(user.getAge()).isGreaterThan(18);
```

### 5. Testar Cenários de Erro

```java
@Test
@DisplayName("Deve lançar exceção quando ID não existe")
void shouldThrowExceptionWhenIdNotFound() {
    // Given
    var nonExistentId = UUID.randomUUID();
    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(EntityNotFoundException.class,
        () -> service.findById(nonExistentId));
}
```

### 6. Usar @Nested para Organizar Testes

```java
@DisplayName("Account Service Tests")
class AccountServiceTest {
    
    @Nested
    @DisplayName("Create Account")
    class CreateAccountTests {
        
        @Test
        @DisplayName("Deve criar conta com sucesso")
        void shouldCreateSuccessfully() { ... }
        
        @Test
        @DisplayName("Deve falhar quando email já existe")
        void shouldFailWhenEmailExists() { ... }
    }
    
    @Nested
    @DisplayName("Verify Email")
    class VerifyEmailTests {
        
        @Test
        @DisplayName("Deve verificar email com código válido")
        void shouldVerifyWithValidCode() { ... }
    }
}
```

## 🚀 Executar Testes

### Comandos Gradle

```bash
# Executar todos os testes
./gradlew test

# Executar testes de uma classe específica
./gradlew test --tests AccountServiceTest

# Executar teste específico
./gradlew test --tests AccountServiceTest.shouldCreateAccountSuccessfully

# Executar com mais detalhes
./gradlew test --info

# Executar testes em paralelo
./gradlew test --parallel --max-workers=4

# Executar apenas testes unitários (por convenção)
./gradlew test --tests *Test

# Executar apenas testes de integração (por convenção)
./gradlew test --tests *IT
```

### Executar via IDE

**IntelliJ IDEA**:
- Clique direito em um teste → "Run"
- Clique direito em um pacote → "Run All Tests"
- Use `Ctrl+Shift+F10` (Windows/Linux) ou `Cmd+Shift+R` (macOS)

## 🔍 Debug de Testes

### Logging em Testes

```yaml
# src/test/resources/application-test.yml
logging:
  level:
    com.buddy.api: DEBUG
    org.springframework.test: DEBUG
```

### Breakpoints

1. Coloque um breakpoint no teste
2. Clique direito → "Debug"
3. Use o debugger para inspecionar variáveis

## 📈 CI/CD Integration

### GitHub Actions

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Run tests
        run: ./gradlew test
      
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: ./build/reports/jacoco/test/jacocoTestReport.xml
```

## 📚 Recursos Adicionais

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ](https://assertj.github.io/doc/)

## 📚 Próximos Passos

- **[Development Guide](./Development-Guide.md)** - Guia de desenvolvimento
- **[Code Standards](./Code-Standards.md)** - Padrões de código
- **[CI/CD Guide](./CICD-Guide.md)** - Integração contínua

---

**Mantido por**: @hywenklis | **Última atualização**: Dezembro 2024
