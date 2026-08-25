# La música del menú

## Qué pediste y qué se puede hacer

Pediste integrar como recurso real la pista de
`https://www.youtube.com/watch?v=e1fesL31v4g`.

Fui a buscarla para integrarla y esto es lo que es:

- **Título:** REQUIEM — Forsaken OST
- **Autora:** Emmy Z (canal `@EmmyNoiz`)
- **Publicada:** 17 de julio de 2026
- **Duración:** 3:10
- **Qué es:** el tema de lobby de *Forsaken*, un juego comercial de Roblox.
  Reemplazó al tema anterior, *Damnation*.

No es música libre. Es la banda sonora de un juego comercial, compuesta por una
autora identificable que trabaja con ese equipo. Para meterla dentro del JAR
harían falta dos permisos por escrito: el de la **composición** y el de la
**grabación**. No los tenemos.

**Por eso no está dentro del JAR, y no voy a decirte que lo está.** Descargar el
audio de YouTube ya viola los términos de servicio de la plataforma antes
siquiera de llegar a la cuestión de los derechos de autor; redistribuirlo dentro
de un mod es otra infracción encima de esa. Si el mod circula por tu server con
esa pista adentro, el que responde sos vos.

Esto es exactamente lo que quedamos: si legalmente no se puede, se dice, no se
finge.

## Qué hay ahora

El menú **tiene música y suena**. Es una pieza original compuesta para el mod y
sintetizada desde cero (`tools/sonidos.py`, función `tema`): ocho acordes largos
sobre un pedal de la, sin ritmo ni melodía, 67 segundos en bucle sin junta.
Es nuestra, así que el mod se reparte sin arrastrar derechos de nadie.

Toda la maquinaria que pediste está construida y funcionando sobre esa pieza:
instancia única, sin duplicados ni reinicios al cambiar de nivel o
redimensionar la ventana, bucle natural, entrada de veinte segundos, volumen
propio en la config, y continuidad durante el apagón cediendo un 22 % para que
el corte eléctrico tenga el frente.

## Cómo poner la pista que quieras: una carpeta, un archivo, nada más

Esto volvió a cambiar en 0.6.0, y ahora sí está terminado.

En 0.5.0 el mod te dejaba el paquete de recursos armado, pero todavía te pedía
tres cosas: renombrar el archivo a un nombre exacto, meterlo en la carpeta
correcta y activar el paquete en Opciones. Tres oportunidades de equivocarte en
silencio —si el nombre no era exacto, no sonaba y no había mensaje—. Eso ya no
existe.

**Ahora es esto:**

```
.minecraft/
└── jobsmenu-musica/        ← el mod crea esta carpeta solo
    ├── LEEME.txt           ← las mismas instrucciones, ahí mismo
    └── tu-pista.ogg        ← soltás el archivo acá. Fin.
```

Una carpeta, en la raíz de la instancia, junto a `mods` y `saves`. Soltás
dentro tu archivo `.ogg` y listo. **No hay que renombrarlo** —el nombre da
igual—, no hay que crear ninguna carpeta, y **no hay que activar nada en
Opciones**.

Lo que hace el mod al arrancar:

1. Crea la carpeta si no existe, con el `LEEME.txt` dentro.
2. Busca un archivo de audio (si hay varios, el primero por orden alfabético).
3. Lo copia al paquete de recursos interno con el nombre que hace falta.
4. Registra el paquete y **lo activa solo**, sin pasar por Opciones.
5. Recarga los recursos, así suena en el acto y no al siguiente arranque.

Si no hay archivo, suena la pista propia del mod y no pasa nada. Para cambiar
de pista, reemplazás el archivo. Para volver a la del mod, lo sacás.

**El único requisito es el formato: OGG Vorbis.** Es el único que Minecraft
sabe decodificar; un MP3 renombrado a `.ogg` no suena. Y acá va otra cosa que
antes faltaba: si dejás un archivo que no es OGG, el mod **te lo dice en el
log** en vez de quedarse mudo. La copia se rehace solo si el archivo cambió, así
que no se paga tiempo de arranque de más.

**Por qué esto es legal y meterla en el JAR no.** Un mod que lee un archivo que
vos pusiste en tu carpeta no distribuye nada: la obra nunca sale de tu máquina.
Es el mismo principio por el que un emulador es legal aunque no venga con
juegos. Lo que no se puede es que el `.ogg` viaje dentro del JAR.

## Cómo conseguir esa pista legalmente

Por orden de facilidad:

1. **Pedísela a la autora. Esta es la vía real, y la investigación de esta
   ronda dice que está más abierta de lo que parecía.** Los hechos:

   - Emmy Z **ya dio permiso explícito** para que sus temas se subieran a la
     wiki de Forsaken. Está dicho por la propia wiki.
   - El equipo de Forsaken ha recibido permisos condicionados para reponer
     temas retirados —por ejemplo, con la condición de que fueran más baratos
     o gratuitos—. O sea: negocia, y pone condiciones en vez de decir que no.
   - También ha **retirado** permisos cuando no le gustó el uso. Eso confirma
     lo mismo desde el otro lado: es alguien que gestiona activamente su obra
     y a quien tiene sentido preguntarle.

   Es una música independiente con canal propio (`@EmmyNoiz`), no un sello con
   departamento legal. Escribile, contale que es para el menú de un server
   privado de Minecraft sin fines de lucro, y pedile permiso por escrito. Si
   dice que sí, guardá el mensaje: **eso es tu licencia**, y con eso el `.ogg`
   pasa al JAR y la clase `MusicaPropia` se borra entera.

   Un aviso que sale de la misma investigación: hay terceros reclamando
   derechos sobre temas de Forsaken en YouTube, y la cuenta oficial del equipo
   en las plataformas de streaming estuvo secuestrada. O sea que hay ruido
   alrededor de esta banda sonora. Razón de más para tener el permiso por
   escrito de la autora y no fiarse de ninguna otra fuente.
2. **Compralo** si lo publica en Bandcamp o similar. Ojo: comprar suele dar
   derecho de uso personal, no de redistribución. Para un server privado con el
   paquete de recursos en tu propia instancia, alcanza; para repartir el JAR, no.
3. **Buscá un equivalente libre.** Si lo que te gusta es el clima y no esa pista
   puntual, hay música ambiental con licencia Creative Commons que sirve igual.
   Si me decís qué es lo que te gusta de REQUIEM —el pedal grave, los coros
   lejanos, el aire— lo puedo componer directamente para el mod y el problema
   desaparece.

Mi recomendación es la 1 con la 3 como respaldo: es tu server, la autora es
accesible, y mientras tanto el menú ya tiene música propia que funciona.
