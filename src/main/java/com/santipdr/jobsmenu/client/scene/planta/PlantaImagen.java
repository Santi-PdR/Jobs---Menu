package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Planta para fondos fotograficos entregados para el menu.
 *
 * No estira la imagen: usa un recorte tipo cover, centrado, y despues la mete
 * dentro del mismo sistema de luz de los recintos procedurales. Por eso un
 * apagon, La Suspension o la penumbra de ronda tambien afectan a estas escenas.
 */
public final class PlantaImagen implements Planta {

    private final ResourceLocation textura;
    private final int anchoTextura;
    private final int altoTextura;
    private final int modo;

    public PlantaImagen(String archivo, int anchoTextura, int altoTextura, int modo) {
        this.textura = new ResourceLocation(JobsMenu.MOD_ID, "textures/backgrounds/" + archivo);
        this.anchoTextura = anchoTextura;
        this.altoTextura = altoTextura;
        this.modo = modo;
    }

    @Override
    public void dibujar(GuiGraphics g, Marco marco, Nivel nivel, float luz, float tiempo) {
        int w = marco.ancho();
        int h = marco.alto();

        float escalaPantalla = Math.max(w / (float) anchoTextura, h / (float) altoTextura);
        float visibleW = w / escalaPantalla;
        float visibleH = h / escalaPantalla;

        float respiracionX = (float) Math.sin(tiempo * 0.071F + modo * 0.73F) * visibleW * 0.012F;
        float respiracionY = (float) Math.sin(tiempo * 0.053F + modo * 1.11F) * visibleH * 0.010F;

        float u = (anchoTextura - visibleW) * 0.5F + respiracionX;
        float v = (altoTextura - visibleH) * 0.5F + respiracionY;
        u = limitar(u, 0.0F, Math.max(0.0F, anchoTextura - visibleW));
        v = limitar(v, 0.0F, Math.max(0.0F, altoTextura - visibleH));

        g.blit(textura, 0, 0, w, h, u, v, visibleW, visibleH, anchoTextura, altoTextura);

        ambiente(g, w, h, nivel, luz, tiempo);

        float oscuridad = 1.0F - limitar(luz, 0.0F, 1.0F);
        if (oscuridad > 0.001F) {
            g.fill(0, 0, w, h, Paleta.conAlfa(Paleta.VANO, 0.92F * oscuridad));
        }
    }

    private void ambiente(GuiGraphics g, int w, int h, Nivel nivel, float luz, float tiempo) {
        float onda = 0.5F + 0.5F * (float) Math.sin(tiempo * (0.20F + modo * 0.013F));
        float a = (0.018F + 0.025F * onda) * luz;

        if (modo == 10) {
            // Contencion: el rojo ya existe en la imagen. Solo respira el calor
            // de abajo; no anadimos rojo a ningun otro recinto.
            int y = (int) (h * 0.58F);
            g.fill(0, y, w, h, Paleta.conAlfa(0xFFFF3A18, a * 1.35F));
            g.fill(0, 0, w, h / 5, Paleta.conAlfa(Paleta.VANO, 0.16F));
            return;
        }

        if (modo == 11 || modo == 13) {
            // Luz calida secundaria en salas donde las luminarias son visibles.
            int y = (int) (h * (0.66F + 0.02F * onda));
            g.fill(0, y, w, Math.min(h, y + 2), Paleta.conAlfa(0xFFFFC06A, a * 0.75F));
        }

        if (modo == 11 || modo == 12 || modo == 14) {
            // Respiracion de instalacion verde, tenue y amplia.
            int banda = Math.max(2, h / 90);
            int y = (int) (h * (0.42F + 0.04F * (float) Math.sin(tiempo * 0.11F + modo)));
            g.fill(0, y, w, y + banda, Paleta.conAlfa(0xFF7CFF73, a * 0.55F));
        }

        // Capa de aire comun: suficiente para unir la foto con la UI sin
        // convertirla en una postal lavada.
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
