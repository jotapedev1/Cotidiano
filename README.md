# Cotidiano

Aplicativo Android para organização de rotina, com metas, obrigações, cronograma semanal,
Pomodoro e um sistema de XP/níveis para incentivar a consistência.

**Integrantes:** João Pedro da Penha Santos e Cauã Ceccaroni Queiroz dos Santos

## Tecnologias

Kotlin, Room (SQLite), ViewModel + Flow/Corrotinas, Navigation Component, Material 3, ViewBinding.

## Estrutura

```
app/src/main/java/com/example/cotidiano/
├── PrincipalActivity.kt        Única Activity: barra de XP, menu inferior e navegação
├── dados/                      Camada de dados
│   ├── Repositorio.kt          Ponto único de acesso aos dados (usado pelos ViewModels)
│   ├── Sessao.kt               Guarda quem está logado
│   └── local/                  Banco Room
│       ├── BancoDeDados.kt     Configuração do banco
│       ├── AppDao.kt           Todas as consultas SQL
│       └── *.kt                Uma entidade (tabela) por arquivo
├── dominio/                    Regras puras, sem Android
│   ├── CalculadoraXp.kt        XP, níveis e itens exclusivos
│   ├── Datas.kt                Cálculo do "dia atual" (reinício das tarefas diárias)
│   └── Seguranca.kt            Hash de senha
└── ui/                         Telas, uma pasta por funcionalidade (Fragment + ViewModel)
    ├── autenticacao/           Login e cadastro
    ├── inicio/                 Aba Hoje (tarefas diárias)
    ├── cronograma/             Cronograma semanal
    ├── obrigacoes/             Checklist de obrigações
    ├── metas/                  Metas e submetas
    ├── pomodoro/               Timer Pomodoro
    └── comum/                  Código compartilhado entre telas

app/src/main/res/layout/        fragment_* (telas), item_* (linhas de lista), dialog_* (diálogos)
```

## Fluxo de dados

`Fragment (tela)  ->  ViewModel  ->  Repositorio  ->  AppDao (Room)`

A tela só exibe e envia cliques; a regra de negócio fica no Repositório e no domínio.
Cada conta tem seus próprios dados: todas as tabelas guardam o `usuarioId`.
