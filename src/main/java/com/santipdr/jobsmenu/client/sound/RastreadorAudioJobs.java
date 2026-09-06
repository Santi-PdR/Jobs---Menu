package com.santipdr.jobsmenu.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Conserva las instancias puntuales que pertenecen al ambiente del menu Jobs.
 *
 * Las camas continuas y la musica tienen gestores propios, pero los eventos,
 * apagones y sonidos de transicion son SimpleSoundInstance. Antes se lanzaban
 * y se olvidaban: al entrar a gameplay no habia una referencia concreta que
 * permitiera ordenar su stop. Esta lista existe solo durante la ejecucion y se
 * vacia al cerrar la visita o reconstruir recursos.
 */
public final class RastreadorAudioJobs {

    private static final List<SoundInstance> PUNTUALES = new ArrayList<>();
    private static long registrados;
    private static long purgados;
    private static long cierres;

    private RastreadorAudioJobs() {
    }

    public static SoundInstance registrar(SoundInstance instancia) {
        if (instancia == null) return null;
        purgarFinalizados();
        PUNTUALES.add(instancia);
        registrados++;
        return instancia;
    }

    /**
     * Los eventos ya terminados no deben quedar retenidos hasta el cierre de la
     * visita. SoundManager conoce el estado real de cada instancia y permite
     * retirar referencias sin tocar ningun sonido que siga activo.
     */
    private static void purgarFinalizados() {
        if (PUNTUALES.isEmpty()) return;
        var sonidos = Minecraft.getInstance().getSoundManager();
        int antes = PUNTUALES.size();
        PUNTUALES.removeIf(instancia -> !sonidos.isActive(instancia));
        purgados += antes - PUNTUALES.size();
    }

    /** Hard-stop de todo evento/FX ambiental Jobs conocido. */
    public static void detenerTodo() {
        if (PUNTUALES.isEmpty()) return;
        var sonidos = Minecraft.getInstance().getSoundManager();
        for (SoundInstance instancia : PUNTUALES) {
            sonidos.stop(instancia);
        }
        PUNTUALES.clear();
        cierres++;
    }

    /**
     * Un resource reload reemplaza el SoundEngine: las referencias anteriores
     * ya no describen instancias reproducibles y deben descartarse.
     */
    public static void recursosRecargados() {
        PUNTUALES.clear();
    }

    public static int cantidad() {
        return PUNTUALES.size();
    }

    public static long registradosParaDiagnostico() {
        return registrados;
    }

    public static long purgadosParaDiagnostico() {
        return purgados;
    }

    public static long cierresParaDiagnostico() {
        return cierres;
    }
}
