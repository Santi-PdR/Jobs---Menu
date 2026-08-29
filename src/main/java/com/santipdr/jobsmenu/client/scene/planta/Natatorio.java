package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Natatorio observado desde la plataforma alta, con el vaso en diagonal. */
public final class Natatorio implements Planta {
    @Override public float pisoPresencia() { return .82F; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int azulejo = Lienzo.tono(n, n.paredAlta, luz, .42F);
        int junta = Lienzo.tono(n, n.junta, luz, .35F);
        Lienzo.fondo(g, w, h, n.fondo, Lienzo.tono(n, n.paredBaja, luz, .55F), 1000);

        // Muro lateral de azulejos: mural destenido y ventanales altos.
        Lienzo.azulejo(g, 0, 0, w, Math.round(h * .55F), azulejo, n.junta,
                Math.max(8, h / 18));
        for (int i = 0; i < 6; i++) {
            int x0 = Math.round(w * (.08F + i * .145F));
            int x1 = Math.round(w * (.18F + i * .145F));
            Lienzo.cristal(g, x0, Math.round(h * .08F), x1, Math.round(h * .31F),
                    0xFF31545A, junta, n.luz, 1020 + i * 19, tiempo);
            Lienzo.quad(g, Math.round(h * .31F), Math.round(h * .78F), x0, x1,
                    x0 - w * .06F, x1 + w * .11F, Paleta.conAlfa(n.luz, .025F * luz));
        }

        // Graderio del fondo, roto por una escalera de acceso central.
        int gradaY = Math.round(h * .39F);
        for (int i = 0; i < 4; i++) {
            int y = gradaY + i * Math.max(8, h / 21);
            int x0 = Math.round(w * (.55F + i * .022F));
            Lienzo.caja(g, x0, y, w, y + Math.max(6, h / 28),
                    Lienzo.tono(n, Paleta.mezclar(n.sueloLejos, n.paredAlta, .36F), luz,
                            .42F - i * .05F));
            Lienzo.caja(g, x0, y, w, y + 2, Paleta.conAlfa(n.luz, .22F * luz));
        }
        Lienzo.quad(g, gradaY, Math.round(h * .65F), w * .72F, w * .79F,
                w * .63F, w * .77F, Paleta.iluminar(n.fondo, .58F));

        // El vaso domina desde mitad de cuadro y se abre en diagonal hacia el
        // espectador. El agua es masa estratificada, no rectangulo azul.
        int aguaY = Math.round(h * .53F);
        Lienzo.quad(g, aguaY, h, w * .10F, w * .79F, -w * .14F, w * .86F,
                Paleta.iluminar(n.sueloLejos, luz * .55F));
        Lienzo.agua(g, -Math.round(w * .14F), aguaY + 5, Math.round(w * .87F), h,
                Paleta.iluminar(n.fondo, .84F), Paleta.iluminar(n.suelo, luz * .54F),
                n.luz, tiempo, 1100);
        Lienzo.linea(g, w * .10F, aguaY, -w * .14F, h, Math.max(3, h / 65),
                Paleta.iluminar(n.techo, luz * .75F));
        Lienzo.linea(g, w * .79F, aguaY, w * .87F, h, Math.max(3, h / 65),
                Paleta.iluminar(n.techo, luz * .66F));
        for (int i = 0; i < 5; i++) {
            float x0 = w * (.20F + i * .115F);
            float x1 = w * (.08F + i * .17F);
            Lienzo.linea(g, x0, aguaY + 5, x1, h, 1,
                    Paleta.conAlfa(i % 2 == 0 ? n.luz : 0xFFE0AB56, .32F));
        }

        // Torre de salto lateral: silueta vertical y trampolines en voladizo.
        int torreX = Math.round(w * .84F);
        Lienzo.caja(g, torreX, Math.round(h * .18F), torreX + Math.max(10, w / 35),
                Math.round(h * .77F), Lienzo.tono(n, n.sueloJunta, luz, .16F));
        for (int i = 0; i < 3; i++) {
            int y = Math.round(h * (.28F + i * .14F));
            int largo = Math.round(w * (.17F - i * .022F));
            Lienzo.caja(g, torreX - largo, y, torreX + w / 25, y + Math.max(5, h / 36),
                    Lienzo.tono(n, n.techo, luz, .20F + i * .10F));
            Lienzo.linea(g, torreX, y, torreX - largo + 3, y + h * .08F, 2, junta);
        }
        for (int y = Math.round(h * .22F); y < h * .70F; y += Math.max(10, h / 14)) {
            Lienzo.linea(g, torreX + w / 32F, y, torreX + w / 18F, y, 2, junta);
        }
        Lienzo.reflejo(g, torreX, aguaY, h, Math.max(8, w / 24), n.luz,
                .15F * luz, tiempo, 1170);
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        // Baranda de la plataforma desde la que se mira hacia abajo.
        Lienzo.linea(g, -20, h * .78F, w * .39F, h * .91F, Math.max(5, h / 30),
                Paleta.iluminar(n.junta, luz * .40F));
        for (int i = 0; i < 4; i++) {
            int x = Math.round(w * (-.02F + i * .13F));
            int y = Math.round(h * (.77F + i * .043F));
            Lienzo.linea(g, x, y, x, h, Math.max(3, h / 65),
                    Paleta.iluminar(n.junta, luz * .36F));
        }
    }
}
