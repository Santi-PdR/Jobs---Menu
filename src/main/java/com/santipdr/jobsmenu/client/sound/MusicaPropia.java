package com.santipdr.jobsmenu.client.sound;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import com.santipdr.jobsmenu.JobsMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

/**
 * La musica del menu que pone el que juega, sin que tenga que configurar nada.
 *
 * EL PROBLEMA, DICHO SIN ADORNOS
 *
 * La pista que se pidio para este menu es REQUIEM, el tema del lobby de
 * Forsaken, compuesto por Emmy Z (@EmmyNoiz). Es una obra con autoria
 * reconocida y sin licencia publica de redistribucion. Meterla dentro del JAR
 * seria repartir la obra de otra persona sin permiso, y eso no se hace: ni por
 * respeto a quien la compuso, ni por lo que le puede caer encima al que reparte
 * el mod. Lo que si es perfectamente legal es que cada persona use la copia que
 * ya tiene. Un mod que lee un archivo que el usuario dejo en su propia carpeta
 * no distribuye nada.
 *
 * QUE HACE ESTA CLASE
 *
 * Todo lo que antes tenia que hacer el usuario a mano. La version anterior
 * dejaba un resource pack armado y pedia tres cosas: renombrar el archivo a un
 * nombre exacto, meterlo en la carpeta correcta y activar el paquete desde el
 * menu de opciones. Eso son tres oportunidades de equivocarse en silencio -si
 * el nombre no es exacto, no suena y no hay ningun mensaje- y el pedido fue
 * explicito: nada de crear carpetas ni tocar archivos a mano.
 *
 * Ahora hay una sola carpeta, con un solo proposito, y basta con soltar dentro
 * un archivo de audio:
 *
 *     .minecraft/jobsmenu-musica/
 *
 * El nombre del archivo da igual. La extension puede ser .ogg o cualquier otra
 * que traiga Vorbis dentro; se toma el primero por orden alfabetico. El mod:
 *
 *   1. crea la carpeta en el primer arranque, con un LEEME al lado;
 *   2. busca dentro un archivo de audio;
 *   3. lo copia al resource pack interno con el nombre que hace falta;
 *   4. registra y ACTIVA el paquete solo, sin pasar por Opciones;
 *   5. recarga los recursos para que el cambio valga en el acto.
 *
 * Si no hay archivo, no pasa nada y suena la pista propia del mod. Si se cambia
 * el archivo, en el siguiente arranque suena el nuevo.
 *
 * POR QUE UN RESOURCE PACK Y NO LEER EL ARCHIVO DIRECTAMENTE
 *
 * Porque el motor de sonido de Minecraft solo reproduce lo que este declarado
 * como recurso: no hay una via soportada para pasarle un archivo suelto del
 * disco a SoundManager. El resource pack es el mecanismo previsto para esto. La
 * diferencia con la version anterior no es el mecanismo, es que ahora el
 * paquete lo arma, lo llena y lo enciende el mod.
 *
 * SOBRE PEDIR PERMISO
 *
 * Hay una via legal de verdad y esta abierta: Emmy Z ha concedido permisos de
 * uso de sus temas en otras ocasiones, y tambien los ha retirado cuando no le
 * gusto el uso que se les daba. O sea que es alguien a quien se le pregunta y
 * responde. Si algun dia da permiso por escrito para este mod, el archivo se
 * mete en el JAR y esta clase se borra entera. Hasta entonces, la carpeta.
 * Los detalles estan en docs/musica.md.
 */
public final class MusicaPropia {

    /** Carpeta donde el usuario deja su pista, en la raiz de la instancia. */
    public static final String CARPETA = "jobsmenu-musica";

    /** Carpeta del paquete generado, dentro de resourcepacks. */
    private static final String PAQUETE = "jobsmenu-musica-activa";

    /** Nombre interno del recurso. El usuario ya no tiene que saberlo. */
    private static final String ARCHIVO = "defecto.ogg";

    /** Formato de paquete de recursos de Minecraft 1.20.1. */
    private static final int FORMATO = 15;

    /**
     * Extensiones que se aceptan.
     *
     * Minecraft solo decodifica Vorbis, asi que un .mp3 renombrado no va a
     * sonar. Se aceptan igual los nombres mas probables para poder avisar en el
     * registro en vez de quedarse mudo, que es lo que hacia antes.
     */
    private static final String[] EXTENSIONES = {".ogg", ".oga", ".mp3", ".wav", ".flac", ".m4a"};

    /** Se comprueba una vez por sesion: no hace falta tocar el disco mas. */
    private static boolean preparado;

