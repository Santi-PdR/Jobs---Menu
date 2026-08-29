# Revision de reload y presentacion

## Estado

Esta revision parte de `0.10.0` y no declara una compilacion o una prueba dentro
de Minecraft. La validacion disponible en Linux es estatica; el entorno no tiene
Java 17.

## Crash despues de cambiar el idioma

El cambio de idioma dispara una recarga de recursos y por eso coincide en el
tiempo con la reconstruccion de `MusicEngine` y `SoundEngine`. El crash observado
no contiene una excepcion Java ni un stack de Jobs Menu: termina en
`EXCEPTION_ACCESS_VIOLATION (0xc0000005)` dentro de `nvapi64.dll`. Eso apunta a
un fallo nativo del controlador NVIDIA, pero no permite afirmar que el mod sea
irrelevante ni que el idioma sea la causa.

La hipotesis correcta sigue siendo: el cambio de idioma puede ser el disparador
temporal que expone un problema del driver o una carrera durante el reload. Para
separarlos hace falta el `hs_err_pid7744.log` completo y una reproduccion
controlada en Windows.

## Crash de registro de sonidos reportado en runtime — 2026-08-29

El reporte nuevo es distinto del fallo nativo anterior: ocurre en el hilo de
render, dentro de `PantallaEstancia` al pintar `RenglonTablon`, y termina en
`MezclaAudio.gesto` cuando llama a `SonidosNivel.UI_PASAR.get()`. Forge informa
`Registry Object not present: jobsmenu:ui.pasar`. El proceso estaba usando
Forge 47.4.10, Java 17 y `jobsmenu-0.9.0.jar`, junto con Embeddium/Oculus y
otros mods gráficos. La causa inmediata del crash es el acceso obligatorio a
un `RegistryObject` ausente; no es necesario atribuirlo a Embeddium para
corregirlo.

La rama actual corrige el camino completo: `MezclaAudio.resolver` comprueba
`isPresent()` antes de usar `get()`, conserva el click vanilla como respaldo
para los gestos, omite una cama ambiental cuyo registro falte y usa la música
vanilla solo como último respaldo del constructor de la pista. El aviso se
registra una sola vez para no inundar `latest.log`. Esto evita que un fallo de
registro convierta el render de un widget en un crash.

El reporte confirma un fallo runtime real del snapshot 0.9.0. La correccion
esta en el codigo 0.10.0 de esta rama, pero aun requiere compilar e instalar el
JAR correcto y repetir la prueba en Minecraft; esta maquina no puede certificar
esa reproduccion porque no tiene Java.

## Cambios de estabilidad

- `RecargaRecursosCliente` coalesce solicitudes de la misma tanda y encola el
  cierre mediante `Minecraft.execute(...)`; no toca instancias de sonido desde
  el executor de recursos.
- `GestorMusica.recursosRecargados()` invalida la instancia anterior antes de que
  otra visita pueda crearla y limpia el estado del credito.
- `GestorAmbiente.recursosRecargados()` detiene las capas y vacia la coleccion de
  instancias; `abrir()` sigue siendo idempotente frente a resize y recreacion de
  pantallas.
- El credito predeterminado no se activa por una pista local. Solo un recurso
  empaquetado con `musica_creditada.txt` habilita las cadenas de credito.
- La lista de `PantallaAjustesAviso` ya no coloca tres opciones en una fila
  pequena de `OptionsList`; la tercera queda como entrada grande.

Estas medidas reducen el riesgo del listener/audio, pero no son una prueba de
que el driver NVIDIA este sano.

## Pasada visual

Se agregaron detalles arquitectonicos discretos y propios de cada recinto, sin
convertirlos en overlays ni ocupar la zona de lectura:

- grua suspendida en Deposito;
- panel de control en Servicio;
- escalera en Biblioteca;
- estructura superior de vidrio en Invernadero;
- ondas de agua en Cisterna.

La implementacion Java y `tools/vista_previa.py` se mantienen en espejo. Los
props usan alfa bajo y movimiento minimo para conservar contraste, foco y modo
de movimiento reducido.

## Validacion ejecutada

```text
python3 tools/verificar.py
Verificacion superada. 0 aviso(s), ningun fallo.
```

Tambien se ejecuto `git diff --check`. No se ejecuto Gradle porque el entorno
actual no dispone de Java 17.

## Reproduccion pendiente en Windows

1. Copiar el `hs_err_pid7744.log` junto con el crash report, sin truncar la
   seccion de `Problematic frame`, `Native frames` y `VM Arguments`.
2. Repetir con el driver NVIDIA actualizado y con los mods graficos opcionales
   aislados; registrar si el fallo sigue en `nvapi64.dll`.
3. Probar cambio de idioma con sonido del mod activo, luego con musica y
   ambiente desactivados, y finalmente sin Jobs Menu. No cambiar varias
   variables a la vez.
4. Probar `F3+T` en el mismo menu y anotar si el resultado difiere del cambio de
   idioma.
5. Guardar `latest.log`, el crash report y el `hs_err` de cada variante.
6. Confirmar que tras el reload no quedan dos instancias de musica ni capas
   ambientales duplicadas y que el credito no aparece para una pista local no
   identificada.

Un resultado limpio con Jobs Menu desactivado pero fallo con otros mods graficos
mantendria como principal al driver. Un fallo que solo aparece con el mod
activo, junto con errores del `SoundEngine`, justificaria seguir aislando el
listener/audio antes de atribuirlo a NVIDIA.
