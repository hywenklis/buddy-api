# 📚 API Documentation - Documentação Completa da API

Esta página fornece uma referência completa de todos os endpoints disponíveis na Buddy API, incluindo exemplos de requisição e resposta.

## 🌐 Base URL

### Desenvolvimento Local
```
http://localhost:8080/api
```

### Produção
```
https://buddy.propresto.app/api
```

### Swagger UI (Documentação Interativa)
```
http://localhost:8080/api/swagger-ui/index.html
```

## 🔐 Autenticação

A maioria dos endpoints requer autenticação via JWT (JSON Web Token).

### Como Autenticar

1. **Criar uma conta** ou fazer **login**
2. Receber o **token JWT** na resposta
3. Incluir o token no header de todas as requisições protegidas:

```http
Authorization: Bearer {seu-token-jwt}
```

### Endpoints Públicos (Não Requerem Autenticação)

- `POST /api/accounts` - Criar conta
- `POST /api/accounts/verify-email` - Verificar email
- `POST /api/authentication` - Login
- `GET /api/pets` - Listar pets
- `GET /api/pets/{id}` - Obter detalhes de um pet

## 📋 Endpoints

### 🔑 Authentication (Autenticação)

#### Login

Autentica um usuário e retorna um token JWT.

**Endpoint**: `POST /api/authentication`

**Request Body**:
```json
{
  "email": "usuario@example.com",
  "password": "SenhaSegura123!"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400,
  "profile": {
    "id": "uuid",
    "name": "Nome do Usuário",
    "email": "usuario@example.com",
    "role": "USER"
  }
}
```

**Erros Possíveis**:
- `401 Unauthorized` - Credenciais inválidas
- `403 Forbidden` - Conta bloqueada ou não verificada

---

### 👤 Accounts (Contas)

#### Criar Conta

Cria uma nova conta de usuário e envia email de verificação.

**Endpoint**: `POST /api/accounts`

**Request Body**:
```json
{
  "email": "novo@example.com",
  "phone": "11987654321",
  "password": "SenhaSegura123!",
  "consent": true
}
```

**Validações**:
- Email: deve ser válido e único
- Telefone: formato brasileiro (11 dígitos)
- Senha: mínimo 8 caracteres, letras e números
- Consent: deve ser `true` (aceite dos termos)

**Response** (201 Created):
```json
{
  "id": "uuid-gerado",
  "message": "Account created successfully. Please verify your email.",
  "email": "novo@example.com"
}
```

**Erros Possíveis**:
- `400 Bad Request` - Dados inválidos
- `409 Conflict` - Email já cadastrado

#### Verificar Email

Confirma o email através do código enviado.

**Endpoint**: `POST /api/accounts/verify-email`

**Request Body**:
```json
{
  "email": "usuario@example.com",
  "code": "123456"
}
```

**Response** (200 OK):
```json
{
  "message": "Email verified successfully",
  "verified": true
}
```

**Erros Possíveis**:
- `400 Bad Request` - Código inválido ou expirado
- `404 Not Found` - Conta não encontrada

---

### 🐾 Pets (Animais)

#### Listar Pets

Lista todos os pets disponíveis para adoção com filtros e paginação.

**Endpoint**: `GET /api/pets`

**Query Parameters**:
```
?page=0                    # Número da página (default: 0)
&size=20                   # Itens por página (default: 20, max: 100)
&species=DOG               # Filtro por espécie (DOG, CAT, BIRD, etc.)
&gender=MALE               # Filtro por gênero (MALE, FEMALE)
&size=MEDIUM               # Filtro por porte (SMALL, MEDIUM, LARGE)
&age=ADULT                 # Filtro por idade (PUPPY, YOUNG, ADULT, SENIOR)
&location=São Paulo        # Filtro por localização
&shelterId=uuid            # Filtro por abrigo
```

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Rex",
      "species": "DOG",
      "breed": "Labrador",
      "gender": "MALE",
      "age": "ADULT",
      "size": "LARGE",
      "description": "Cão amigável e brincalhão",
      "location": "São Paulo, SP",
      "shelter": {
        "id": "uuid",
        "name": "Abrigo Amigo dos Animais"
      },
      "images": [
        {
          "id": "uuid",
          "url": "https://example.com/image1.jpg",
          "isPrimary": true
        }
      ],
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

#### Obter Detalhes de um Pet

Retorna informações detalhadas de um pet específico.

**Endpoint**: `GET /api/pets/{id}`

**Path Parameters**:
- `id` (UUID) - ID do pet

