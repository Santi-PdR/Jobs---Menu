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

**Por qué el archivo no está en este repositorio.** El entorno donde se edita
el mod no tiene acceso a YouTube (está bloqueado), así que la grabación no se
pudo descargar desde acá para incluirla. Y aunque se pudiera: la obra de un
tercero no entra al control de versiones de un repo público. Por eso
`.gitignore` excluye `music/*.ogg`.

**Pero la vía para que la pista viaje DENTRO del JAR ya está construida.** Si es
para tu server entre amigos y vas a poner el crédito en pantalla —el mod lo
muestra solo, arriba a la derecha, al empezar a sonar—, dejás el archivo en la
carpeta `music/` del repo y al compilar queda horneado en el `.jar`. Los pasos,
más abajo. La responsabilidad de usar esa grabación es de quien compila el mod.

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

## Hornear REQUIEM (u otra pista) DENTRO del JAR

Esta es la vía para que la música **viaje con el mod**: quien instale el `.jar`
la oye sin poner nada. Pensada para un server entre amigos con el crédito en
pantalla.

1. Conseguí el archivo en **OGG Vorbis** (`.ogg`). Es el único formato que
   Minecraft decodifica. Si tenés un MP3, convertilo antes.
2. Copialo al repo como **`music/requiem.ogg`** (ese nombre exacto).
3. Compilá con `.\gradlew build`. El build detecta el archivo, lo hornea en el
   JAR reemplazando al tema sintetizado, y deja una marca interna
   (`assets/jobsmenu/musica_creditada`) que le dice al mod que esa pista tiene
   autor. En consola vas a ver:
   `[jobsmenu] REQUIEM horneada en el JAR desde music/requiem.ogg (con credito).`

Con eso, al abrir el menú suena REQUIEM y aparece el crédito arriba a la
derecha —**REQUIEM · Emmy Z · Forsaken OST**— una vez por sesión, entrando y
saliendo suave. El texto del crédito se edita en `lang/*.json`
(`jobsmenu.credito.titulo` y `jobsmenu.credito.autor`); si querés apagarlo,
`credito_musica = false` en la config.

El archivo `music/requiem.ogg` **no se sube al repositorio** (`.gitignore` lo
excluye): la obra de un tercero no se versiona. Si no ponés el archivo, el JAR
usa el tema sintetizado y el crédito no aparece —no se le atribuye a nadie una
pieza que compuso el propio mod—.

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
