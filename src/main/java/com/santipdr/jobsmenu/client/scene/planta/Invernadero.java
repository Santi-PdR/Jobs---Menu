package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 6 - El invernadero.
 *
 * Una nave de vidrio y hierro tomada por las plantas. La luz no viene de tubos
 * ni de fuego: entra por el techo, blanca y difusa, filtrada por vidrios sucios
 * y por las hojas. Es el unico recinto iluminado desde ARRIBA y por luz natural,
 * y por eso el mas ambiguo: no se sabe si es de dia o si esa claridad viene de
 * otra cosa.
 *
 * Lo que lo distingue: el TECHO ES LA FUENTE. Una cristalera a dos aguas con su
 * entramado de hierro deja caer haces de luz polvorienta. Abajo, bancos de
 * cultivo desbordados y una humedad verde que lo cubre todo. La vegetacion se
 * mete desde los bordes hacia el centro; el pasillo del medio es lo unico que
 * sigue despejado.
 */
public final class Invernadero implements Planta {

    private static final int TRAMOS = 14;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    @Override
    public float pisoPresencia() {
        return 0.96F;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.paredBaja, nivel.techo, 0.55F), 1.9F);
        portonFondo(grafico, m, nivel, luz);

        // La cristalera del techo: mas clara que cualquier otra superficie,
        // porque es de donde viene la luz.
        cristalera(grafico, m, nivel, luz);
        Trazo.transversales(grafico, m, true, nivel.techoJunta, nivel.niebla, luz, TRAMOS, 0.34F);

        Trazo.plano(grafico, m, false, nivel.suelo, nivel.sueloLejos, nivel.niebla, luz, 0.52F);
        Trazo.transversales(grafico, m, false, nivel.sueloJunta, nivel.niebla, luz, TRAMOS, 0.40F);
        senderoCentral(grafico, m, nivel, luz);

        Trazo.paredes(grafico, m, nivel, luz);
        Trazo.juntasVerticales(grafico, m, nivel, luz, TRAMOS, 1.0F, 0.28F);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);

        bancos(grafico, m, nivel, luz);
        vegetacion(grafico, m, nivel, luz, tiempo);
        haces(grafico, m, nivel, luz, tiempo);
        vahoSuperficie(grafico, m, nivel, luz, tiempo);
    }

    /** Un porton de vidrio esmerilado al fondo: la luz sigue del otro lado. */
    private static void portonFondo(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float suelo = m.sueloEn(1.0F);
        float alto = m.h() * 1.30F;
        int x0 = Math.round(m.izq(0.40F));
        int x1 = Math.round(m.der(0.40F));
        int y0 = Math.round(suelo - alto);
        int y1 = Math.round(suelo);
        grafico.fillGradient(x0, y0, x1, y1,
                Paleta.iluminar(Paleta.mezclar(nivel.techo, 0xFFFFFFFF, 0.20F), luz * 0.90F),
                Paleta.iluminar(nivel.paredBaja, luz * 0.55F));
        // Cuarterones del porton.
        int marco = Paleta.iluminar(nivel.junta, luz * 0.6F);
        for (int k = 1; k < 3; k++) {
            grafico.fill(x0 + (x1 - x0) * k / 3, y0, x0 + (x1 - x0) * k / 3 + 1, y1, marco);
        }
        for (int k = 1; k < 4; k++) {
            grafico.fill(x0, y0 + (y1 - y0) * k / 4, x1, y0 + (y1 - y0) * k / 4 + 1, marco);
        }
    }

    /**
     * La cristalera a dos aguas: el techo de vidrio con su entramado de hierro.
     *
     * En vez del plano de placas comun, cada fila del techo se pinta mas clara
     * cuanto mas cerca de la cumbrera (el eje), donde el vidrio da al cielo. Las
     * barras del entramado corren hacia la fuga y se cruzan con las cabias.
     */
    private static void cristalera(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        int hasta = Math.round(m.techoEn(1.0F));
        for (int y = 0; y < hasta; y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            if (dy <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            // Cuanto mas alto en pantalla (mas cerca de la cumbrera), mas claro:
            // ahi el vidrio mira al cielo. El borde inferior es sombra de hierro.
            float haciaLaCumbre = 1.0F - Trazo.limitar(y / (float) Math.max(1, hasta), 0.0F, 1.0F);
            int vidrio = Paleta.mezclar(nivel.techo, 0xFFFFFFFF, 0.10F + 0.35F * haciaLaCumbre);
            vidrio = Trazo.velar(vidrio, nivel.niebla, lej, 0.30F);
            grafico.fill(0, y, m.ancho(), y + Trazo.PASO,
                    Paleta.iluminar(vidrio, Trazo.atenuar(luz, lej) * (0.7F + 0.3F * haciaLaCumbre)));
        }
        // La cumbrera: la viga central mas alta, en el eje, en sombra contra el
        // vidrio brillante.
        for (int j = 1; j <= TRAMOS; j++) {
            float dx = Trazo.profundidad(j, TRAMOS);
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float y = m.techoEn(dx * 0.10F);
            int x = Math.round(m.centro(dx));
            int grosor = Math.max(1, (int) (m.h() * dx * 0.012F));
            grafico.fill(x - grosor, (int) y, x + grosor, (int) y + grosor,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, Trazo.atenuar(luz, lej)), 0.55F));
        }
    }

    /** El sendero central de gravilla, despejado entre la vegetacion. */
    private static void senderoCentral(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int y = Math.round(m.sueloEn(1.0F)); y < m.alto(); y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            if (dy <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            float medio = m.w() * dy * 0.22F;
            int color = Paleta.mezclar(nivel.suelo, nivel.techo, 0.20F);
            grafico.fill((int) (m.centro(dy) - medio), y, (int) (m.centro(dy) + medio), y + Trazo.PASO,
                    Paleta.conAlfa(Paleta.iluminar(color, Trazo.atenuar(luz, lej)), 0.30F));
        }
    }

    /** Los bancos de cultivo: mesones bajos a los dos lados del sendero. */
    private static void bancos(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 2; j <= TRAMOS; j += 2) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 6.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            for (int signo = -1; signo <= 1; signo += 2) {
                float x = m.lado(signo, dx * 0.62F);
                if (x < -m.w() || x > m.ancho() + m.w()) {
                    continue;
                }
                float ancho = Math.max(3.0F, m.w() * dx * 0.20F);
                float y = m.sueloEn(dx * 0.72F);
                float alto = m.h() * dx * 0.05F;
                int mesa = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.45F), at * 0.7F);
                grafico.fill((int) (x - ancho * 0.5F), (int) y, (int) (x + ancho * 0.5F), (int) (y + alto), mesa);
                // La tierra en el banco, mas oscura.
                grafico.fill((int) (x - ancho * 0.5F), (int) (y - alto * 0.4F), (int) (x + ancho * 0.5F), (int) y,
                        Paleta.iluminar(Trazo.velar(0xFF2C2415, nivel.niebla, lej, 0.4F), at * 0.6F));
            }
        }
    }

    /**
     * La vegetacion desbordada: matas que suben desde los bancos y cuelgan del
     * borde, deterministas, en varios verdes. Se mete desde los laterales hacia
     * el centro; nunca invade el sendero.
     */
    private static void vegetacion(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        int total = (int) (46);
        for (int i = 0; i < total; i++) {
            float dx = 1.15F + Trazo.pseudo(i * 5) * (TRAMOS * 0.42F);
            if (dx > 7.0F) {
                continue;
            }
            int signo = Trazo.pseudo(i * 5 + 1) < 0.5F ? -1 : 1;
            float frac = 0.44F + Trazo.pseudo(i * 5 + 2) * 0.55F;
            float x = m.lado(signo, dx * frac);
            if (x < -20 || x > m.ancho() + 20) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float base = m.sueloEn(dx * 0.72F);
            float altura = m.h() * dx * (0.10F + Trazo.pseudo(i * 5 + 3) * 0.30F);
            float anchoMata = Math.max(2.0F, m.w() * dx * (0.03F + Trazo.pseudo(i * 5 + 4) * 0.06F));
            // Verde propio de la mata: del claro al oscuro segun ruido.
            int verde = Paleta.mezclar(0xFF3E5A28, 0xFF6E8A3A, Trazo.pseudo(i * 7));
            verde = Trazo.velar(verde, nivel.niebla, lej, 0.4F);
            // Se dibuja como una pila de manojos que se afinan hacia arriba, con
            // un vaiven lentisimo (la planta respira con la corriente).
            float vaiven = (float) Math.sin(tiempo * 0.4F + i) * anchoMata * 0.15F;
            int hojas = 6;
            for (int k = 0; k < hojas; k++) {
                float f = k / (float) hojas;
                float w = anchoMata * (1.0F - f * 0.6F);
                float yy = base - altura * f;
                float ox = vaiven * f;
                grafico.fill((int) (x - w + ox), (int) (yy - altura / hojas), (int) (x + w + ox), (int) yy,
                        Paleta.conAlfa(Paleta.iluminar(verde, at * (0.7F + 0.3F * f)), 0.9F));
            }
        }
    }

    /**
     * Los haces de luz que bajan del vidrio: columnas claras y polvorientas,
     * inclinadas, que es lo que dice que la luz entra por arriba. Se mueven
     * lentisimo, como el sol que no deberia estar ahi.
     */
    private static void haces(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int i = 0; i < 5; i++) {
            float fase = tiempo * 0.03F + i * 0.7F;
            float frac = (float) Math.sin(fase) * 0.7F;
            float dxTop = 2.0F + i * 1.6F;
            float xTop = m.enX(dxTop, frac);
            float yTop = m.techoEn(dxTop * 0.3F);
            float xBot = m.enX(dxTop * 1.4F, frac * 0.7F);
            float yBot = m.sueloEn(dxTop * 0.9F);
            float lej = Trazo.limitar(1.0F / dxTop, 0.0F, 1.0F);
            float a = 0.05F * luz * (0.5F + 0.5F * lej);
            int pasos = 14;
            float ancho = Math.max(3.0F, m.w() * dxTop * 0.05F);
            for (int k = 0; k < pasos; k++) {
                float t = k / (float) pasos;
                float x = xTop + (xBot - xTop) * t;
                float y = yTop + (yBot - yTop) * t;
                grafico.fill((int) (x - ancho * (1.0F + t)), (int) y,
                        (int) (x + ancho * (1.0F + t)), (int) y + Trazo.PASO * 2,
                        Paleta.conAlfa(Paleta.iluminar(0xFFFFFFF0, luz), a * (1.0F - t * 0.5F)));
            }
        }
    }

    /** Vaho verde sobre el suelo: la humedad del invernadero, en jirones lentos. */
    private static void vahoSuperficie(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        int desde = Math.round(m.sueloEn(1.0F));
        for (int y = desde; y < m.alto(); y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            if (dy <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            float humedad = (1.0F - lej) * 0.10F * luz;
            if (humedad <= 0.005F) {
                continue;
            }
            int niebla = Paleta.mezclar(nivel.niebla, 0xFF6E8A3A, 0.30F);
            int paso = Math.max(Trazo.PASO * 8, m.ancho() / 9);
            for (int jx = 0; jx < m.ancho(); jx += paso) {
                float onda = (float) Math.sin(tiempo * 0.14F + jx * 0.012F + dy * 0.5F);
                float a = humedad * Trazo.limitar(0.5F + 0.5F * onda, 0.0F, 1.0F);
                if (a <= 0.005F) {
                    continue;
                }
                grafico.fill(jx, y, Math.min(m.ancho(), jx + paso), y + Trazo.PASO,
                        Paleta.conAlfa(niebla, a));
            }
        }
    }

    @Override
    public void primerPlano(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        PrimerPlano.invernadero(grafico, m, nivel, luz, tiempo);
    }
}

