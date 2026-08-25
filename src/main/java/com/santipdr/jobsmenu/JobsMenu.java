package com.santipdr.jobsmenu;

import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

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

    /** Version visible en el sello del menu. Se sincroniza a mano con gradle.properties. */
    public static final String VERSION = "0.4.0";

    public JobsMenu() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigTurno.SPEC, "jobsmenu-client.toml");
            SonidosNivel.inscribir(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }
}