**Response** (200 OK):
```json
{
  "id": "uuid",
  "name": "Rex",
  "species": "DOG",
  "breed": "Labrador",
  "gender": "MALE",
  "birthDate": "2020-05-15",
  "age": "ADULT",
  "size": "LARGE",
  "color": "Marrom",
  "description": "Cão muito amigável, castrado e vacinado",
  "specialNeeds": null,
  "vaccinated": true,
  "neutered": true,
  "location": "São Paulo, SP",
  "adoptionStatus": "AVAILABLE",
  "shelter": {
    "id": "uuid",
    "name": "Abrigo Amigo dos Animais",
    "phone": "11987654321",
    "email": "contato@abrigo.com"
  },
  "images": [
    {
      "id": "uuid",
      "url": "https://example.com/image1.jpg",
      "description": "Foto frontal",
      "isPrimary": true
    },
    {
      "id": "uuid",
      "url": "https://example.com/image2.jpg",
      "description": "Foto lateral",
      "isPrimary": false
    }
  ],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Erros Possíveis**:
- `404 Not Found` - Pet não encontrado

#### Criar Pet (Requer Autenticação - SHELTER)

Cadastra um novo pet no abrigo.

**Endpoint**: `POST /api/pets`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "name": "Rex",
  "species": "DOG",
  "breed": "Labrador",
  "gender": "MALE",
  "birthDate": "2020-05-15",
  "size": "LARGE",
  "color": "Marrom",
  "description": "Cão muito amigável",
  "specialNeeds": null,
  "vaccinated": true,
  "neutered": true,
  "shelterId": "uuid-do-abrigo"
}
```

**Response** (201 Created):
```json
{
  "id": "uuid-gerado",
  "message": "Pet created successfully",
  "pet": {
    "id": "uuid-gerado",
    "name": "Rex",
    "species": "DOG"
  }
}
```

#### Atualizar Pet (Requer Autenticação - SHELTER)

Atualiza informações de um pet.

**Endpoint**: `PUT /api/pets/{id}`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**: (mesma estrutura do POST, todos os campos opcionais)

**Response** (200 OK):
```json
{
  "message": "Pet updated successfully",
  "pet": {
    "id": "uuid",
    "name": "Rex Atualizado"
  }
}
```

#### Deletar Pet (Requer Autenticação - SHELTER)

Remove um pet do sistema.

**Endpoint**: `DELETE /api/pets/{id}`

**Response** (204 No Content)

---

### 🏠 Shelters (Abrigos)

#### Criar Abrigo (Requer Autenticação)

Cadastra um novo abrigo.

**Endpoint**: `POST /api/shelters`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "name": "Abrigo Amigo dos Animais",
  "description": "Abrigo dedicado ao resgate e adoção responsável",
  "phone": "11987654321",
  "email": "contato@abrigo.com",
  "website": "https://abrigo.com",
  "address": {
    "street": "Rua das Flores",
    "number": "123",
    "complement": "Sala 1",
    "neighborhood": "Centro",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01234-567",
    "country": "Brasil"
  },
  "capacity": 50,
  "foundedDate": "2015-03-20"
}
```

**Response** (201 Created):
```json
{
  "id": "uuid-gerado",
  "message": "Shelter created successfully",
  "shelter": {
    "id": "uuid",
    "name": "Abrigo Amigo dos Animais"
  }
}
```

#### Listar Abrigos

Lista todos os abrigos cadastrados.

**Endpoint**: `GET /api/shelters`

**Query Parameters**:
```
?page=0&size=20
&city=São Paulo
&state=SP
```

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Abrigo Amigo dos Animais",
      "description": "Descrição breve",
      "city": "São Paulo",
      "state": "SP",
      "phone": "11987654321",
      "petsCount": 25
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

---

### 👥 Profiles (Perfis)

#### Obter Perfil do Usuário (Requer Autenticação)

Retorna o perfil do usuário autenticado.

**Endpoint**: `GET /api/profiles/me`

**Headers**:
```
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "id": "uuid",
  "accountId": "uuid",
  "firstName": "João",
  "lastName": "Silva",
  "dateOfBirth": "1990-05-15",
  "phone": "11987654321",
  "address": {
    "street": "Rua das Flores",
    "number": "123",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01234-567"
  },
  "about": "Amante de animais",
  "hasExperience": true,
  "hasPets": true,
  "housingType": "APARTMENT",
  "createdAt": "2024-01-10T08:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

#### Atualizar Perfil (Requer Autenticação)

Atualiza o perfil do usuário.

**Endpoint**: `PUT /api/profiles/me`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**: (todos os campos opcionais)
```json
{
  "firstName": "João",
  "lastName": "Silva",
  "dateOfBirth": "1990-05-15",
  "phone": "11987654321",
  "about": "Amante de animais e defensor da adoção responsável"
}
```

