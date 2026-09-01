package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.JobsMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Retira el resource pack temporal usado por versiones anteriores. */
public final class LimpiezaRecursosLegados {

    private static final Set<String> PAQUETES_MUSICA = Set.of(
            "jobsmenu-musica-activa",
            "jobsmenu-musica"
    );
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

            Path resourcepacks = cliente.gameDirectory.toPath().resolve("resourcepacks");
            for (String nombre : PAQUETES_MUSICA) {
                for (String candidato : new String[] { nombre, nombre + ".zip" }) {
                    Path raiz = resourcepacks.resolve(candidato);
                    if (Files.exists(raiz)) {
                        try (var rutas = Files.walk(raiz)) {
                            for (Path ruta : rutas.sorted(Comparator.reverseOrder()).toList()) {
                                Files.deleteIfExists(ruta);
                            }
                        }
                        recargar = true;
                        JobsMenu.LOG.info("[jobsmenu] Resource pack de musica legado '{}' retirado.", candidato);
                    }
                }
            }
        } catch (IOException | RuntimeException fallo) {
            JobsMenu.LOG.warn("[jobsmenu] No se pudo retirar por completo el resource pack de musica legado.", fallo);
        }

        if (recargar) cliente.reloadResourcePacks();
    }

    private static boolean esPaqueteMusica(String id) {
        if (id == null) return false;
        String normalizado = id.toLowerCase(Locale.ROOT).replace('\\', '/');
        for (String nombre : PAQUETES_MUSICA) {
            if (normalizado.equals(nombre)
                    || normalizado.equals(nombre + ".zip")
                    || normalizado.endsWith("/" + nombre)
                    || normalizado.endsWith("/" + nombre + ".zip")
                    || normalizado.endsWith(":" + nombre)
                    || normalizado.endsWith(":" + nombre + ".zip")) return true;
        }
        return false;
    }
}
