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

/**
 * Planta para fondos entregados para el menu.
 *
 * La textura se valida una vez con el mismo decodificador PNG que usa Minecraft.
 * Sus dimensiones se leen del propio archivo para que una futura sustitucion de
 * assets no pueda volver a romper las UV. Los fondos reciben movimiento lento,
 * luz ambiental y efectos propios sin convertir la interfaz en un video pesado.
 */
public final class PlantaImagen implements Planta {

    private final ResourceLocation textura;
    private final int modo;

    private Boolean texturaValida;
    private int anchoTextura = 1;
    private int altoTextura = 1;
    private boolean falloRegistrado;

    public PlantaImagen(String archivo, int modo) {
        this.textura = new ResourceLocation(JobsMenu.MOD_ID, "textures/backgrounds/" + archivo);
        this.modo = modo;
    }

    @Override
    public void dibujar(GuiGraphics g, Marco marco, Nivel nivel, float luz, float tiempo) {
        int w = marco.ancho();
        int h = marco.alto();

        if (texturaDisponible()) {
            dibujarImagen(g, w, h, tiempo);
        } else {
            dibujarFallback(g, w, h, nivel, tiempo);
        }

        ambiente(g, w, h, nivel, luz, tiempo);

        float oscuridad = 1.0F - limitar(luz, 0.0F, 1.0F);
        if (oscuridad > 0.001F) {
            g.fill(0, 0, w, h, Paleta.conAlfa(Paleta.VANO, 0.92F * oscuridad));
        }
    }

    /**
     * Comprueba el recurso con NativeImage, no solo su existencia. Un PNG puede
     * tener cabecera e IEND correctos y aun contener un IDAT corrupto; ese era el
     * caso que producia el cubo morado/negro en varios niveles.
     */
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

    /**
     * Cover con overscan animado. El recorte es siempre un poco menor que la
     * textura visible, asi que incluso una imagen 16:9 puede respirar y desplazarse
     * suavemente sin mostrar bordes.
     */
    private void dibujarImagen(GuiGraphics g, int w, int h, float tiempo) {
        float escalaPantalla = Math.max(w / (float) anchoTextura, h / (float) altoTextura);
        float baseVisibleW = Math.min(anchoTextura, w / escalaPantalla);
        float baseVisibleH = Math.min(altoTextura, h / escalaPantalla);

        float intensidad = (modo == 15 || modo == 17) ? 1.0F : (modo == 16 ? 0.42F : 0.68F);
        float pulso = (float) Math.sin(tiempo * 0.026F + modo * 0.61F);
        float zoom = 1.026F + intensidad * (0.009F + 0.008F * pulso);

        float visibleW = baseVisibleW / zoom;
        float visibleH = baseVisibleH / zoom;
        float margenX = Math.max(0.0F, anchoTextura - visibleW);
        float margenY = Math.max(0.0F, altoTextura - visibleH);

        float paneoX = (float) Math.sin(tiempo * 0.017F + modo * 0.83F) * margenX * 0.34F;
        float paneoY = (float) Math.cos(tiempo * 0.013F + modo * 1.07F) * margenY * 0.31F;

        float u = margenX * 0.5F + paneoX;
        float v = margenY * 0.5F + paneoY;
        u = limitar(u, 0.0F, margenX);
        v = limitar(v, 0.0F, margenY);

        int regionW = Math.max(1, Math.min(anchoTextura, Math.round(visibleW)));
        int regionH = Math.max(1, Math.min(altoTextura, Math.round(visibleH)));
        g.blit(textura, 0, 0, w, h, u, v, regionW, regionH, anchoTextura, altoTextura);
    }

    private void dibujarFallback(GuiGraphics g, int w, int h, Nivel nivel, float tiempo) {
        int mitad = Math.max(1, h / 2);
        g.fillGradient(0, 0, w, mitad, nivel.paredAlta, nivel.paredBaja);
        g.fillGradient(0, mitad, w, h, nivel.suelo, nivel.fondo);

        int cx = w / 2;
        int cy = (int) (h * 0.53F);
        int huecoW = Math.max(24, w / 7);
        int huecoH = Math.max(32, h / 3);
        int pulso = Math.round((float) Math.sin(tiempo * 0.17F + modo) * 2.0F);
        g.fill(cx - huecoW / 2 - pulso, cy - huecoH / 2,
                cx + huecoW / 2 + pulso, cy + huecoH / 2, nivel.fondo);
    }

