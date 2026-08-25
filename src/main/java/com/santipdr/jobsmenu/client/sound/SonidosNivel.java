package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.JobsMenu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Los sonidos del aviso.
 *
 * No hay musica en este menu y no la va a haber. Lo que se oye es la
 * instalacion electrica del nivel funcionando sola, y el ruido que hace el
 * papeleo cuando alguien lo toca.
 */
public final class SonidosNivel {

    private SonidosNivel() {
    }

    public static final DeferredRegister<SoundEvent> REGISTRO =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JobsMenu.MOD_ID);

    /** El fluorescente del pasillo. Va en bucle mientras el menu este abierto. */
    public static final RegistryObject<SoundEvent> ZUMBIDO = registrar("ambiente.zumbido");

    /** El cursor pasa por un renglon. Papel, apenas. */
    public static final RegistryObject<SoundEvent> RECORRER = registrar("aviso.recorrer");

    /** Se marca la casilla de un renglon. */
    public static final RegistryObject<SoundEvent> MARCAR = registrar("aviso.marcar");

    /** Se abre otra pantalla. Interruptor de pared. */
    public static final RegistryObject<SoundEvent> PESADO = registrar("aviso.pesado");

    /** El nivel se apaga para dar paso al siguiente. */
    public static final RegistryObject<SoundEvent> APAGON = registrar("nivel.apagon");

    /** El nivel nuevo prende. */
    public static final RegistryObject<SoundEvent> ENCENDIDO = registrar("nivel.encendido");

    private static RegistryObject<SoundEvent> registrar(String nombre) {
        return REGISTRO.register(nombre,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(JobsMenu.MOD_ID, nombre)));
    }

    public static void inscribir(IEventBus bus) {
        REGISTRO.register(bus);
    }
}
