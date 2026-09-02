package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Renglon principal del aviso con lectura de formulario y respuesta fisica. */
public class RenglonTablon extends AbstractButton {

    private static final int LADO_CASILLA = 8;
    private static final int SANGRIA_ORDEN = 15;
    private static final int SANGRIA_ETIQUETA = 34;
    private static final float DESPLAZAMIENTO = 2.0F;
    private static final float SUAVIZADO = 0.24F;

    private final String orden;
    private final Runnable accion;
    private final boolean terminal;
    private float foco;
    private float luzFrame = 1.0F;
    private boolean sonaba;
    private long presionadoHasta;
    private long confirmadoHasta;
    private Component etiquetaMedida = Component.empty();
    private int anchoEtiquetaMedida = -1;
    private int anchoWidgetMedido = -1;

    public RenglonTablon(int x, int y, int ancho, int alto, String orden,
                         Component etiqueta, Runnable accion, boolean terminal) {
        super(x, y, ancho, alto, etiqueta);
        this.orden = orden;
        this.accion = accion;
        this.terminal = terminal;
    }

    public void setLuzFrame(float luz) {
        this.luzFrame = Math.max(0.0F, Math.min(1.0F, luz));
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager gestor) {
    }

    @Override
    public void onPress() {
        long ahora = System.currentTimeMillis();
        this.presionadoHasta = ahora + 190L;
        this.confirmadoHasta = ahora + 310L;
        MezclaAudio.gesto(this.terminal ? SonidosNivel.UI_CONFIRMAR : SonidosNivel.UI_ELEGIR, 0.85F);
        if (this.accion != null) this.accion.run();
    }

    @Override
    public boolean mouseClicked(double ratonX, double ratonY, int boton) {
        if (!this.active && this.visible && boton == 0 && this.isMouseOver(ratonX, ratonY)) {
            MezclaAudio.gesto(SonidosNivel.UI_NEGADO, 0.70F);
            return true;
        }
        return super.mouseClicked(ratonX, ratonY, boton);
    }

    @Override
    public void renderWidget(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        Minecraft cliente = Minecraft.getInstance();
        long ahora = System.currentTimeMillis();
        float presion = 0.0F;
        if (this.presionadoHasta > 0L) {
            long restante = this.presionadoHasta - ahora;
            if (restante > 0L) presion = Math.min(1.0F, restante / 190.0F);
            else this.presionadoHasta = 0L;
        }
        float confirmacion = ahora < this.confirmadoHasta
                ? Math.max(0.0F, Math.min(1.0F, (this.confirmadoHasta - ahora) / 310.0F)) : 0.0F;

        boolean encima = this.isHoveredOrFocused() && this.active;
        boolean teclado = this.isFocused() && this.active && !this.isMouseOver(ratonX, ratonY);
        if (encima && !this.sonaba) MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.60F);
        this.sonaba = encima;

