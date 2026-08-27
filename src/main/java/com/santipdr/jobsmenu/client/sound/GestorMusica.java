package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.client.scene.Presencia;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * El tema del menu.
 *
 * SOBRE LA PISTA
 *
 * El evento musica.tema apunta al archivo musica/defecto.ogg, una pieza
 * ambiental compuesta y sintetizada para este mod (ver tools/sonidos.py):
 * ocho acordes largos sobre un pedal de la, sin ritmo ni melodia, pensada para
 * escucharse en bucle sin cansar. Es original, asi que el mod se puede repartir
 * sin arrastrar derechos de nadie.
 *
 * Si en algun momento hay una pista distinta con permiso de uso, no hace falta
 * tocar codigo: se agrega el archivo como musica/tema.ogg y se lo declara en
 * sounds.json junto al que ya esta. Todo lo de esta clase - el volumen, el
 * bucle, la continuidad durante el apagon - funciona igual con cualquier pista.
 *
 * COMO SE COMPORTA
 *
 * Una sola instancia, viva mientras el menu este abierto. No se reinicia al
 * cambiar de nivel ni al reconstruirse la pantalla, y sigue sonando durante el
 * apagon: es lo unico que no se apaga cuando se corta la luz, porque no es un
 * sonido del pasillo sino de la escena. Eso ademas le da continuidad al cambio
 * de nivel, que sin ella se sentiria como un corte.
 */
public class GestorMusica extends AbstractTickableSoundInstance {

    /** Instancia unica. Si ya hay una sonando, no se crea otra. */
    private static GestorMusica activa;

    /** Subida lenta: la musica tiene que entrar sin que se note que entro. */
    private static final float SUAVIZADO_SUBIDA = 0.012F;

    /** Bajada al cerrar el menu. Tampoco de golpe. */
    private static final float SUAVIZADO_BAJADA = 0.045F;

    private float actual;
    private int edad;

    private GestorMusica() {
        super(SonidosNivel.MUSICA_TEMA.get(), SoundSource.MUSIC, RandomSource.create());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.pitch = 1.0F;
        this.relative = true;
        this.attenuation = Attenuation.NONE;
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
        this.actual = 0.0F;
        this.edad = 0;
    }

    /**
     * Pone el tema a sonar si no lo esta ya.
     *
     * El control de instancia unica es lo que evita el problema clasico de los
     * menus con musica: la pantalla se reconstruye cada vez que se cambia el
     * tamano de la ventana, y sin este control quedarian dos o tres copias de
     * la misma pista sonando desfasadas.
     */
    public static void asegurar() {
        if (!ConfigTurno.musicaMenu()) {
            return;
        }
        if (activa != null && !activa.isStopped()) {
            return;
        }
        activa = new GestorMusica();
        Minecraft.getInstance().getSoundManager().play(activa);
        // Un rastro en el log: si la musica no se oye, esto dice si al menos se
        // mando a reproducir. Un SoundManager que descarta el sonido lo hace en
        // silencio, y sin este aviso no hay forma de saber si el problema es el
        // archivo, la mezcla o que nunca se llamo aca.
        com.santipdr.jobsmenu.JobsMenu.LOG.info(
                "[jobsmenu] Musica del menu enviada a reproducir (musica/defecto.ogg).");
    }

    /** Deja de sonar, con caida. No corta en seco. */
    public static void soltar() {
        activa = null;
    }

    /** Si el tema esta sonando ahora mismo. */
    public static boolean sonando() {
        return activa != null && !activa.isStopped();
    }

    /**
     * Cuanto se ve el credito de la pista ahora mismo, de 0 a 1.
     *
     * El credito -titulo y autor de la pista- se muestra UNA sola vez, al
     * empezar a sonar el tema, arriba a la derecha. La cuenta sale de la edad
     * de la instancia de musica, que nace con el menu y no se reinicia al
     * cambiar de pantalla: por eso el credito no vuelve a aparecer cada vez que
     * el jugador entra y sale de las opciones, solo la primera vez de la sesion.
     *
     * La envolvente es trapezoidal y en ticks (20 por segundo): entra a los 2 s,
     * se sostiene hasta los 15 s y se va del todo a los 18. Da tiempo de sobra a
     * leer dos lineas cortas sin quedarse en pantalla molestando despues.
     */
    public static float creditoAlfa() {
        GestorMusica m = activa;
        if (m == null || m.isStopped()
                || !ConfigTurno.musicaMenu() || !ConfigTurno.creditoMusica()
                || !hayPistaCreditada()) {
            return 0.0F;
        }
        int edad = m.edad;
        final int entra0 = 40;
        final int entra1 = 90;
        final int sale0 = 300;
        final int sale1 = 360;
        if (edad <= entra0 || edad >= sale1) {
            return 0.0F;
        }
        if (edad < entra1) {
            return (edad - entra0) / (float) (entra1 - entra0);
        }
        if (edad <= sale0) {
            return 1.0F;
        }
        return 1.0F - (edad - sale0) / (float) (sale1 - sale0);
    }

