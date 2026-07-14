# ScaffoldingTentShop

```
=============================================
  ___ScaffoldingTentShop___
  Powered by Kame
  Discord: code.tg
=============================================
```

Plugin de lojas jogadores para Minecraft. Crie lojas físicas no mundo usando scaffolding + carpet, com hologramas, GUI completo e economia via Vault.

## Funcionalidades

- **Criação por interação** — Segure o item configurado e clique no scaffolding para criar
- **Hologramas configuráveis** — 3 linhas personalizáveis + timer de expiração visível
- **6 GUIs** — Navegar lojas, comprar, vender, configurar, adicionar produto, admin
- **Economia via Vault** — Compre e venda itens entre jogadores
- **Sistema de expiração** — Lojas expiram automaticamente e dropam os itens
- **Sons em interações** — Feedback sonoro ao adicionar/remover itens
- **Proteção** — Bloqueio de drag/ctrl+click indevido no painel de produtos
- **Confirmação no chat** — Criação de loja com confirmação "sim/não"

## Requisitos

- Minecraft **1.21+** (Paper ou Spigot)
- **Vault**
- Plugin de economia (EssentialsX, CMI, etc.)

## Instalação

1. Baixe o arquivo `.jar` da pasta `target/`
2. Coloque na pasta `plugins/` do seu servidor
3. Reinicie o servidor
4. Edite `plugins/PlayserShops/config.yml` conforme necessário

## Como Criar uma Loja

1. Coloque um **scaffolding** no local desejado
2. Coloque um **carpet** de qualquer cor em cima do scaffolding
3. **Segure o item de ativação** (padrão: esmeralda)
4. Clique com **botão direito** no scaffolding
5. Aparecerá no chat: *"Deseja criar uma loja por $1000?"*
6. Digite **sim** para confirmar ou **não** para cancelar

> A loja será criada com um holograma flutuante mostrando nome, mensagem e tempo restante.

## Como Usar

### Dono da Loja
- Clique com botão direito no carpet da loja para **configurar**
- Use `/lojinha` para ver suas lojas
- Renomeie, altere mensagem, adicione produtos e defina preços

### Visitante
- Clique com botão direito no carpet da loja para **comprar/vender**
- **Click esquerdo** = Comprar item (paga ao dono)
- **Click direito** = Vender item (recebe do dono)

### Administrador
- Use `/lojinha admin` para gerenciar todas as lojas
- Clique direito deleta loja, clique esquerdo abre config

## Comandos

| Comando | Descrição | Permissão |
|---|---|---|
| `/lojinha` | Abrir GUI das suas lojas | Todos |
| `/lojinha admin` | Painel administrativo | `playsershops.admin` |

## Permissões

| Permissão | Descrição | Padrão |
|---|---|---|
| `playsershops.admin` | Acesso ao painel admin | `op` |

## Configuração

Edite `plugins/PlayserShops/config.yml`:

| Config | Tipo | Padrão | Descrição |
|---|---|---|---|
| `shop-duration` | long | `10800` | Duração da loja em segundos (3 horas) |
| `shop-creation-cost` | double | `1000.0` | Custo para criar uma loja (0 = grátis) |
| `max-shops-per-player` | int | `3` | Máximo de lojas por jogador |
| `creation-item` | String | `EMERALD` | Material do item de ativação |
| `hologram.offset-y` | double | `2.5` | Altura do holograma acima do scaffolding |
| `hologram.show-timer` | boolean | `true` | Mostrar timer de expiração no holograma |
| `hologram.timer-line` | String | `"&7⏱ &f%time_remaining%"` | Texto da linha do timer |
| `hologram.lines[0]` | String | `"&b&l[PLAYER SHOP]"` | Primeira linha do holograma |
| `hologram.lines[1]` | String | `"&f%shop_name%"` | Segunda linha (suporta `%shop_name%`) |
| `hologram.lines[2]` | String | `"&e%shop_message%"` | Terceira linha (suporta `%shop_message%`) |

> Todas as strings de cor suportam `&` e hex `#RRGGBB`.

## Créditos

- **Kame** — Desenvolvedor
- **Discord:** code.tg

---
© 2026 Kame. Todos os direitos reservados.
