# Riesgos y comprobaciones pendientes

No hay fallos conocidos que impidan compilar el mod. Quedan estas verificaciones
de runtime, imposibles de sustituir con una revisión estática:

- Confirmar visualmente los diez recintos en las combinaciones extremas de
  resolución y GUI Scale indicadas en `docs/PRUEBAS_MANUALES.md`.
- Escuchar la transición real entre las 74 piezas en distintos dispositivos y
  mezclas Master; la ausencia de clipping estructural no determina percepción.
- Dejar terminar la pista completa y observar varias vueltas para comprobar el
  silencio de 40 ticks con el backend OpenAL de la máquina objetivo.
- Probar OGG truncados después de una cabecera inicialmente válida. La validación
  temprana rechaza archivos renombrados y cabeceras inválidas, pero la
  decodificación completa pertenece al SoundEngine.
- Comprobar convivencia con cada mod que también sustituya la clase vanilla
  exacta de una pantalla. Jobs Menu no impone prioridad global.
- Confirmar rendimiento en GPU integrada y con Embeddium/Oculus; el renderer no
  usa shaders propios, pero la combinación real depende del pack y del driver.

La hoja `docs/vista_previa.png` es una referencia de silueta y composición, no
una captura del renderer de Minecraft.

