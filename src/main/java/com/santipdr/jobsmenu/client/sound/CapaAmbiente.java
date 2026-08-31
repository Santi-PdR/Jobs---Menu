package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.client.scene.Presencia;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Una cama de sonido continuo de un nivel, en bucle mientras el nivel este a la vista.
 *
 * Hay una instancia viva por nivel, no una sola que cambia de archivo: asi los
 * ambientes pueden solaparse durante la transicion y el pasillo nuevo empieza
 * a escucharse mientras el viejo todavia se esta yendo.
 */
public class CapaAmbiente extends AbstractTickableSoundInstance {

    private static final float SUAVIZADO_SUBIDA = 0.035F;
    private static final float SUAVIZADO_BAJADA = 0.055F;

    public enum Papel {
        BASE(0.82F, 0.30F, 0.083F, 0.06F),
        CARACTER(0.66F, 0.72F, 0.061F, 0.09F),
        ACTIVIDAD(0.74F, 0.88F, 0.037F, 0.02F);

        private final float peso;
        private final float pisoSinLuz;
        private final float respiracion;
        private final float vaiven;

        Papel(float peso, float pisoSinLuz, float respiracion, float vaiven) {
            this.peso = peso;
            this.pisoSinLuz = pisoSinLuz;
            this.respiracion = respiracion;
            this.vaiven = vaiven;
        }
    }

    private final int nivel;
    private final Papel papel;
    private final float tonoBase;
    private float actual;
    private int edad;

    public CapaAmbiente(SoundEvent evento, int nivel, Papel papel) {
        super(evento, SoundSource.AMBIENT, RandomSource.create());
        this.nivel = nivel;
        this.papel = papel;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.relative = true;
        this.attenuation = Attenuation.NONE;
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
        this.actual = 0.0F;
        this.edad = 0;

        float tono = 0.975F + 0.004F * Math.min(nivel, 9);
        if (papel == Papel.CARACTER) {
            this.edad = 617;
            tono *= 0.995F;
        } else if (papel == Papel.ACTIVIDAD) {
            this.edad = 1_483;
            tono = 1.0F;
        }
        this.tonoBase = tono;
        this.pitch = tono;
    }

    public Papel papel() {
        return this.papel;
    }

    public int nivel() {
        return this.nivel;
    }

    private static float matizNivel(int nivel, Papel papel) {
        if (nivel < 10) {
            return 1.0F;
        }
        return switch (nivel) {
            case 10 -> papel == Papel.ACTIVIDAD ? 1.12F : (papel == Papel.BASE ? 0.88F : 0.82F);
            case 11 -> papel == Papel.ACTIVIDAD ? 0.82F : (papel == Papel.BASE ? 0.78F : 0.86F);
            case 12 -> papel == Papel.ACTIVIDAD ? 1.05F : (papel == Papel.BASE ? 0.86F : 0.95F);
            case 13 -> papel == Papel.ACTIVIDAD ? 0.92F : (papel == Papel.BASE ? 0.82F : 0.72F);
            case 14 -> papel == Papel.ACTIVIDAD ? 0.90F : (papel == Papel.BASE ? 0.88F : 0.90F);
            case 15 -> papel == Papel.ACTIVIDAD ? 1.18F : (papel == Papel.BASE ? 0.68F : 0.58F);
            case 16 -> papel == Papel.ACTIVIDAD ? 0.70F : (papel == Papel.BASE ? 0.58F : 0.52F);
            case 17 -> papel == Papel.ACTIVIDAD ? 0.95F : (papel == Papel.BASE ? 0.72F : 0.64F);
            default -> 1.0F;
        };
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public boolean agotada() {
        return this.isStopped();
    }

    public void detenerAhora() {
        this.stop();
    }

    @Override
    public void tick() {
        this.edad++;

        RotacionNiveles.Estado estado = RotacionNiveles.capturar();
        boolean enMenu = com.santipdr.jobsmenu.client.SesionMenu.activa();
        boolean mia = estado.indice() == this.nivel;
        boolean permitido = enMenu && mia && ConfigTurno.sonidoAmbiente();

        float objetivo = 0.0F;
        if (permitido) {
            objetivo = ConfigTurno.volumenAmbiente() * MezclaAudio.AMBIENTE
                    * this.papel.peso * ConfigTurno.volumenAviso();
            objetivo *= matizNivel(this.nivel, this.papel);

            float luz = estado.luz();
            float factorLuz;
            if (estado.enSuspension()) {
                factorLuz = this.papel == Papel.ACTIVIDAD ? this.papel.pisoSinLuz : 0.015F;
            } else {
                factorLuz = this.papel.pisoSinLuz + (1.0F - this.papel.pisoSinLuz) * luz;
            }
            objetivo *= factorLuz;

            float t = this.edad / 20.0F;
            objetivo *= 1.0F + this.papel.vaiven * (float) Math.sin(t * this.papel.respiracion)
                    + 0.03F * (float) Math.sin(t * 0.031F + 1.7F);

            if (this.papel != Papel.ACTIVIDAD) {
                objetivo *= 1.0F - (1.0F - MezclaAudio.AGACHE_FIGURA)
                        * Presencia.visibilidad(estado.ahora());
            }

            // Microderiva tonal de ciclo muy largo. No se usa en ACTIVIDAD,
            // donde hay objetos reconocibles; solo evita que BASE/CARACTER se
            // delaten como un archivo perfectamente identico cada vuelta.
            if (this.papel == Papel.ACTIVIDAD) {
                this.pitch = this.tonoBase;
            } else {
                float deriva = 0.0024F * (float) Math.sin(t * 0.014F + this.nivel * 0.73F)
                        + 0.0011F * (float) Math.sin(t * 0.031F + this.papel.ordinal() * 1.9F);
                this.pitch = this.tonoBase * (1.0F + deriva);
            }
        } else {
            this.pitch = this.tonoBase;
        }

        float paso = objetivo > this.actual ? SUAVIZADO_SUBIDA : SUAVIZADO_BAJADA;
        this.actual += (objetivo - this.actual) * paso;
        if (this.actual < 0.0008F) {
            this.actual = 0.0F;
        }
        this.volume = this.actual;

        if (this.actual <= 0.0F && (!enMenu || !mia)) {
            this.stop();
        }
    }
}
