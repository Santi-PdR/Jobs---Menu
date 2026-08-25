package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

/**
 * La mesa de mezcla del menu.
 *
 * Todo lo que suena pasa por aca, y aca esta decidido de una vez cuanto pesa
 * cada cosa. El orden es deliberado y no se negocia por sonido:
 *
 *   musica    - sostiene la escena, nunca la tapa
 *   ambiente  - se escucha por debajo de la musica, siempre presente
 *   evento    - asoma y se va; no debe hacer levantar la cabeza
 *   interfaz  - lo mas breve y lo mas bajo, porque es lo que mas se repite
 *   nivel     - la transicion, unica autorizada a pisar al resto un momento
 *
 * Tener los pesos en un solo lugar es lo que hace que la mezcla se pueda
 * corregir sin salir a buscar constantes por diez archivos.
 */
public final class MezclaAudio {

    private MezclaAudio() {
    }

    /** Tema del menu. Ambiental: acompana, no protagoniza. */
    public static final float MUSICA = 0.42F;

    /** Ambiente base del nivel, ya multiplicado por el volumen de la config. */
    public static final float AMBIENTE = 0.80F;

    /** Eventos ocasionales del nivel. */
    public static final float EVENTO = 0.55F;

    /** Gestos de interfaz. */
    public static final float INTERFAZ = 0.50F;

    /** Apagon y encendido. Se les permite mandar durante la transicion. */
    public static final float TRANSICION = 0.85F;

    /** La figura. Apenas por encima del piso de ruido, a proposito. */
    public static final float FIGURA = 0.40F;

    /**
     * Cuanto baja el resto mientras la figura esta presente.
     *
     * No se corta nada: se afloja. Un corte se nota como un corte y delata el
     * truco; una bajada de un tercio se siente como que el aire se puso denso.
     */
    public static final float AGACHE_FIGURA = 0.62F;

    /**
     * Dispara un gesto de interfaz.
     *
     * Todos llevan una variacion minima de tono. Reproducir siempre la misma
     * muestra al mismo tono es lo que hace que un sonido de UI se vuelva
     * insoportable a los cincuenta usos: el oido aprende el archivo. Un dos por
     * ciento arriba o abajo alcanza para que no lo aprenda nunca.
     */
    public static void gesto(RegistryObject<SoundEvent> evento, float volumen) {
        if (!ConfigTurno.sonidoBotones()) {
            return;
        }
        float tono = 0.98F + (float) Math.random() * 0.04F;
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(evento.get(), tono, volumen * INTERFAZ));
    }

    /** Un sonido de ambiente suelto, sin posicion, con tono y volumen dados. */
    public static void ambiental(RegistryObject<SoundEvent> evento, float volumen, float tono) {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(evento.get(), tono, volumen));
    }
}
