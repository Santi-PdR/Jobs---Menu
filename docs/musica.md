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

## Cómo poner la pista que quieras: el mod ya te dejó el hueco hecho

Esto cambió en 0.5.0. Antes esta sección te explicaba cómo armar un paquete de
recursos a mano, con su estructura exacta de carpetas. Era correcto y era una
molestia: si te equivocabas en el nombre de una carpeta, no sonaba nada y no
había ningún mensaje de error que te dijera por qué.

**Ahora lo arma el mod solo.** La primera vez que abrís el menú, el mod crea en
la carpeta `resourcepacks` de tu instancia un paquete completo y válido:

```
resourcepacks/
└── jobsmenu-musica/
    ├── pack.mcmeta          ← ya creado, ya válido
    ├── LEEME.txt            ← las instrucciones, ahí mismo
    └── assets/jobsmenu/sounds/musica/
        └── (acá va tu defecto.ogg)
```

Todo está hecho menos una cosa, que es justamente la que no puedo hacer yo:
poner el archivo. Vos:

1. Conseguís tu copia de la pista (ver abajo).
2. La convertís a **OGG Vorbis, 44 100 Hz**.
3. La renombrás a `defecto.ogg`.
4. La soltás en la carpeta `musica` que ya existe.
5. Activás el paquete en Opciones → Paquetes de recursos.

Y suena, con el mismo volumen, el mismo bucle y el mismo comportamiento durante
el apagón que la pista propia.

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