    /** Cierto si hay una pista del usuario instalada y activa. */
    private static boolean conMusica;

    /** Nombre del archivo que se encontro, para poder decirlo en el registro. */
    private static String pista = "";

    private MusicaPropia() {
    }

    /**
     * Deja todo listo: carpeta, deteccion, copia y activacion.
     *
     * Se llama una sola vez, al abrir el menu. Cualquier fallo de disco se
     * traga a proposito: que no se pueda escribir en la carpeta no es motivo
     * para que el menu no abra.
     */
    public static void preparar() {
        if (preparado) {
            return;
        }
        preparado = true;

        try {
            Path juego = Minecraft.getInstance().gameDirectory.toPath();
            Path buzon = juego.resolve(CARPETA);
            Files.createDirectories(buzon);
            escribirSiFalta(buzon.resolve("LEEME.txt"), instrucciones());

            Path fuente = buscarPista(buzon);
            if (fuente == null) {
                conMusica = false;
                desactivarPaqueteAnterior();
                return;
            }
            pista = fuente.getFileName().toString();

            if (!pista.toLowerCase(Locale.ROOT).endsWith(".ogg")
                    && !pista.toLowerCase(Locale.ROOT).endsWith(".oga")) {
                // No se convierte: el mod no trae codificador. Pero se dice, en
                // vez de dejar al usuario preguntandose por que no suena nada.
                JobsMenu.LOG.warn("[jobsmenu] La pista '" + pista + "' no es OGG Vorbis. "
                        + "Minecraft solo reproduce OGG: convertila y volve a probar.");
                conMusica = false;
                desactivarPaqueteAnterior();
                return;
            }
            if (!esOggVorbis(fuente)) {
                JobsMenu.LOG.warn("[jobsmenu] La pista '{}' no contiene una cabecera OGG Vorbis valida.", pista);
                conMusica = false;
                desactivarPaqueteAnterior();
                return;
            }

            Path raiz = juego.resolve("resourcepacks").resolve(PAQUETE);
            Path destino = raiz.resolve("assets").resolve("jobsmenu")
                    .resolve("sounds").resolve("musica").resolve(ARCHIVO);
            Files.createDirectories(destino.getParent());
            escribirSiFalta(raiz.resolve("pack.mcmeta"), metadatos());

            // Solo se copia si cambio: copiar en cada arranque un archivo de
            // varios megas es tiempo de carga regalado.
            if (!Files.exists(destino) || Files.mismatch(fuente, destino) != -1L) {
                Files.copy(fuente, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            conMusica = activarPaquete();
            if (conMusica) {
                JobsMenu.LOG.info("[jobsmenu] Musica del menu: '" + pista + "'.");
            }
        } catch (IOException | RuntimeException fallo) {
            conMusica = false;
            JobsMenu.LOG.warn("[jobsmenu] No se pudo preparar la musica personalizada; se usara la pista del mod.", fallo);
        }
    }

    /**
     * Enciende el paquete sin pasar por la pantalla de Opciones.
     *
     * Hay que recargar el repositorio antes de poder seleccionar el paquete:
     * si se creo la carpeta en este mismo arranque, el repositorio todavia no
     * sabe que existe y setSelected lo ignoraria en silencio.
     *
     * Devuelve cierto si el paquete quedo activo. No fuerza la recarga de
     * recursos cuando ya estaba puesto de antes, porque una recarga completa
     * congela el juego un par de segundos y no hay ningun motivo para pagarla
     * en cada arranque.
     */
    private static boolean activarPaquete() {
        Minecraft cliente = Minecraft.getInstance();
        PackRepository repositorio = cliente.getResourcePackRepository();
        repositorio.reload();

        String id = null;
        for (String disponible : repositorio.getAvailableIds()) {
            if (esPaquetePropio(disponible)) {
                id = disponible;
                break;
            }
        }
        if (id == null) {
            return false;
        }
        if (repositorio.getSelectedIds().contains(id)) {
            return true;
        }

        Set<String> seleccion = new LinkedHashSet<>(repositorio.getSelectedIds());
        seleccion.add(id);
        repositorio.setSelected(seleccion);
        cliente.options.updateResourcePacks(repositorio);
        cliente.options.save();

        // Recarga en caliente, solo la primera vez que se instala el paquete.
        // Sin esto el archivo quedaria copiado y el paquete marcado, pero la
        // pista no sonaria hasta el siguiente arranque, y el usuario no tiene
        // por que enterarse de que existe esa diferencia: dejo el archivo y
        // quiere oirlo. La recarga congela el juego un instante, y por eso se
        // paga una sola vez y no en cada arranque.
        cliente.reloadResourcePacks();
        return true;
    }

    /** Quita el pack generado si ya no hay pista, evitando reproducir una copia vieja. */
    private static void desactivarPaqueteAnterior() {
        Minecraft cliente = Minecraft.getInstance();
        PackRepository repositorio = cliente.getResourcePackRepository();
        repositorio.reload();
        Set<String> seleccion = new LinkedHashSet<>(repositorio.getSelectedIds());
        boolean cambio = seleccion.removeIf(MusicaPropia::esPaquetePropio);
        if (!cambio) {
            return;
        }
        repositorio.setSelected(seleccion);
        cliente.options.updateResourcePacks(repositorio);
        cliente.options.save();
        cliente.reloadResourcePacks();
        JobsMenu.LOG.info("[jobsmenu] Paquete de musica personalizado desactivado; vuelve la pista del mod.");
    }

    private static boolean esPaquetePropio(String id) {
        return id.equals(PAQUETE) || id.endsWith("/" + PAQUETE) || id.endsWith(":" + PAQUETE);
    }

    /** Comprobacion barata que rechaza archivos renombrados o truncados antes del SoundEngine. */
    private static boolean esOggVorbis(Path archivo) throws IOException {
        byte[] cabecera = new byte[96];
        int leidos;
        try (InputStream entrada = Files.newInputStream(archivo)) {
            leidos = entrada.read(cabecera);
        }
        if (leidos < 12 || cabecera[0] != 'O' || cabecera[1] != 'g'
                || cabecera[2] != 'g' || cabecera[3] != 'S') {
            return false;
        }
        for (int i = 4; i + 5 < leidos; i++) {
            if (cabecera[i] == 'v' && cabecera[i + 1] == 'o' && cabecera[i + 2] == 'r'
                    && cabecera[i + 3] == 'b' && cabecera[i + 4] == 'i' && cabecera[i + 5] == 's') {
                return true;
            }
        }
        return false;
    }

    /** El primer archivo de audio de la carpeta, por orden alfabetico. */
    private static Path buscarPista(Path buzon) throws IOException {
        Path elegida = null;
        try (DirectoryStream<Path> listado = Files.newDirectoryStream(buzon)) {
            for (Path candidata : listado) {
                if (!Files.isRegularFile(candidata)) {
                    continue;
                }
                String nombre = candidata.getFileName().toString().toLowerCase(Locale.ROOT);
                for (String extension : EXTENSIONES) {
                    if (nombre.endsWith(extension)) {
                        if (elegida == null
                                || candidata.getFileName().toString()
                                    .compareToIgnoreCase(elegida.getFileName().toString()) < 0) {
                            elegida = candidata;
                        }
                        break;
                    }
                }
            }
        }
        return elegida;
    }

    /** Cierto si el que juega ya dejo su pista y quedo activa. */
    public static boolean tieneMusicaPropia() {
        return conMusica;
    }

    /** Nombre del archivo que esta sonando, o cadena vacia. */
    public static String nombrePista() {
        return conMusica ? pista : "";
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
                "MUSICA DEL MENU",
                "===============",
                "",
                "Deje aqui dentro el archivo de musica que quiera oir en el menu.",
                "Eso es todo. No hay que renombrarlo, ni crear carpetas, ni activar",
                "nada en las opciones: el mod lo detecta al arrancar y lo pone.",
                "",
                "REQUISITOS",
                "",
                "  - Formato OGG Vorbis (.ogg). Es el unico que Minecraft sabe",
                "    reproducir. Si tiene un MP3, conviertalo antes; hay",
                "    conversores en linea y en cualquier editor de audio.",
                "  - Si deja varios archivos, se usa el primero por orden",
                "    alfabetico.",
                "",
                "PARA CAMBIAR DE PISTA",
                "",
                "  Reemplace el archivo y reinicie el juego.",
                "",
                "PARA VOLVER A LA PISTA DEL MOD",
                "",
                "  Saque el archivo de esta carpeta y reinicie el juego.",
                "",
                "EL CREDITO EN PANTALLA",
                "",
                "Al empezar a sonar, el menu muestra arriba a la derecha el credito",
                "de la pista. Por defecto dice REQUIEM, de Emmy Z. Si deja aqui otra",
                "cancion, ese credito ya no le corresponde: cambielo en los archivos",
                "de idioma del mod (claves jobsmenu.credito.titulo y .autor) o",
                "apaguelo con credito_musica = false en la configuracion.",
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
