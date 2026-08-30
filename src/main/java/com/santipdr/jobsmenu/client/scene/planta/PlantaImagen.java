package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Planta para fondos entregados para el menu.
 *
 * La imagen se dibuja en modo cover sin deformacion y participa de la misma
 * luz que las plantas procedurales. Si el recurso falta o no puede resolverse,
 * se muestra un fondo de seguridad en vez de dejar el menu negro.
 */
public final class PlantaImagen implements Planta {

    private final ResourceLocation textura;
    private final int anchoTextura;
    private final int altoTextura;
    private final int modo;

    public PlantaImagen(String archivo, int anchoTextura, int altoTextura, int modo) {
        this.textura = new ResourceLocation(JobsMenu.MOD_ID, "textures/backgrounds/" + archivo);
        this.anchoTextura = Math.max(1, anchoTextura);
        this.altoTextura = Math.max(1, altoTextura);
        this.modo = modo;
    }

    @Override
    public void dibujar(GuiGraphics g, Marco marco, Nivel nivel, float luz, float tiempo) {
        int w = marco.ancho();
        int h = marco.alto();

        boolean disponible = Minecraft.getInstance().getResourceManager().getResource(textura).isPresent();
        if (disponible) {
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

    private void dibujarImagen(GuiGraphics g, int w, int h, float tiempo) {
        float escalaPantalla = Math.max(w / (float) anchoTextura, h / (float) altoTextura);
        float visibleW = Math.min(anchoTextura, w / escalaPantalla);
        float visibleH = Math.min(altoTextura, h / escalaPantalla);

        float respiracionX = (float) Math.sin(tiempo * 0.071F + modo * 0.73F) * visibleW * 0.010F;
        float respiracionY = (float) Math.sin(tiempo * 0.053F + modo * 1.11F) * visibleH * 0.008F;

        float u = (anchoTextura - visibleW) * 0.5F + respiracionX;
        float v = (altoTextura - visibleH) * 0.5F + respiracionY;
        u = limitar(u, 0.0F, Math.max(0.0F, anchoTextura - visibleW));
        v = limitar(v, 0.0F, Math.max(0.0F, altoTextura - visibleH));

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

        if (modo == 10 || modo == 15) {
            // Rojo reservado a zonas relacionadas con Executores.
            int y = (int) (h * 0.58F);
            float fuerza = modo == 15 ? 1.55F : 1.25F;
            g.fill(0, y, w, h, Paleta.conAlfa(0xFFFF3A18, a * fuerza));
            g.fill(0, 0, w, h / 5, Paleta.conAlfa(Paleta.VANO, 0.18F));
        }

        if (modo == 11 || modo == 13) {
            int y = (int) (h * (0.66F + 0.02F * onda));
            g.fill(0, y, w, Math.min(h, y + 2), Paleta.conAlfa(0xFFFFC06A, a * 0.75F));
        }

        if (modo == 11 || modo == 12 || modo == 14) {
            int banda = Math.max(2, h / 90);
            int y = (int) (h * (0.42F + 0.04F * (float) Math.sin(tiempo * 0.11F + modo)));
            g.fill(0, y, w, y + banda, Paleta.conAlfa(0xFF7CFF73, a * 0.55F));
        }

        if (modo == 16) {
            // Prisma: contraste frio casi monocromo. Nada pulsa de golpe.
            int banda = Math.max(1, h / 120);
            int y = (int) (h * (0.34F + 0.12F * onda));
            g.fill(0, y, w, y + banda, Paleta.conAlfa(0xFFDDE6E6, a * 0.42F));
            g.fill(0, 0, w, h, Paleta.conAlfa(0xFF0A0B0D, 0.06F));
        }

        if (modo == 17) {
            // Galeria de sombra: niebla azul muy tenue, sin convertir la figura
            // del propio fondo en un jumpscare.
            int y = (int) (h * 0.54F);
            g.fill(0, y, w, h, Paleta.conAlfa(0xFF2454A8, a * 0.65F));
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
