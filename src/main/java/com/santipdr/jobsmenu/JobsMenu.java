package com.santipdr.jobsmenu;

import com.mojang.logging.LogUtils;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

import org.slf4j.Logger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Punto de entrada del mod.
 *
 * Este mod es exclusivamente de cliente: no registra nada del lado del servidor
 * y no altera ninguna mecanica de juego. Su unico trabajo es vestir los menus.
 */
@Mod(JobsMenu.MOD_ID)
public class JobsMenu {

    public static final String MOD_ID = "jobsmenu";

    /**
     * Registro del mod.
     *
     * Existe para una sola cosa: cuando el jugador deja una pista de musica
     * que Minecraft no puede reproducir, hay que poder decirselo. Un mod que
     * se queda mudo sin explicar por que obliga a adivinar, y adivinar es
     * exactamente lo que este sistema tiene que evitar.
     */
    public static final Logger LOG = LogUtils.getLogger();

    public JobsMenu() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigTurno.SPEC, "jobsmenu-client.toml");
            SonidosNivel.inscribir(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }
}
