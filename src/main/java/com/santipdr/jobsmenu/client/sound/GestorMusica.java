package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.client.scene.Presencia;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
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
    }

    /** Deja de sonar, con caida. No corta en seco. */
    public static void soltar() {
        activa = null;
    }

    /** Si el tema esta sonando ahora mismo. */
    public static boolean sonando() {
        return activa != null && !activa.isStopped();
    }

    @Override
    public void tick() {
        this.edad++;

        Minecraft cliente = Minecraft.getInstance();
        boolean enMenu = cliente.screen instanceof com.santipdr.jobsmenu.client.screen.PantallaNivel;
        boolean permitido = enMenu && ConfigTurno.musicaMenu();

        float objetivo = 0.0F;
        if (permitido) {
            objetivo = ConfigTurno.volumenMusica() * MezclaAudio.MUSICA;

            // Entrada larga la primera vez: veinte segundos hasta el volumen
            // pleno. Que la musica ya este ahi cuando el jugador se da cuenta.
            float entrada = Math.min(1.0F, this.edad / 400.0F);
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
