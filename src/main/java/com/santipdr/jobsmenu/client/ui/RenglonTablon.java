package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Renglon principal del aviso: formulario fisico con respuesta visible y contenida. */
public class RenglonTablon extends AbstractButton {

    private static final int LADO_CASILLA = 8;
    private static final int SANGRIA_ORDEN = 15;
    private static final int SANGRIA_ETIQUETA = 34;
    private static final float DESPLAZAMIENTO = 2.0F;
    private static final float SUAVIZADO = 0.25F;

    private final String orden;
    private final Runnable accion;
    private final boolean terminal;
    private float foco;
    private float luzFrame = 1.0F;
    private boolean sonaba;
    private long presionadoHasta;
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
        this.presionadoHasta = System.currentTimeMillis() + 190L;
        MezclaAudio.gesto(this.terminal ? SonidosNivel.UI_CONFIRMAR : SonidosNivel.UI_ELEGIR, 0.85F);
        this.accion.run();
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
        float presion = 0.0F;
        if (this.presionadoHasta > 0L) {
            long restante = this.presionadoHasta - System.currentTimeMillis();
            if (restante > 0L) presion = Math.min(1.0F, restante / 190.0F);
            else this.presionadoHasta = 0L;
        }

        boolean encima = this.isHoveredOrFocused() && this.active;
        if (encima && !this.sonaba) MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.60F);
        this.sonaba = encima;

        float objetivo = encima ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo() || !ConfigTurno.escenaViva()) this.foco = objetivo;
        else {
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
            if (ConfigTurno.guiaLectura()) {
                grafico.fill(x0, y, x0 + ancho, y + alto,
                        Paleta.conAlfa(Paleta.tintaSecundaria(), 0.13F * this.foco * tinta));
                grafico.fill(x0 + 2, y + 2, x0 + ancho - 2, y + 3,
                        Paleta.conAlfa(Paleta.UI_PAPEL_FOCO, 0.10F * this.foco * tinta));
            }
            grafico.fill(x0, y + 2, x0 + 2, y + alto - 2,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.58F * this.foco * tinta));
            grafico.fill(x0 + 5, y + alto - 2,
                    x0 + Math.min(ancho - 4, 18 + Math.round((ancho - 24) * this.foco)), y + alto - 1,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.20F * this.foco * tinta));
        }

        float intensidadTerminal = Math.max(this.foco, presion);
        if (this.terminal) {
            int tenue = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.20F * tinta);
            grafico.fill(x0 + 2, y + 1, x0 + ancho - 2, y + 2, tenue);
            if (intensidadTerminal > 0.0F) {
                int borde = Paleta.conAlfa(Paleta.tintaPrincipal(),
                        (0.38F + 0.34F * intensidadTerminal) * tinta);
                grafico.fill(x0, y, x0 + ancho, y + 1, borde);
                grafico.fill(x0, y + alto - 1, x0 + ancho, y + alto, borde);
                grafico.fill(x0 + ancho - 3, y + 3, x0 + ancho - 2, y + alto - 3, borde);
            }
        }
        if (presion > 0.0F) {
            int largoPresion = Math.max(1, Math.round((ancho - 4) * presion));
            grafico.fill(x0 + 2, y + alto - 3, x0 + 2 + largoPresion, y + alto - 2,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.74F * presion * tinta));
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
                    Paleta.conAlfa(Paleta.tintaPrincipal(), (0.58F + 0.42F * crecida) * tinta));
        }

        int colorOrden = Paleta.conAlfa(Paleta.tintaSecundaria(), (0.68F + 0.32F * this.foco) * tinta);
        int colorEtiqueta = Paleta.conAlfa(Paleta.mezclar(Paleta.tintaSecundaria(), Paleta.tintaPrincipal(), this.foco), tinta);
        dibujarTexto(grafico, cliente, Component.literal(this.orden), x + SANGRIA_ORDEN, lineaBase, colorOrden, escala);
        dibujarTexto(grafico, cliente, this.getMessage(), x + SANGRIA_ETIQUETA, lineaBase, colorEtiqueta, escala);
        puntosDeRelleno(grafico, cliente, x, ancho, lineaBase, tinta);

        if (this.foco > 0.55F && ancho > 100) {
            int marcador = x0 + ancho - 8;
            grafico.fill(marcador, y + alto / 2 - 1, marcador + 3, y + alto / 2,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.34F * tinta));
            grafico.fill(marcador + 2, y + alto / 2, marcador + 4, y + alto / 2 + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.22F * tinta));
        }
    }

    private void puntosDeRelleno(GuiGraphics grafico, Minecraft cliente,
                                 int x, int ancho, int lineaBase, float tinta) {
        int inicio = x + SANGRIA_ETIQUETA
                + Math.round(anchoEtiqueta(cliente) * (ConfigTurno.textoGrande() ? 1.15F : 1.0F)) + 5;
        int fin = this.getX() + ancho - 10;
        if (fin <= inicio) return;
        float largo = fin - inicio;
        for (int px = inicio; px < fin; px += 3) {
            float posicion = (px - inicio) / largo;
            float local = Math.max(0.0F, Math.min(1.0F, (this.foco - posicion * 0.55F) / 0.45F));
            grafico.fill(px, lineaBase + 6, px + 1, lineaBase + 7,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), (0.24F + 0.30F * local) * tinta));
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