    /**
     * Si la pista que suena tiene un autor al que acreditar.
     *
     * Se cumple en dos casos, y solo en esos dos:
     *
     *   - el JAR trae una pista horneada con credito. El build deja un recurso
     *     marca (assets/jobsmenu/musica_creditada.txt) cuando reemplaza el tema por
     *     REQUIEM; si no se horneo nada, el marcador no existe;
     *   - el jugador dejo su propia pista en la carpeta de runtime.
     *
     * La pieza sintetizada que viene de fabrica NO se acredita a nadie: es del
     * mod. Por eso, sin marca y sin pista propia, el credito no aparece y nunca
     * se le atribuye a un autor una musica que no compuso.
     */
    private static boolean hayPistaCreditada() {
        if (MusicaPropia.tieneMusicaPropia()) {
            return true;
        }
        return marcadorHorneado();
    }

    /** Estado del marcador de pista horneada, -1 sin calcular, 0 no, 1 si. */
    private static int marcador = -1;

    /**
     * Si el JAR trae la marca de pista con credito. Se consulta el gestor de
     * recursos una sola vez por sesion y se cachea: el contenido del JAR no
     * cambia en caliente, y mirar el disco cada fotograma no tiene sentido.
     */
    private static boolean marcadorHorneado() {
        if (marcador < 0) {
            boolean hay = Minecraft.getInstance().getResourceManager()
                    .getResource(new ResourceLocation("jobsmenu", "musica_creditada.txt")).isPresent();
            marcador = hay ? 1 : 0;
        }
        return marcador == 1;
    }

    /**
     * Autoriza a nacer en silencio.
     *
     * El tema entra con veinte segundos de subida desde cero, y el motor de
     * sonido descarta al instante toda instancia que arranque muda. Sin esto
     * la musica quedaba registrada, empaquetada dentro del jar y correctamente
     * mezclada, pero no se escuchaba una sola nota: el motor la tiraba antes
     * de que la subida empezara.
     */
    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        this.edad++;

        Minecraft cliente = Minecraft.getInstance();
        boolean enMenu = cliente.screen instanceof com.santipdr.jobsmenu.client.screen.PantallaNivel;
        boolean permitido = enMenu && ConfigTurno.musicaMenu();

        // POR QUE LA MUSICA NO SE OIA
        //
        // Minecraft trae su propio gestor de musica de menu, que suena en el
        // MISMO canal (SoundSource.MUSIC) que nuestro tema. Cuando el juego
        // decide poner su musica de menu, la nuestra queda tapada o desalojada,
        // y el ambiente -que va por otro canal (AMBIENT)- se seguia oyendo: de
        // ahi el sintoma de "el ambiente suena pero la musica no".
        //
        // La solucion es callar al gestor de vanilla mientras nuestro menu esta
        // abierto, para que el canal de musica quede libre para el tema del
        // aviso. No toca los deslizadores del jugador: solo evita que dos
        // musicas peleen por el mismo canal.
        //
        // AVISO honesto: si ademas el deslizador "Musica" del juego esta en
        // cero, no hay codigo que valga -ese control lo manda el jugador-. Por
        // eso el ajuste de volumen del aviso avisa que hay que subirlo tambien.
        if (permitido) {
            cliente.getMusicManager().stopPlaying();
        }

        float objetivo = 0.0F;
        if (permitido) {
            objetivo = ConfigTurno.volumenMusica() * MezclaAudio.MUSICA;

            // Entrada suave pero no eterna: unos seis segundos hasta el volumen
            // pleno (antes eran veinte, y con la curva al cuadrado el tema no se
            // oia hasta pasado medio minuto; quien entraba un momento al menu se
            // iba sin escuchar nada). Sigue siendo un fundido, no un golpe.
            float entrada = Math.min(1.0F, this.edad / 120.0F);
            objetivo *= entrada * entrada;

            // Durante el apagon la musica se sostiene, pero cede un poco de
            // lugar para que el corte electrico tenga el frente para el solo.
            if (RotacionNiveles.enTransicion()) {
                objetivo *= 0.78F;
            }

            // Con la presencia al fondo, tambien se retira.
            objetivo *= 1.0F - (1.0F - MezclaAudio.AGACHE_FIGURA) * 0.5F * Presencia.visibilidad();
        }

        float paso = objetivo > this.actual ? SUAVIZADO_SUBIDA : SUAVIZADO_BAJADA;
        this.actual += (objetivo - this.actual) * paso;
        if (this.actual < 0.0006F) {
            this.actual = 0.0F;
        }
        this.volume = this.actual;

        if (!enMenu && this.actual <= 0.0F) {
            this.stop();
            if (activa == this) {
                activa = null;
            }
        }
    }
}
