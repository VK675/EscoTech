# EscoTech

Plataforma gamificada para aprender C#. Os alunos respondem a desafios e ganham XP; os professores criam desafios e atribuem-nos às turmas.

Kotlin · layouts XML com ViewBinding · Firebase Authentication + Firestore · RecyclerView em todas as listas.

## Configurar o Firebase (obrigatório)

Sem este passo o projeto **não compila** (falta o `google-services.json`).

1. Abre <https://console.firebase.google.com> e cria um projeto (ex.: "EscoTech").
2. Clica em **Adicionar app → Android**. Nome do pacote: `com.example.escotech`.
3. Descarrega o `google-services.json` e coloca-o na pasta **`app/`** deste projeto.
4. Em **Authentication → Método de início de sessão**, ativa **Email/palavra-passe**.
5. Em **Firestore Database**, clica em **Criar base de dados** (podes escolher a região `eur3`).
6. Em **Firestore → Regras**, cola o conteúdo de `firestore.rules` e clica em **Publicar**.
7. No Android Studio: **Sync Project with Gradle Files** e depois **Run**.

## Experimentar

1. Cria uma conta de **Professor** e toca em **Adicionar desafios de exemplo** (6 desafios de C#).
2. Toca em **Atribuir**, escreve a turma (ex.: `12A`), escolhe um prazo e um desafio.
3. Sai e cria uma conta de **Aluno** na turma `12A`. O desafio atribuído aparece com o prazo.
4. Responde a desafios para ganhar XP, subir de nível e aparecer no ranking.

## Estrutura

```
app/src/main/java/com/example/escotech/
├── LoginActivity.kt             Login + redirecionamento pelo papel (role)
├── RegisterActivity.kt          Registo (nome, email, palavra-passe, papel, turma)
├── StudentHomeActivity.kt       Saudação, barra de XP, lista de desafios
├── ChallengeActivity.kt         Pergunta, 4 opções, confirmar, ganhar XP
├── LeaderboardActivity.kt       Ranking dos alunos por XP
├── StudentProfileActivity.kt    Avatar, nível, XP, desafios feitos, dias seguidos
├── TeacherDashboardActivity.kt  Estatísticas, atribuições, desafios criados
├── CreateChallengeActivity.kt   Formulário de novo desafio
├── AssignChallengeActivity.kt   Atribuir desafio a uma turma com prazo
├── model/                       User, Challenge, Assignment, Submission
├── adapter/                     Adapters das RecyclerViews
└── util/                        Funções de apoio e desafios de exemplo
```

## Dados no Firestore

| Coleção | Campos |
|---|---|
| `users/{uid}` | name, email, role (`student`/`teacher`), xp, level, avatarId, **classId** |
| `challenges/{id}` | title, description, question, options (4), correctAnswer (0–3), xpReward, difficulty, createdBy |
| `assignments/{id}` | challengeId, classId, deadline, createdBy |
| `submissions/{uid_challengeId}` | userId, challengeId, isCorrect, xpEarned, completedAt |

Notas:
- `classId` foi acrescentado aos utilizadores para ligar alunos às atribuições da turma.
- O id de cada resposta é `uid_challengeId`, por isso cada aluno só responde **uma vez** a cada desafio.
- Nível = XP ÷ 100 + 1 (cada nível precisa de 100 XP).
- **Alunos ativos** = alunos que responderam nos últimos 7 dias. **Taxa de conclusão** = respostas dos alunos das turmas ÷ total esperado nas atribuições do professor.

## Limitações (projeto escolar)

- A resposta certa é verificada na própria app, por isso um aluno com conhecimentos técnicos podia lê-la no Firestore. Para evitar isso seria preciso uma Cloud Function.
- Qualquer pessoa pode registar-se como professor.