        float objetivo = encima ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo() || !ConfigTurno.escenaViva()) {
            this.foco = objetivo;
        } else {
            this.foco += (objetivo - this.foco) * SUAVIZADO;
            if (Math.abs(objetivo - this.foco) < 0.02F) this.foco = objetivo;
        }

        int x0 = this.getX();
        int x = x0 + (int) (DESPLAZAMIENTO * this.foco);
        int y = this.getY();
        int ancho = this.getWidth();
        int alto = this.getHeight();
        float escala = ConfigTurno.textoGrande() ? 1.15F : 1.0F;
        int altoTexto = Math.round(8.0F * escala);
        int lineaBase = y + Math.max(0, (alto - altoTexto) / 2);
        float tinta = (this.active ? 1.0F : 0.40F) * (0.10F + 0.90F * this.luzFrame);

        if (this.foco > 0.0F) {
            float fondo = ConfigTurno.guiaLectura() ? 0.135F : 0.055F;
            grafico.fill(x0, y, x0 + ancho, y + alto,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), fondo * this.foco * tinta));
            grafico.fill(x0 + 2, y + 2, x0 + ancho - 2, y + 3,
                    Paleta.conAlfa(Paleta.UI_PAPEL_FOCO, 0.11F * this.foco * tinta));
            grafico.fill(x0, y + 2, x0 + 2, y + alto - 2,
                    Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.tintaPrincipal(),
                            (teclado ? 0.72F : 0.58F) * this.foco * tinta));
            grafico.fill(x0 + 5, y + alto - 2,
                    x0 + Math.min(ancho - 4, 18 + Math.round((ancho - 24) * this.foco)), y + alto - 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.24F * this.foco * tinta));
            if (ancho > 120) {
                int cx = x0 + ancho / 2;
                grafico.fill(cx - 8, y, cx + 8, y + 1,
                        Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.10F * this.foco * tinta));
            }
        }

        float intensidadTerminal = Math.max(this.foco, presion);
        if (this.terminal) {
            int tenue = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.20F * tinta);
            grafico.fill(x0 + 2, y + 1, x0 + ancho - 2, y + 2, tenue);
            grafico.fill(x0 + 2, y + alto - 2, x0 + ancho - 2, y + alto - 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.09F * tinta));
            if (intensidadTerminal > 0.0F) {
                int borde = Paleta.conAlfa(Paleta.tintaPrincipal(),
                        (0.38F + 0.34F * intensidadTerminal) * tinta);
                grafico.fill(x0, y, x0 + ancho, y + 1, borde);
                grafico.fill(x0, y + alto - 1, x0 + ancho, y + alto, borde);
                grafico.fill(x0 + ancho - 3, y + 3, x0 + ancho - 2, y + alto - 3, borde);
                grafico.fill(x0 + 3, y + 3, x0 + 4, y + alto - 3,
                        Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.18F * tinta));
            }
        }

        if (presion > 0.0F) {
            int largoPresion = Math.max(1, Math.round((ancho - 4) * presion));
            grafico.fill(x0 + 2, y + alto - 3, x0 + 2 + largoPresion, y + alto - 2,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.74F * presion * tinta));
            grafico.fill(x0 + 3, y + 2, x0 + ancho - 3, y + alto - 3,
                    Paleta.conAlfa(Paleta.VANO, 0.05F * presion));
        }

        int casillaY = y + (alto - LADO_CASILLA) / 2;
        dibujarMarco(grafico, x, casillaY, LADO_CASILLA,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.72F * tinta));
        grafico.fill(x + 2, casillaY + 2, x + LADO_CASILLA - 2, casillaY + LADO_CASILLA - 2,
                Paleta.conAlfa(Paleta.UI_PAPEL, 0.08F * tinta));
        if (this.foco > 0.18F) {
            float crecida = Math.min(1.0F, (this.foco - 0.18F) / 0.58F);
            int margen = Math.max(1, Math.round(2.5F - crecida));
            grafico.fill(x + margen, casillaY + margen,
                    x + LADO_CASILLA - margen, casillaY + LADO_CASILLA - margen,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, (0.52F + 0.40F * crecida) * tinta));
            if (crecida > 0.52F) {
                grafico.fill(x + 2, casillaY + LADO_CASILLA / 2,
                        x + LADO_CASILLA - 2, casillaY + LADO_CASILLA / 2 + 1,
                        Paleta.conAlfa(Paleta.tintaPrincipal(), 0.50F * tinta));
            }
        }

        int ordenX = x + SANGRIA_ORDEN;
        int ordenW = Math.max(11, cliente.font.width(this.orden) + 6);
        if (ancho > 92) {
            grafico.fill(ordenX - 3, y + 3, ordenX - 3 + ordenW, y + alto - 3,
                    Paleta.conAlfa(Paleta.VANO, 0.035F + 0.035F * this.foco));
            grafico.fill(ordenX - 3, y + alto - 4, ordenX - 3 + ordenW, y + alto - 3,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.13F * tinta));
        }

        int colorOrden = Paleta.conAlfa(Paleta.tintaSecundaria(), (0.68F + 0.32F * this.foco) * tinta);
        int colorEtiqueta = Paleta.conAlfa(Paleta.mezclar(Paleta.tintaSecundaria(), Paleta.tintaPrincipal(), this.foco), tinta);
        dibujarTexto(grafico, cliente, Component.literal(this.orden), ordenX, lineaBase, colorOrden, escala);
        dibujarTexto(grafico, cliente, this.getMessage(), x + SANGRIA_ETIQUETA, lineaBase, colorEtiqueta, escala);
        puntosDeRelleno(grafico, cliente, x, ancho, lineaBase, tinta);

        if (this.foco > 0.50F && ancho > 100) {
            int marcador = x0 + ancho - 10;
            int cy = y + alto / 2;
            grafico.fill(marcador, cy - 2, marcador + 1, cy + 3,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.30F * tinta));
            grafico.fill(marcador + 1, cy - 1, marcador + 3, cy,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.40F * tinta));
            grafico.fill(marcador + 1, cy + 1, marcador + 3, cy + 2,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.24F * tinta));
        }

        if (teclado && ancho > 64) {
            int cy = y + alto / 2;
            grafico.fill(x0 - 3, cy - 2, x0 - 1, cy + 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.66F * tinta));
            grafico.fill(x0 + ancho + 1, cy - 1, x0 + ancho + 3, cy + 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.34F * tinta));
        }

        if (confirmacion > 0.0F && this.active) {
            int cx = x0 + ancho / 2;
            int span = Math.max(6, Math.round((ancho - 16) * (1.0F - confirmacion)));
            grafico.fill(Math.max(x0 + 8, cx - span / 2), y + alto - 2,
                    Math.min(x0 + ancho - 8, cx + span / 2), y + alto - 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.48F * confirmacion * tinta));
        }

        if (!this.active) {
            int cy = y + alto / 2;
            grafico.fill(x0 + 2, cy, x0 + 7, cy + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.26F * tinta));
            grafico.fill(x0 + ancho - 7, cy, x0 + ancho - 2, cy + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.16F * tinta));
        }
    }

    private void puntosDeRelleno(GuiGraphics grafico, Minecraft cliente,
                                 int x, int ancho, int lineaBase, float tinta) {
        int inicio = x + SANGRIA_ETIQUETA
                + Math.round(anchoEtiqueta(cliente) * (ConfigTurno.textoGrande() ? 1.15F : 1.0F)) + 5;
        int fin = this.getX() + ancho - 13;
        if (fin <= inicio) return;
        float largo = fin - inicio;
        for (int px = inicio; px < fin; px += 3) {
            float posicion = (px - inicio) / largo;
            float local = Math.max(0.0F, Math.min(1.0F, (this.foco - posicion * 0.55F) / 0.45F));
            grafico.fill(px, lineaBase + 6, px + 1, lineaBase + 7,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), (0.22F + 0.32F * local) * tinta));
        }
    }

    private int anchoEtiqueta(Minecraft cliente) {
        Component mensaje = this.getMessage();
        if (this.anchoWidgetMedido != this.getWidth() || !mensaje.equals(this.etiquetaMedida)) {
            this.anchoEtiquetaMedida = cliente.font.width(mensaje);
            this.etiquetaMedida = mensaje;
            this.anchoWidgetMedido = this.getWidth();
        }
        return this.anchoEtiquetaMedida;
    }

    private static void dibujarTexto(GuiGraphics grafico, Minecraft cliente, Component texto,
                                     int x, int y, int color, float escala) {
        if (escala == 1.0F) {
            grafico.drawString(cliente.font, texto, x, y, color, false);
            return;
        }
        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        grafico.pose().scale(escala, escala, 1.0F);
        grafico.drawString(cliente.font, texto, 0, 0, color, false);
        grafico.pose().popPose();
    }

    private static void dibujarMarco(GuiGraphics grafico, int x, int y, int lado, int color) {
        grafico.fill(x, y, x + lado, y + 1, color);
        grafico.fill(x, y + lado - 1, x + lado, y + lado, color);
        grafico.fill(x, y, x + 1, y + lado, color);
        grafico.fill(x + lado - 1, y, x + lado, y + lado, color);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput salida) {
        this.defaultButtonNarrationText(salida);
    }
}
