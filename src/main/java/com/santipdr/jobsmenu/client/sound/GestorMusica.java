package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.client.scene.Presencia;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Tema musical del menu, con mezcla propia y pausa breve entre vueltas. */
public class GestorMusica extends AbstractTickableSoundInstance {

    private static GestorMusica activa;

    private static final float SUAVIZADO_SUBIDA = 0.012F;
    private static final float SUAVIZADO_BAJADA = 0.045F;

    /** 40 ticks = aproximadamente 2 segundos entre el final y la siguiente vuelta. */
    private static final int PAUSA_ENTRE_VUELTAS_TICKS = 40;

    private float actual;
    private int edad;

    private GestorMusica() {
        // MASTER evita que el tema dependa del deslizador vanilla "Musica".
        // Sigue respetando el volumen maestro del juego y, encima de eso,
        // nuestro volumen propio de ConfigTurno.
        super(SonidosNivel.MUSICA_TEMA.get(), SoundSource.MASTER, RandomSource.create());
        this.looping = true;
        this.delay = PAUSA_ENTRE_VUELTAS_TICKS;
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

    public static void asegurar() {
        if (!ConfigTurno.musicaMenu()) {
            return;
        }
        if (activa != null && !activa.isStopped()) {
            return;
        }
        activa = new GestorMusica();
        Minecraft.getInstance().getSoundManager().play(activa);
        com.santipdr.jobsmenu.JobsMenu.LOG.info(
                "[jobsmenu] Musica del menu enviada a reproducir: mezcla propia, pausa de 2 s entre vueltas.");
    }

    public static void soltar() {
        activa = null;
    }

    public static boolean sonando() {
        return activa != null && !activa.isStopped();
    }

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

    private static boolean hayPistaCreditada() {
        if (MusicaPropia.tieneMusicaPropia()) {
            return true;
        }
        return marcadorHorneado();
    }

    private static int marcador = -1;

    private static boolean marcadorHorneado() {
        if (marcador < 0) {
            boolean hay = Minecraft.getInstance().getResourceManager()
                    .getResource(new ResourceLocation("jobsmenu", "musica_creditada.txt")).isPresent();
            marcador = hay ? 1 : 0;
        }
        return marcador == 1;
    }

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

        // Aunque el tema ya no usa MUSIC, detenemos la musica vanilla para que
        // no se superponga con REQUIEM por otro canal y ensucie la mezcla.
        if (permitido) {
            cliente.getMusicManager().stopPlaying();
        }

        float objetivo = 0.0F;
        if (permitido) {
            objetivo = ConfigTurno.volumenMusica() * MezclaAudio.MUSICA;

            float entrada = Math.min(1.0F, this.edad / 120.0F);
            objetivo *= entrada * entrada;

            if (RotacionNiveles.enTransicion()) {
                objetivo *= 0.78F;
            }

            objetivo *= 1.0F - (1.0F - MezclaAudio.AGACHE_FIGURA)
                    * 0.5F * Presencia.visibilidad();
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
