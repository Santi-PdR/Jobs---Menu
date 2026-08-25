# Jobs · Menú de Turno

Mod **de cliente** que reemplaza los menús de Minecraft por los del servidor **Jobs**: un tablón de turnos
atornillado a la pared de un depósito, bajo una lámpara de sodio que parpadea, con la próxima aparición de
los **Executores** ya agendada en la esquina.

No añade objetos, ni entidades, ni mecánicas. Sólo cambia lo que ves antes de entrar a trabajar.

| | |
|---|---|
| Versión | **0.1.0** |
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente (el servidor no necesita el mod) |

## Qué trae la 0.1.0

- Pantalla de título propia: cabecera, cuatro renglones de tablón, sello de turno.
- Escena de fondo procedural: pasillo, lámpara con parpadeo, lluvia, polvo y una silueta que cruza cada tanto.
- Cuenta regresiva a la próxima aparición (ciclo de 13 minutos).
- Avisos rotativos de la empresa.
- Configuración de cliente con interruptores de accesibilidad (movimiento reducido, destellos reducidos,
  interfaz mínima).

## Compilar

Requiere JDK 17 instalado.

```powershell
.\gradlew build
```

El `.jar` queda en `build\libs\jobsmenu-0.1.0.jar`. Se copia a la carpeta `mods` de la instancia.

> Si `gradle\wrapper\gradle-wrapper.jar` no existe todavía, el bloque de despliegue de abajo lo descarga solo.

## Verificación estática

Sin necesidad de JDK:

```powershell
python tools\verificar.py
```

Comprueba versiones sincronizadas, claves de idioma, JSON válido y balance de llaves en el código.

## Documentación

Todo el diseño —identidad, paleta, voz, alcance por fases y reglas de trabajo— está en
[`CONTEXTO.md`](CONTEXTO.md).
