package com.santipdr.jobsmenu.client.scene.planta;

import com.mojang.blaze3d.platform.NativeImage;
import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.Objects;

/**
 * Planta para los PNG entregados por el proyecto (niveles 10-17).
 *
 * Desde 0.13.0 estas imagenes son deliberadamente estaticas: no hay zoom,
 * paneo, scanlines, niebla animada, flicker ni desplazamientos de color. El
 * renderer solo hace un recorte cover centrado, una integracion estatica muy
 * leve y los apagones/transiciones que pertenecen al flujo general del menu.
 */
public final class PlantaImagen implements Planta {

    private final ResourceLocation textura;
    private final int modo;

    private Boolean texturaValida;
    private int anchoTextura = 1;
    private int altoTextura = 1;
    private boolean falloRegistrado;

    public PlantaImagen(String archivo, int modo) {
        this.textura = Objects.requireNonNull(
                ResourceLocation.tryBuild(JobsMenu.MOD_ID, "textures/backgrounds/" + archivo));
        this.modo = modo;
    }

    /**
     * Compatibilidad con declaraciones antiguas de Nivel. Las dimensiones se
     * ignoran: la fuente de verdad sigue siendo el PNG real.
     */
    public PlantaImagen(String archivo, int anchoIgnorado, int altoIgnorado, int modo) {
        this(archivo, modo);
    }

    @Override
    public void dibujar(GuiGraphics g, Marco marco, Nivel nivel, float luz, float tiempo) {
        int w = marco.ancho();
        int h = marco.alto();

        if (texturaDisponible()) {
            dibujarImagen(g, w, h);
        } else {
            dibujarFallback(g, w, h, nivel);
        }

        integrarEstatico(g, w, h, nivel);

        // La imagen no pulsa con el fluorescente. Solo responde al estado de
        // energia/transicion general que llega desde EscenaNivel.
        float oscuridad = 1.0F - limitar(luz, 0.0F, 1.0F);
        if (oscuridad > 0.001F) {
            g.fill(0, 0, w, h, Paleta.conAlfa(Paleta.VANO, 0.92F * oscuridad));
        }
    }

    /** Valida una vez usando el mismo decodificador PNG que usa Minecraft. */
    private boolean texturaDisponible() {
        if (texturaValida != null) {
            return texturaValida;
        }

        var recurso = Minecraft.getInstance().getResourceManager().getResource(textura);
        if (recurso.isEmpty()) {
            registrarFallo("recurso ausente", null);
            texturaValida = false;
            return false;
        }

        try (InputStream entrada = recurso.get().open(); NativeImage imagen = NativeImage.read(entrada)) {
            anchoTextura = Math.max(1, imagen.getWidth());
            altoTextura = Math.max(1, imagen.getHeight());
            texturaValida = true;
            return true;
        } catch (Exception error) {
            registrarFallo("PNG no decodificable", error);
            texturaValida = false;
            return false;
        }
    }

    private void registrarFallo(String motivo, Exception error) {
        if (falloRegistrado) {
            return;
        }
        falloRegistrado = true;
        if (error == null) {
            JobsMenu.LOG.warn("Jobs Menu: fondo {} invalido: {}. Se usara fallback procedural.", textura, motivo);
        } else {
            JobsMenu.LOG.warn("Jobs Menu: fondo {} invalido: {}. Se usara fallback procedural.", textura, motivo, error);
        }
    }

    /** Cover centrado y estable: el mismo pixel fuente queda siempre en sitio. */
    private void dibujarImagen(GuiGraphics g, int w, int h) {
        float escalaPantalla = Math.max(w / (float) anchoTextura, h / (float) altoTextura);
        float visibleW = Math.min(anchoTextura, w / escalaPantalla);
        float visibleH = Math.min(altoTextura, h / escalaPantalla);
        float u = Math.max(0.0F, (anchoTextura - visibleW) * 0.5F);
        float v = Math.max(0.0F, (altoTextura - visibleH) * 0.5F);

        int regionW = Math.max(1, Math.min(anchoTextura, Math.round(visibleW)));
        int regionH = Math.max(1, Math.min(altoTextura, Math.round(visibleH)));
        g.blit(textura, 0, 0, w, h, u, v, regionW, regionH, anchoTextura, altoTextura);
    }

    private void dibujarFallback(GuiGraphics g, int w, int h, Nivel nivel) {
        int mitad = Math.max(1, h / 2);
        g.fillGradient(0, 0, w, mitad, nivel.paredAlta, nivel.paredBaja);
        g.fillGradient(0, mitad, w, h, nivel.suelo, nivel.fondo);

        int cx = w / 2;
        int cy = (int) (h * 0.53F);
        int huecoW = Math.max(24, w / 7);
        int huecoH = Math.max(32, h / 3);
        g.fill(cx - huecoW / 2, cy - huecoH / 2,
                cx + huecoW / 2, cy + huecoH / 2, nivel.fondo);
    }

    /**
     * Integracion fija para que el PNG no parezca una capa pegada sobre la UI.
     * No usa tiempo y por tanto no produce ningun movimiento perceptible.
     */
    private void integrarEstatico(GuiGraphics g, int w, int h, Nivel nivel) {
        int bordeX = Math.max(10, w / 20);
        int bordeY = Math.max(8, h / 22);
        float vignette = 0.075F;
        g.fill(0, 0, bordeX, h, Paleta.conAlfa(Paleta.VANO, vignette));
        g.fill(w - bordeX, 0, w, h, Paleta.conAlfa(Paleta.VANO, vignette));
        g.fill(0, 0, w, bordeY, Paleta.conAlfa(Paleta.VANO, vignette * 0.70F));
        g.fill(0, h - bordeY, w, h, Paleta.conAlfa(Paleta.VANO, vignette));

        // Tinte casi imperceptible y fijo, derivado de la paleta del propio nivel.
        g.fill(0, 0, w, h, Paleta.conAlfa(nivel.niebla, 0.018F));

        // El modo se conserva para diagnostico/futuras reglas estaticas.
        if (modo < 10) {
            g.fill(0, 0, 1, 1, 0x00000000);
        }
    }

    private static float limitar(float valor, float minimo, float maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    @Override
    public int tramos() {
        return 12;
    }

    @Override
    public float pisoPresencia() {
        return 0.92F;
    }
}
