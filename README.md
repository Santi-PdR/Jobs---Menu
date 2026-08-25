# Jobs · Aviso a los ocupantes

Mod **de cliente** que reemplaza los menús de Minecraft por los del servidor **Jobs**: un aviso fotocopiado
y pegado con cinta a la pared de un pasillo amarillo que no se termina. Dice en qué nivel estás, cuánto
cuesta la salida al siguiente, y cuánto falta para la próxima ronda de los **Executores**.

Al fondo del pasillo hay un vano oscuro. Cada tanto algo lo cruza.

El fondo va cambiando de nivel solo. Entre uno y otro se corta la luz.

No añade objetos, ni entidades, ni mecánicas. Sólo cambia lo que ves antes de entrar a trabajar.

![Vista previa del menú](docs/vista_previa.png)

| | |
|---|---|
| Versión | **0.2.0** |
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente (el servidor no necesita el mod) |

## Qué trae la 0.2.0

- **Corredor rehecho.** La perspectiva se calcula desde una sola variable: la abertura del fondo y las
  cuatro aristas que salen del punto de fuga. Las juntas de pared, las del suelo y las del cielorraso usan
  la misma serie de profundidades, así que coinciden tramo a tramo y el pasillo se lee como un pasillo.
- **Cuatro niveles rotando.** Sección administrativa, Depósito, Pasillos de servicio y Las piscinas. Cada
  uno con sus colores, sus proporciones, su reflejo en el suelo y sus cosas colgadas.
- **Transición con apagón.** El nivel no se funde: la luz se corta, hay un momento en que no hay nada, y
  cuando el tubo arranca a los tirones el pasillo ya es otro. Nadie lo anuncia.
- **Sonido.** Zumbido de fluorescente en bucle que sigue a la luz, roce de papel al recorrer los renglones,
  sello al marcar la casilla, interruptor de pared al cambiar de pantalla, y los dos golpes del apagón.
  Todo sintetizado para el mod: sin muestras de terceros.
- Rótulo del nivel actual abajo a la izquierda, con su nota al pie.
- Pantalla de título propia: la hoja del aviso, con nivel actual, tarifa de salida y cuatro renglones de
  formulario con casilla marcable.
- Cuenta regresiva a la próxima ronda (ciclo de 13 minutos). Al llegar a cero, la luz cae.
- Avisos rotativos de la administración.
- Configuración de cliente con interruptores de accesibilidad, rotación de niveles y volumen del ambiente.

## Compilar

Requiere JDK 17 instalado.

```powershell
.\gradlew build
```

El `.jar` queda en `build\libs\jobsmenu-0.2.0.jar` y se copia a la carpeta `mods` de la instancia.

> Si `gradle\wrapper\gradle-wrapper.jar` no existe todavía, el bloque de despliegue lo descarga solo.

## Herramientas sin JDK

```powershell
python tools\verificar.py       # versiones, idiomas, JSON, ASCII, llaves, símbolos, audio y niveles
python tools\vista_previa.py    # dibuja el menú a PNG para revisar la escena
python tools\vista_previa.py --contacto docs\vista_previa.png   # los cuatro niveles juntos
python tools\sonidos.py         # regenera los .ogg (requiere numpy y soundfile)
```

## Documentación

Todo el diseño —canon del servidor, identidad, paleta, voz, alcance por fases y reglas de trabajo— está en
[`CONTEXTO.md`](CONTEXTO.md).
