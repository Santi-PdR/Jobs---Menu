package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * El zumbido del fluorescente, sonando mientras el aviso este en pantalla.
 *
 * No es una pista de musica: es una instancia que se sostiene sola y sigue lo
 * que hace la luz. Cuando el nivel se apaga para cambiar, el zumbido se apaga
 * con el; cuando el tubo nuevo arranca a los tirones, el zumbido arranca igual.
 * Que el oido y el ojo digan lo mismo es la mitad del efecto.
 */
public class ZumbidoNivel extends AbstractTickableSoundInstance {

    /** Cuanto se demora en llegar al volumen pedido, por tick. */
    private static final float SUAVIZADO = 0.08F;

    private float actual;

    public ZumbidoNivel() {
        super(SonidosNivel.ZUMBIDO.get(), SoundSource.AMBIENT, RandomSource.create());
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
    }

    @Override
    public void tick() {
        Minecraft cliente = Minecraft.getInstance();

        // Solo suena con el aviso delante. Si el jugador se fue a otra pantalla,
        // el pasillo se queda callado.
        boolean visible = cliente.screen instanceof com.santipdr.jobsmenu.client.screen.PantallaNivel;
        boolean permitido = visible && ConfigTurno.sonidoAmbiente();

        float objetivo = 0.0F;
        if (permitido) {
            // El zumbido sigue a la luz: si el tubo titila, el sonido titila.
            objetivo = ConfigTurno.volumenAmbiente() * (0.35F + 0.65F * RotacionNiveles.luzDisponible());
        }

        this.actual += (objetivo - this.actual) * SUAVIZADO;
        if (this.actual < 0.001F) {
            this.actual = 0.0F;
        }
        this.volume = this.actual;

        // La red electrica de un nivel no es la de otro.
        this.pitch = 0.94F + 0.12F * RotacionNiveles.indiceActual() / Math.max(1.0F, 3.0F);

        if (!visible && this.actual <= 0.0F) {
            this.stop();
        }
    }
}
