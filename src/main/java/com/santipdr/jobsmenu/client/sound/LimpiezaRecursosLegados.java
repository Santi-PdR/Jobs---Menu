package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.JobsMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

/** Retira el resource pack temporal usado por versiones anteriores. */
public final class LimpiezaRecursosLegados {

    private static final String PAQUETE_MUSICA = "jobsmenu-musica-activa";
    private static boolean ejecutada;

    private LimpiezaRecursosLegados() {
    }

    public static void ejecutar() {
        if (ejecutada) return;
        ejecutada = true;

        Minecraft cliente = Minecraft.getInstance();
        PackRepository repositorio = cliente.getResourcePackRepository();
        boolean recargar = false;

        try {
            repositorio.reload();
            Set<String> seleccion = new LinkedHashSet<>(repositorio.getSelectedIds());
            if (seleccion.removeIf(LimpiezaRecursosLegados::esPaqueteMusica)) {
                repositorio.setSelected(seleccion);
                cliente.options.updateResourcePacks(repositorio);
                cliente.options.save();
                recargar = true;
            }

            Path raiz = cliente.gameDirectory.toPath()
                    .resolve("resourcepacks").resolve(PAQUETE_MUSICA);
            if (Files.exists(raiz)) {
                try (var rutas = Files.walk(raiz)) {
                    for (Path ruta : rutas.sorted(Comparator.reverseOrder()).toList()) {
                        Files.deleteIfExists(ruta);
                    }
                }
                recargar = true;
                JobsMenu.LOG.info("[jobsmenu] Resource pack de musica legado retirado; la pista vive dentro del mod.");
            }
        } catch (IOException | RuntimeException fallo) {
            JobsMenu.LOG.warn("[jobsmenu] No se pudo retirar por completo el resource pack de musica legado.", fallo);
        }

        if (recargar) cliente.reloadResourcePacks();
    }

    private static boolean esPaqueteMusica(String id) {
        return id.equals(PAQUETE_MUSICA)
                || id.endsWith("/" + PAQUETE_MUSICA)
                || id.endsWith(":" + PAQUETE_MUSICA);
    }
}
