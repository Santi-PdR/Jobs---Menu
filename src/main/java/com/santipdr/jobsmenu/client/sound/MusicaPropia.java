package com.santipdr.jobsmenu.client.sound;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.client.Minecraft;

/**
 * El hueco donde el que juega pone su propia musica.
 *
 * EL PROBLEMA, DICHO SIN ADORNOS
 *
 * La pista que se pidio para este menu es REQUIEM, el tema del lobby de
 * Forsaken, compuesto por Emmy Z (@EmmyNoiz). Es una obra con autoria
 * reconocida y sin licencia publica de redistribucion. Meter ese archivo
 * dentro del JAR seria repartir la obra de otra persona sin permiso, y eso no
 * se hace: ni por respeto a quien la compuso, ni por lo que le puede caer
 * encima al que reparte el mod.
 *
 * Lo que si es perfectamente legal es que cada persona use la copia que ya
 * tiene. Un mod que lee un archivo que el usuario puso en su propia carpeta no
 * distribuye nada. Es el mismo principio por el que un emulador es legal aunque
 * no venga con juegos.
 *
 * QUE HACE ESTA CLASE
 *
 * Prepara el hueco y lo deja listo para recibir el archivo. En el primer
 * arranque crea, dentro de la carpeta resourcepacks de la instancia, un
 * paquete completo y valido -carpetas, pack.mcmeta y unas instrucciones- al que
 * solo le falta el .ogg. El que juega arrastra su copia, activa el paquete y el
 * menu suena con su musica.
 *
 * No es "aca tienes un paquete vacio, arreglatelas". Es la diferencia entre
 * tener que averiguar la estructura exacta de un resource pack -que hay que
 * acertar al nombre de cada carpeta o no funciona en silencio- y dejar caer un
 * archivo en una carpeta que ya existe y ya esta bien armada.
 *
 * SOBRE PEDIR PERMISO
 *
 * Hay una via legal de verdad y esta abierta: Emmy Z ha concedido permisos de
 * uso de sus temas en otras ocasiones, y tambien los ha retirado cuando no le
 * gusto el uso que se les daba. O sea que es alguien a quien se le pregunta y
 * responde. Si algun dia da permiso por escrito para este mod, el archivo se
 * mete en el JAR y esta clase se borra entera. Hasta entonces, el hueco.
 * Los detalles estan en docs/musica.md.
 */
public final class MusicaPropia {

    /** Carpeta del paquete dentro de resourcepacks. */
    private static final String CARPETA = "jobsmenu-musica";

    /** Nombre exacto que tiene que tener el archivo. */
    public static final String ARCHIVO = "defecto.ogg";

    /** Formato de paquete de recursos de Minecraft 1.20.1. */
    private static final int FORMATO = 15;

    /** Se comprueba una vez por sesion: no hace falta tocar el disco mas. */
    private static boolean preparado;

    /** Cierto si la ultima comprobacion encontro el archivo en su sitio. */
    private static boolean conMusica;

    private MusicaPropia() {
    }

    /**
     * Crea el paquete si no existe y anota si ya tiene la pista dentro.
     *
     * Se llama una sola vez, al abrir el menu por primera vez. Cualquier fallo
     * de disco se traga a proposito: que no se pueda escribir en la carpeta no
     * es motivo para que el menu no abra.
     */
    public static void preparar() {
        if (preparado) {
            return;
        }
        preparado = true;

        try {
            Path raiz = Minecraft.getInstance().gameDirectory.toPath()
                    .resolve("resourcepacks").resolve(CARPETA);
            Path destino = raiz.resolve("assets").resolve("jobsmenu")
                    .resolve("sounds").resolve("musica");

            Files.createDirectories(destino);
            escribirSiFalta(raiz.resolve("pack.mcmeta"), metadatos());
            escribirSiFalta(raiz.resolve("LEEME.txt"), instrucciones());

            conMusica = Files.isRegularFile(destino.resolve(ARCHIVO));
        } catch (IOException | RuntimeException ignorada) {
            // Sin permisos de escritura no hay paquete, y no pasa nada:
            // el menu sigue funcionando con su pista propia.
            conMusica = false;
        }
    }

    /** Cierto si el que juega ya dejo su pista en el paquete. */
    public static boolean tieneMusicaPropia() {
        return conMusica;
    }

    private static void escribirSiFalta(Path ruta, String contenido) throws IOException {
        if (!Files.exists(ruta)) {
            Files.write(ruta, contenido.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String metadatos() {
        return "{\n"
                + "  \"pack\": {\n"
                + "    \"pack_format\": " + FORMATO + ",\n"
                + "    \"description\": \"Musica propia para Jobs\\u00b7Aviso a los ocupantes\"\n"
                + "  }\n"
                + "}\n";
    }

    private static String instrucciones() {
        return String.join("\n",
                "MUSICA PROPIA PARA EL MENU",
                "==========================",
                "",
                "Este paquete lo creo el mod solo. Esta entero y bien armado: lo",
                "unico que le falta es el archivo de musica, que tiene que poner",
                "usted porque el mod no puede repartir musica de otra persona.",
                "",
                "COMO SE USA",
                "",
                "  1. Consiga su copia de la pista que quiera oir en el menu.",
                "  2. Conviertala a formato OGG Vorbis si no lo esta ya.",
                "  3. Renombrela exactamente a:  " + ARCHIVO,
                "  4. Pongala en la carpeta:",
                "",
                "       assets/jobsmenu/sounds/musica/" + ARCHIVO,
                "",
                "     (esa carpeta ya existe aqui al lado, no hay que crearla)",
                "",
                "  5. En el juego: Opciones > Paquetes de recursos, y active el",
                "     paquete que aparece como \"Musica propia para Jobs\".",
                "",
                "Listo. El menu pasa a sonar con su pista en lugar de la que trae",
                "el mod. Para volver atras, desactive el paquete.",
                "",
                "SOBRE LOS DERECHOS",
                "",
                "Usar una copia suya en su propia instalacion es asunto suyo. Lo",
                "que no se puede hacer -ni lo hace el mod- es repartir el archivo",
                "dentro del JAR: eso seria distribuir la obra de otra persona sin",
                "su permiso.",
                "",
                "Si lo que quiere es que la pista venga incluida de fabrica, el",
                "camino es pedirle permiso a quien la compuso. Esta explicado en",
                "docs/musica.md, en el repositorio del mod.",
                "");
    }
}