    private void ambiente(GuiGraphics g, int w, int h, Nivel nivel, float luz, float tiempo) {
        float onda = 0.5F + 0.5F * (float) Math.sin(tiempo * (0.20F + modo * 0.013F));
        float a = (0.018F + 0.025F * onda) * luz;

        // Vignette suave para integrar imagen, botones y tipografia como una sola escena.
        int bordeX = Math.max(10, w / 18);
        int bordeY = Math.max(8, h / 20);
        float vignette = 0.070F + 0.025F * onda;
        g.fill(0, 0, bordeX, h, Paleta.conAlfa(Paleta.VANO, vignette));
        g.fill(w - bordeX, 0, w, h, Paleta.conAlfa(Paleta.VANO, vignette));
        g.fill(0, 0, w, bordeY, Paleta.conAlfa(Paleta.VANO, vignette * 0.75F));
        g.fill(0, h - bordeY, w, h, Paleta.conAlfa(Paleta.VANO, vignette));

        // Una linea de exposicion muy tenue recorre todos los fondos de imagen.
        int recorrido = Math.max(1, h + 48);
        int yScan = Math.floorMod((int) (tiempo * 2.2F + modo * 37.0F), recorrido) - 24;
        if (yScan >= 0 && yScan < h) {
            g.fill(0, yScan, w, Math.min(h, yScan + 1), Paleta.conAlfa(nivel.luz, 0.020F * luz));
        }

        if (modo == 10 || modo == 15) {
            // Contencion / Executor: respiracion roja y chispas verticales lentas.
            int y = (int) (h * 0.58F);
            float fuerza = modo == 15 ? 1.55F : 1.25F;
            g.fill(0, y, w, h, Paleta.conAlfa(0xFFFF3A18, a * fuerza));
            g.fill(0, 0, w, h / 5, Paleta.conAlfa(Paleta.VANO, 0.18F));
            for (int i = 0; i < 3; i++) {
                float fase = tiempo * (0.065F + i * 0.009F) + modo * 0.7F + i * 2.1F;
                int x = (int) ((0.5F + 0.5F * Math.sin(fase)) * Math.max(1, w - 2));
                int yy = (int) ((0.22F + 0.16F * i + 0.04F * Math.cos(fase * 0.7F)) * h);
                int largo = Math.max(4, h / 30);
                g.fill(x, yy, Math.min(w, x + 1), Math.min(h, yy + largo),
                        Paleta.conAlfa(0xFFFF8A52, (0.025F + i * 0.008F) * luz));
            }
        }

        if (modo == 11 || modo == 13) {
            // Luz calida respirada. En 13 simula candelabros sin dibujar fuego falso.
            int y = (int) (h * (0.66F + 0.02F * onda));
            g.fill(0, y, w, Math.min(h, y + 2), Paleta.conAlfa(0xFFFFC06A, a * 0.75F));
            if (modo == 13) {
                float flicker = 0.018F + 0.018F * (0.5F + 0.5F * (float) Math.sin(tiempo * 0.41F));
                g.fill(0, 0, w, h, Paleta.conAlfa(0xFFFFA63D, flicker * luz));
            }
        }

        if (modo == 11 || modo == 12 || modo == 14) {
            int banda = Math.max(2, h / 90);
            int y = (int) (h * (0.42F + 0.04F * (float) Math.sin(tiempo * 0.11F + modo)));
            g.fill(0, y, w, Math.min(h, y + banda), Paleta.conAlfa(0xFF7CFF73, a * 0.55F));
        }

        if (modo == 16) {
            // Prisma: glint frio que deriva lentamente, sin flashes bruscos.
            int banda = Math.max(1, h / 120);
            int y = (int) (h * (0.34F + 0.12F * onda));
            g.fill(0, y, w, Math.min(h, y + banda), Paleta.conAlfa(0xFFDDE6E6, a * 0.42F));
            g.fill(0, 0, w, h, Paleta.conAlfa(0xFF0A0B0D, 0.06F));
        }

        if (modo == 17) {
            // Galeria de sombra: niebla azul en dos capas y bordes que respiran.
            int y = (int) (h * (0.52F + 0.025F * Math.sin(tiempo * 0.037F)));
            g.fill(0, y, w, h, Paleta.conAlfa(0xFF2454A8, a * 0.72F));
            int banda = Math.max(8, h / 15);
            int y2 = (int) (h * (0.69F + 0.035F * Math.cos(tiempo * 0.029F + 1.3F)));
            g.fill(0, y2, w, Math.min(h, y2 + banda), Paleta.conAlfa(0xFF17386F, 0.028F * luz));
            g.fill(0, 0, w, h / 4, Paleta.conAlfa(Paleta.VANO, 0.10F));
        }

        g.fill(0, 0, w, h, Paleta.conAlfa(nivel.niebla, 0.035F));
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
