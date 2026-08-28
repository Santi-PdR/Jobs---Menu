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
autora identificable que trabaja con ese equipo.

**Estado actual:** el owner subió la pista al repo y está **incluida y sonando**.
Desde 0.6.4 **no se hornea nada al compilar**: el archivo
`src/main/resources/assets/jobsmenu/sounds/musica/defecto.ogg` **ES REQUIEM
directamente** en los recursos. Lo que ves en el repo es lo que entra al `.jar`,
sin magia de Gradle. El original tal como lo subió el owner queda archivado como
respaldo en `music/REQUIEM-Forsaken-OST.ogg`. La responsabilidad de usar esa
grabación es de quien compila y reparte el mod; para distribuirlo públicamente
haría falta permiso escrito de la autora.

> Nota de proceso: el paso de horneado en `build.gradle` se quitó porque era
> frágil (la caché de `processResources` podía saltárselo) y de hecho la pista no
> llegaba a sonar. Ahora el `.ogg` que suena es, sin intermediarios, el que está
> en el árbol de recursos.

## Qué hay ahora

El menú **tiene música y suena**: es REQUIEM, la pista real, con su crédito en
pantalla. Toda la maquinaria está construida y funcionando: instancia única, sin
duplicados ni reinicios al cambiar de nivel o redimensionar la ventana, bucle
natural, entrada suave, volumen propio en la config, continuidad durante el
apagón (cede un 22 % para que el corte eléctrico tenga el frente), y se calla al
gestor de música de vanilla mientras el aviso está abierto para no pelear por el
canal `MUSIC`.

## Cómo cambiar la pista (reemplazando el recurso)

Esta es la vía para que la música **viaje con el mod**: quien instale el `.jar`
la oye sin poner nada.

1. Conseguí el archivo en **OGG Vorbis** (`.ogg`). Es el único formato que
   Minecraft decodifica. Si tenés un MP3, convertilo antes.
2. **Tiene que ser MONO.** Una pista estéreo puede quedar **muda** aunque el
   archivo sea válido y los volúmenes estén al máximo: el motor de Minecraft trata
   distinto lo mono y lo estéreo, y fue exactamente lo que impidió oír REQUIEM
   durante varias versiones. Convertí a un canal antes de meterla (con `ffmpeg`:
   `ffmpeg -i entrada.ogg -ac 1 -c:a libvorbis defecto.ogg`).
3. Reemplazá `src/main/resources/assets/jobsmenu/sounds/musica/defecto.ogg` por tu
   archivo (con ese nombre exacto). En `sounds.json`, el evento `musica.tema` debe
   quedar con `"stream": false` —igual que las camas de ambiente que sí suenan—.
4. Compilá con `.\gradlew build`.

**El crédito** aparece arriba a la derecha —**REQUIEM · Emmy Z · Forsaken OST**—
una vez por sesión, entrando y saliendo suave, y sólo si existe el recurso marca
`assets/jobsmenu/musica_creditada.txt`. El texto se edita en `lang/*.json`
(`jobsmenu.credito.titulo` y `jobsmenu.credito.autor`); si querés apagarlo,
`credito_musica = false` en la config. Si tu pista es de dominio propio y no debe
llevar crédito, borrá ese archivo marca y no se atribuye a nadie.

## Cómo poner la pista que quieras SIN recompilar: una carpeta, un archivo

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

**Dos requisitos: formato OGG Vorbis y MONO.** El OGG es el único formato que
Minecraft sabe decodificar; un MP3 renombrado a `.ogg` no suena. Y la pista tiene
que ser **mono (un canal)**: una estéreo puede quedar muda aunque el archivo sea
válido, porque el motor de sonido trata distinto lo mono y lo estéreo. Si tu
archivo es estéreo, convertilo antes con
`ffmpeg -i entrada.ogg -ac 1 -c:a libvorbis salida.ogg`. Si dejás un archivo que
no es OGG, el mod **te lo dice en el log** en vez de quedarse mudo. La copia se
rehace solo si el archivo cambió, así que no se paga tiempo de arranque de más.

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

