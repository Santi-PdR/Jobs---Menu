# Auditoría 0.42.0 — Compatibilidad de terceros

Fecha: 2026-09-06  
Versión: **0.42.0**  
Artefacto esperado: **`jobsmenu-0.42.0.jar`**

## Objetivo

Reducir la intervención de Jobs fuera de sus propias superficies y evitar que nuevas integraciones del modpack requieran listas de compatibilidad por nombre de mod.

## Hallazgo 1 — aislamiento demasiado específico

0.41.1 protegía explícitamente Video Settings y paquetes conocidos de Sodium/Embeddium/Iris. Ese enfoque seguía teniendo un límite: una configuración de otro mod podía recibir banda, transición o sustitución de clicks Jobs si su namespace no estaba en la lista.

### Corrección

`EscuchaCliente.esPantallaTerceros()` clasifica por propiedad del tipo:

- Jobs → `com.santipdr.jobsmenu.client.screen.*`;
- Minecraft → `net.minecraft.*`;
- Forge → `net.minecraftforge.*`;
- cualquier otro namespace → tercero.

`esSuperficieAjenaIntocable()` añade también `VideoSettingsScreen` vanilla.

Las Screens externas ya no reciben render, transiciones, hover/click Jobs ni gestión de listas/pulido.

## Hallazgo 2 — una Screen externa podía propagar redirecciones Jobs

`SesionMenu.activa()` permanecía verdadera mientras se navegaba fuera de gameplay. Una GUI de tercero podía abrir una Screen vanilla y el antiguo `flujoAdministrativo` interpretarla como navegación Jobs.

### Corrección

El flujo administrativo exige ahora:

```java
boolean flujoAdministrativo = !esPantallaTerceros(anterior) && (...);
```

La pantalla anterior es la frontera de propiedad. Si un mod inicia el cambio, Jobs no sustituye su destino por Options/Worlds/Multiplayer/Mods propios.

## Hallazgo 3 — el botón gráfico podía cambiar de etiqueta

0.41.1 localizaba el backend por `options.video`. Eso conserva muchos mixins, pero un mod puede reemplazar el botón completo y usar otra etiqueta.

### Corrección

La primera captura memoriza `x`, `y`, ancho y alto del control natural. Tras `Init.Post`, si el original desapareció, Jobs puede reconocer un `AbstractButton` sustituto que ocupa aproximadamente la misma ranura.

No se importan clases de ningún proveedor y no se construye ninguna Screen alternativa. Si no hay control natural resoluble, se rechaza el click con feedback Jobs en lugar de abrir una GUI incompleta.

## Hallazgo 4 — `dev-latest` tenía release nueva y tag viejo

La release 0.41.1 estaba actualizada, pero `refs/tags/dev-latest` seguía apuntando a un commit histórico. `target_commitish: main` de la release no movía por sí solo el ref Git existente.

### Corrección

El workflow añade después de preparar/subir el artefacto:

```bash
git tag -f dev-latest "$GITHUB_SHA"
git push origin refs/tags/dev-latest --force
```

El paso es exclusivo de `main`. `tools/verificar_version.py` exige tanto el nombre del paso como ambos comandos.

## Verificadores

- nuevo `tools/verificar_compatibilidad_042.py`;
- `tools/verificar_ui_musica.py` actualizado al aislamiento proveedor-agnóstico;
- `tools/verificar_version.py` protege movimiento del tag;
- se retira `tools/verificar_graficos_041.py`, cuyo alcance era específico de la iteración anterior;
- permanecen runtime 0.41, identidad musical 0.40, reload 0.39, optimización 0.38 y continuidad Multiplayer.

## Contratos preservados

- Forge 1.20.1 / Java 17 / client-side;
- gameplay sin transiciones/audio de menú;
- música Jobs sin fallback vanilla;
- FX puntuales con hard-stop;
- Multiplayer con selección+scroll persistentes en F5/resize;
- servidor oficial único;
- PNG 10–17 estáticos;
- JPG 18–31 con movimiento sólo sutil/desactivable;
- controles visuales Jobs sin hitboxes externas superpuestas.

## Aceptación final requerida

Después del merge deben cumplirse simultáneamente:

1. PR CI verde;
2. main CI verde;
3. paso `Move rolling development tag` verde;
4. `refs/tags/dev-latest` resuelve exactamente al SHA de `main` publicado;
5. release `dev-latest` contiene exactamente `jobsmenu-0.42.0.jar`;
6. digest SHA-256 del asset verificado;
7. prueba manual en `test-1` de Gráficos y al menos una GUI externa de otro mod.
