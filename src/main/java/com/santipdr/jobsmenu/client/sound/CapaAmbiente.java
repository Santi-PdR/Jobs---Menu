package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.client.scene.Presencia;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * El room tone de un nivel, sonando en bucle mientras ese nivel este a la vista.
 *
 * Hay una instancia viva por nivel, no una sola que cambia de archivo: asi los
 * cuatro ambientes pueden solaparse durante la transicion y el pasillo nuevo
 * empieza a escucharse mientras el viejo todavia se esta yendo. Cambiar el
 * archivo de una unica instancia obligaria a cortar en seco, y el corte se oye.
 *
 * Cada capa se ocupa sola de tres cosas:
 *
 *   - subir cuando su nivel esta en pantalla y bajar cuando no;
 *   - seguir a la luz, porque casi todo lo que suena en el pasillo es electrico
 *     y si los tubos se apagan la instalacion se apaga con ellos;
 *   - agacharse cuando hay una presencia al fondo.
 *
 * Se apaga sola y se descarta cuando termino de bajar del todo.
 */
public class CapaAmbiente extends AbstractTickableSoundInstance {

    /** Cuanto se tarda en llegar al volumen pedido, por tick. Lento a proposito. */
    private static final float SUAVIZADO_SUBIDA = 0.035F;

    /** Bajar es mas rapido que subir, pero no tanto como para que se note. */
    private static final float SUAVIZADO_BAJADA = 0.055F;

    private final int nivel;
    private float actual;

    /** Ticks vividos, para la respiracion lenta del volumen. */
    private int edad;

    public CapaAmbiente(SoundEvent evento, int nivel) {
        super(evento, SoundSource.AMBIENT, RandomSource.create());
        this.nivel = nivel;
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

        // Cada nivel tiene su propia red electrica y su propio tamano: un tono
        // ligeramente distinto por nivel hace que no se perciba que las cuatro
        // capas salen del mismo generador.
        this.pitch = 0.96F + 0.03F * nivel;
    }

    /** El nivel al que pertenece esta capa. */
    public int nivel() {
        return this.nivel;
    }

    /**
     * Autoriza a nacer en silencio.
     *
     * Es la linea de la que dependia todo el ambiente del menu. El motor de
     * sonido descarta cualquier instancia cuyo volumen sea cero en el momento
     * de arrancar, y no la vuelve a mirar nunca: se pierde en el mismo
     * fotograma en que se la crea. Como esta capa entra siempre desde cero
     * para poder subir sin escalon, sin esto no llegaba a sonar jamas, por muy
     * bien registrada que estuviera en sounds.json.
     */
    @Override
    public boolean canStartSilent() {
        return true;
    }

    /** Si ya se apago del todo y se puede tirar. */
    public boolean agotada() {
        return this.isStopped();
    }

    @Override
    public void tick() {
        this.edad++;

        Minecraft cliente = Minecraft.getInstance();
        boolean enMenu = cliente.screen instanceof com.santipdr.jobsmenu.client.screen.PantallaNivel;
        boolean mia = RotacionNiveles.indiceActual() == this.nivel;
        boolean permitido = enMenu && mia && ConfigTurno.sonidoAmbiente();

        float objetivo = 0.0F;
        if (permitido) {
            objetivo = ConfigTurno.volumenAmbiente() * MezclaAudio.AMBIENTE;

            // La instalacion depende de la luz. Nunca llega a cero del todo:
            // aun sin corriente queda el aire moviendose por los conductos.
            float luz = RotacionNiveles.luzDisponible();
            objetivo *= 0.30F + 0.70F * luz;

            // Respiracion muy lenta, del orden del minuto. Es lo que impide que
            // un bucle de veinte segundos se sienta como un bucle.
            float t = this.edad / 20.0F;
            objetivo *= 1.0F + 0.06F * (float) Math.sin(t * 0.083F)
                    + 0.03F * (float) Math.sin(t * 0.031F + 1.7F);

            // Algo al fondo del pasillo: el ambiente se retira.
            objetivo *= 1.0F - (1.0F - MezclaAudio.AGACHE_FIGURA) * Presencia.visibilidad();
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