**Response** (200 OK):
```json
{
  "message": "Profile updated successfully",
  "profile": {
    "id": "uuid",
    "firstName": "João",
    "lastName": "Silva"
  }
}
```

---

### 📝 Adoption Requests (Pedidos de Adoção)

#### Criar Pedido de Adoção (Requer Autenticação)

Cria um pedido de adoção para um pet específico.

**Endpoint**: `POST /api/adoption-requests`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "petId": "uuid-do-pet",
  "message": "Gostaria de adotar este pet porque...",
  "questionnaire": {
    "hasExperience": true,
    "hasPets": false,
    "housingType": "HOUSE",
    "hasYard": true,
    "householdMembers": 4,
    "allergies": false,
    "workSchedule": "9 to 5",
    "reason": "Sempre quis ter um cachorro e agora tenho condições"
  }
}
```

**Response** (201 Created):
```json
{
  "id": "uuid-gerado",
  "message": "Adoption request created successfully",
  "status": "PENDING"
}
```

#### Listar Meus Pedidos (Requer Autenticação)

Lista todos os pedidos de adoção do usuário.

**Endpoint**: `GET /api/adoption-requests/me`

**Headers**:
```
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "uuid",
      "pet": {
        "id": "uuid",
        "name": "Rex",
        "species": "DOG"
      },
      "status": "PENDING",
      "createdAt": "2024-01-20T14:00:00Z",
      "updatedAt": "2024-01-20T14:00:00Z"
    }
  ]
}
```

#### Atualizar Status do Pedido (Requer Autenticação - SHELTER)

Aprova, rejeita ou atualiza o status de um pedido de adoção.

**Endpoint**: `PATCH /api/adoption-requests/{id}/status`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "status": "APPROVED",
  "notes": "Pedido aprovado após análise do questionário"
}
```

**Valores possíveis para status**:
- `PENDING` - Pendente
- `UNDER_REVIEW` - Em análise
- `APPROVED` - Aprovado
- `REJECTED` - Rejeitado
- `CANCELLED` - Cancelado
- `COMPLETED` - Concluído

**Response** (200 OK):
```json
{
  "message": "Status updated successfully",
  "adoptionRequest": {
    "id": "uuid",
    "status": "APPROVED"
  }
}
```

---

## 📊 Respostas de Erro Padrão

Todas as respostas de erro seguem o mesmo formato:

```json
{
  "timestamp": "2024-01-20T15:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/accounts",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    },
    {
      "field": "password",
      "message": "Password must be at least 8 characters"
    }
  ]
}
```

### Códigos de Status HTTP

| Código | Significado | Quando Ocorre |
|--------|-------------|---------------|
| 200 | OK | Requisição bem-sucedida |
| 201 | Created | Recurso criado com sucesso |
| 204 | No Content | Recurso deletado com sucesso |
| 400 | Bad Request | Dados inválidos na requisição |
| 401 | Unauthorized | Token inválido ou ausente |
| 403 | Forbidden | Sem permissão para acessar o recurso |
| 404 | Not Found | Recurso não encontrado |
| 409 | Conflict | Conflito (ex: email já existe) |
| 422 | Unprocessable Entity | Erro de validação de negócio |
| 429 | Too Many Requests | Rate limit excedido |
| 500 | Internal Server Error | Erro interno do servidor |

---

## 🔄 Paginação

Todos os endpoints de listagem suportam paginação através dos parâmetros:

```
?page=0       # Número da página (começa em 0)
&size=20      # Itens por página (max: 100)
&sort=name,asc # Ordenação (campo,direção)
```

**Resposta com paginação**:
```json
{
  "content": [...],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

---

## 🔍 Filtros e Busca

Muitos endpoints suportam filtros via query parameters. Exemplo:

```
GET /api/pets?species=DOG&size=LARGE&location=São Paulo
```

Para busca textual, use o parâmetro `search`:

```
GET /api/pets?search=labrador
```

---

## 🚦 Rate Limiting

Para proteger a API, aplicamos rate limiting:

- **Limite**: 100 requisições por minuto por IP
- **Header de resposta**: `X-RateLimit-Remaining`
- **Quando excedido**: 429 Too Many Requests

---

## 📚 Próximos Passos

- **[Getting Started](./Getting-Started.md)** - Como começar a usar a API
- **[Authentication Guide](./Authentication-Guide.md)** - Guia detalhado de autenticação
- **[Integration Guide](./Integration-Guide.md)** - Como integrar com a API

---

**Para testar a API**: Use o [Swagger UI](http://localhost:8080/api/swagger-ui/index.html) para explorar e testar todos os endpoints interativamente!
