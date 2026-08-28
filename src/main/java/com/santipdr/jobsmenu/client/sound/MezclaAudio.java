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

    /**
     * MARGEN DE MEZCLA
     *
     * Con las cifras anteriores, el peor caso -cama y caracter y actividad y
     * musica y un evento y el apagon y un gesto de interfaz, todo cayendo en
     * el mismo instante- llegaba a 0.94 de pico: medio decibelio de margen.
     * Eso no es una mezcla con cabeza, es una mezcla que todavia no distorsiono
     * de casualidad, y basta con que el jugador suba el volumen maestro o que
     * un resource pack cambie una pieza para que empiece a recortar.
     *
     * Bajar tres decibelios el conjunto no se oye -el volumen maestro lo
     * compensa- y compra el margen que hace falta para que las coincidencias
     * raras no rompan nada.
     */

    /**
     * Tema del menu.
     *
     * Subido de 0.34 a 0.55 en 0.6.4. Con la pista propia sintetizada el 0.34
     * bastaba -era un lecho armonico de fondo-, pero la pista actual (REQUIEM)
     * es un tema de lobby con melodia, pensado para escucharse, y a 0.34 con la
     * entrada lenta el jugador entraba, miraba y salia sin oir una nota. Sigue
     * por debajo de un evento o de la transicion: acompana, pero ahora se oye.
     */
    public static final float MUSICA = 0.55F;

    /** Ambiente base del nivel, ya multiplicado por el volumen de la config. */
    public static final float AMBIENTE = 0.66F;

    /** Eventos ocasionales del nivel. */
    public static final float EVENTO = 0.48F;

    /**
     * Gestos de interfaz.
     *
     * Se sube respecto del resto, no se baja. Las ocho piezas se remezclaron
     * con un balance propio -pasar suena siete decibelios por debajo de
     * confirmar porque suena treinta veces mas seguido- y ese balance ya deja
     * los gestos frecuentes muy abajo. Aplicarles ademas la reduccion general
     * los habria dejado por debajo del piso del ambiente.
     */
    public static final float INTERFAZ = 0.54F;

    /** Apagon y encendido. Se les permite mandar durante la transicion. */
    public static final float TRANSICION = 0.72F;

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

