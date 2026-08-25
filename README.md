# Jobs · Aviso a los ocupantes

Mod **de cliente** que reemplaza los menús de Minecraft por los del servidor **Jobs**: un aviso fotocopiado
y pegado con cinta a la pared de un pasillo amarillo que no se termina. Dice en qué nivel estás, cuánto
cuesta la salida al siguiente, y cuánto falta para la próxima ronda de los **Executores**.

Al fondo del pasillo hay un vano oscuro. Cada tanto algo lo cruza.

No añade objetos, ni entidades, ni mecánicas. Sólo cambia lo que ves antes de entrar a trabajar.

![Vista previa del menú](docs/vista_previa.png)

| | |
|---|---|
| Versión | **0.1.0** |
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente (el servidor no necesita el mod) |

## Qué trae la 0.1.0

- Pantalla de título propia: la hoja del aviso, con nivel actual, tarifa de salida y cuatro renglones de
  formulario con casilla marcable.
- Pasillo procedural en perspectiva de un punto: papel mural, cielorraso, alfombra, humedades, hilera de
  fluorescentes con parpadeo desfasado y el vano del fondo.
- Cuenta regresiva a la próxima ronda (ciclo de 13 minutos). Al llegar a cero, la luz cae.
- Avisos rotativos de la administración.
- Configuración de cliente con interruptores de accesibilidad.

## Compilar

Requiere JDK 17 instalado.

```powershell
.\gradlew build
```

El `.jar` queda en `build\libs\jobsmenu-0.1.0.jar` y se copia a la carpeta `mods` de la instancia.

> Si `gradle\wrapper\gradle-wrapper.jar` no existe todavía, el bloque de despliegue lo descarga solo.

## Herramientas sin JDK

```powershell
python tools\verificar.py       # versiones, idiomas, JSON, ASCII, balance de llaves
python tools\vista_previa.py    # dibuja el menú a PNG para revisar la escena
```

## Documentación

Todo el diseño —canon del servidor, identidad, paleta, voz, alcance por fases y reglas de trabajo— está en
[`CONTEXTO.md`](CONTEXTO.md).
