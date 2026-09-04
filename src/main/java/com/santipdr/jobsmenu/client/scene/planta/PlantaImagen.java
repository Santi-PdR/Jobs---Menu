package com.santipdr.jobsmenu.client.scene.planta;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.Objects;

/**
 * Renderer de los fondos de imagen del proyecto.
 *
 * Niveles 10-17: contrato historico, imagen totalmente estatica.
 * Niveles 18-31: pueden usar un movimiento de camara extremadamente leve para
 * que las escenas nuevas respiren sin deformar el archivo ni tapar su lectura.
 * El movimiento se desactiva con escena quieta, Movimiento reducido o Bajo
 * consumo. Fades, apagones y transiciones globales siguen viviendo fuera.
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
     * ignoran: la fuente de verdad sigue siendo la imagen real.
     */
    public PlantaImagen(String archivo, int anchoIgnorado, int altoIgnorado, int modo) {
        this(archivo, modo);
    }

    @Override
    public void dibujar(GuiGraphics g, Marco marco, Nivel nivel, float luz, float tiempoIgnorado) {
        int w = marco.ancho();
        int h = marco.alto();

        if (texturaDisponible()) {
            dibujarImagen(g, w, h);
        } else {
            dibujarFallback(g, w, h, nivel);
        }

        integrar(g, w, h, nivel);

        float oscuridad = 1.0F - limitar(luz, 0.0F, 1.0F);
        if (oscuridad > 0.001F) {
            g.fill(0, 0, w, h, Paleta.conAlfa(Paleta.VANO, 0.92F * oscuridad));
        }
    }

    /** Valida una vez usando el mismo decodificador de imagen que usa Minecraft. */
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
            registrarFallo("imagen no decodificable", error);
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

    /** Cover estable, con movimiento no destructivo solo en los fondos 18-31. */
    private void dibujarImagen(GuiGraphics g, int w, int h) {
        float escalaPantalla = Math.max(w / (float) anchoTextura, h / (float) altoTextura);
        float visibleWBase = Math.min(anchoTextura, w / escalaPantalla);
        float visibleHBase = Math.min(altoTextura, h / escalaPantalla);

        float intensidad = intensidadMovimiento();
        boolean animar = intensidad > 0.0F
                && ConfigTurno.escenaViva()
                && !ConfigTurno.movimientoReducido()
                && !ConfigTurno.bajoConsumo();

        float visibleW = visibleWBase;
        float visibleH = visibleHBase;
        float centroX = 0.5F;
        float centroY = 0.5F;

        if (animar) {
            float t = (System.currentTimeMillis() % 600_000L) / 1000.0F;
            float fase = modo * 0.731F;
            float pulso = 0.62F + 0.38F * (float) Math.sin(t * 0.105F + fase);
            float zoom = 1.0F + intensidad * (0.72F + 0.28F * pulso);
            visibleW = visibleWBase / zoom;
            visibleH = visibleHBase / zoom;

            centroX += 0.13F * (float) Math.sin(t * 0.043F + fase);
            centroY += 0.08F * (float) Math.sin(t * 0.031F + fase * 1.37F);
        }

        float margenX = Math.max(0.0F, anchoTextura - visibleW);
        float margenY = Math.max(0.0F, altoTextura - visibleH);
        float u = margenX * limitar(centroX, 0.0F, 1.0F);
        float v = margenY * limitar(centroY, 0.0F, 1.0F);

        int regionW = Math.max(1, Math.min(anchoTextura, Math.round(visibleW)));
        int regionH = Math.max(1, Math.min(altoTextura, Math.round(visibleH)));

        RenderSystem.setShaderTexture(0, textura);
        Minecraft.getInstance().getTextureManager().getTexture(textura).setFilter(true, false);
        g.blit(textura, 0, 0, w, h, u, v, regionW, regionH, anchoTextura, altoTextura);
    }

    /**
     * Intensidades deliberadamente pequenas. Los fondos con vacio, luna o
     * circuitos reciben algo mas de respiracion; piedra, interior y bodegon se
     * mantienen practicamente quietos. Los niveles 10-17 siempre devuelven 0.
     */
    private float intensidadMovimiento() {
        return switch (modo) {
            case 18, 25, 27, 30 -> 0.018F;
            case 19, 21, 26, 29, 31 -> 0.012F;
            case 20, 22, 23, 24 -> 0.006F;
            case 28 -> 0.003F;
            default -> 0.0F;
        };
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

    /** Integracion fija sobre la imagen; no genera geometria ni objetos nuevos. */
    private void integrar(GuiGraphics g, int w, int h, Nivel nivel) {
        int bordeX = Math.max(10, w / 20);
        int bordeY = Math.max(8, h / 22);
        float vignette = 0.075F;
        g.fill(0, 0, bordeX, h, Paleta.conAlfa(Paleta.VANO, vignette));
        g.fill(w - bordeX, 0, w, h, Paleta.conAlfa(Paleta.VANO, vignette));
        g.fill(0, 0, w, bordeY, Paleta.conAlfa(Paleta.VANO, vignette * 0.70F));
        g.fill(0, h - bordeY, w, h, Paleta.conAlfa(Paleta.VANO, vignette));
        g.fill(0, 0, w, h, Paleta.conAlfa(nivel.niebla, modo >= 18 ? 0.012F : 0.018F));
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
