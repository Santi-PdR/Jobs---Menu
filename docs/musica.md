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

## Cómo poner la pista que quieras, sin tocar código

El sistema está preparado para recibir otro archivo. **No hace falta compilar
nada ni modificar el mod**: se hace con un paquete de recursos, que es el
mecanismo que Minecraft tiene justamente para esto.

1. Conseguí el archivo de forma legítima (ver más abajo).
2. Convertilo a **OGG Vorbis mono o estéreo, 44 100 Hz**.
3. Nombralo `defecto.ogg`.
4. Armá esta estructura dentro de la carpeta `resourcepacks` de tu instancia:

```
resourcepacks/
└── musica-jobs/
    ├── pack.mcmeta
    └── assets/
        └── jobsmenu/
            └── sounds/
                └── musica/
                    └── defecto.ogg
```

Con este `pack.mcmeta`:

```json
{
  "pack": {
    "pack_format": 15,
    "description": "Musica del menu Jobs"
  }
}
```

5. Activalo en Opciones → Paquetes de recursos.

El menú va a usar tu archivo en lugar del sintetizado, con el mismo volumen, el
mismo bucle y el mismo comportamiento en la transición. Y como el paquete vive
en tu instancia y no dentro del JAR, **el mod se sigue pudiendo repartir sin
problemas legales**: el archivo con derechos nunca sale de tu máquina.

El bloque de PowerShell de esta entrega te deja la carpeta `musica-jobs` ya
creada con su `pack.mcmeta` puesto. Sólo falta que dejes el `.ogg` adentro.

## Cómo conseguir esa pista legalmente

Por orden de facilidad:

1. **Pedísela a la autora.** Es la vía real y es más viable de lo que parece:
   Emmy Z es una música independiente, tiene canal propio (`@EmmyNoiz`) y ya
   dio permiso explícito para que sus temas se subieran a la wiki de Forsaken,
   así que no es alguien cerrado a que le usen la obra si se le pregunta.
   Escribile por el canal, contale que es para el menú de un server privado de
   Minecraft sin fines de lucro y pedile permiso por escrito. Si te dice que sí,
   guardá ese mensaje: eso es tu licencia.
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
