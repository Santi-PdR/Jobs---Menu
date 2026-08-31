package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;

/**
 * Multijugador de Jobs. La lista, ping, LAN, MOTD y acciones siguen siendo las
 * de Minecraft; solo se cambia el marco y la superficie de interaccion.
 */
public final class PantallaMultijugadorJobs extends JoinMultiplayerScreen {

    private Button realSelect, realDirect, realAdd, realEdit, realDelete, realRefresh, realCancel;
    private BotonExpediente select, edit, delete;
    private int panelX, panelY, panelW, panelH;

    public PantallaMultijugadorJobs(Screen anterior) {
        super(anterior);
    }

    @Override
    protected void init() {
        super.init();
        this.panelX = 8;
        this.panelY = 6;
        this.panelW = this.width - 16;
        this.panelH = this.height - 12;

        this.realSelect = buscar("selectServer.select");
        this.realDirect = buscar("selectServer.direct");
        this.realAdd = buscar("selectServer.add");
        this.realEdit = buscar("selectServer.edit");
        this.realDelete = buscar("selectServer.delete");
        this.realRefresh = buscar("selectServer.refresh");
        this.realCancel = buscar("gui.cancel");

        for (var child : this.children()) {
            if (child instanceof Button b) b.visible = false;
            if (child instanceof ServerSelectionList lista) {
                lista.setRenderBackground(false);
                lista.setRenderTopAndBottom(false);
                int top = Math.max(42, panelY + 48);
                int bottom = Math.max(top + 40, this.height - 78);
                lista.updateSize(this.width, this.height, top, bottom);
            }
        }
        crearBotones();
    }

    private Button buscar(String clave) {
        String esperado = Component.translatable(clave).getString();
        for (var child : this.children()) {
            if (child instanceof Button b && b.getMessage().getString().equals(esperado)) return b;
        }
        return null;
    }

    private void crearBotones() {
        int gap = 5;
        int margen = 18;
        int util = Math.max(240, this.panelW - margen * 2);
        int topW = Math.max(72, (util - gap * 2) / 3);
        int bottomW = Math.max(60, (util - gap * 3) / 4);
        int topY = this.height - 68;
        int bottomY = this.height - 42;
        int xTop = this.panelX + margen;
        int xBottom = this.panelX + margen;

        this.select = agregar(xTop, topY, topW, "selectServer.select",
                BotonExpediente.Tipo.PRINCIPAL, this.realSelect);
        agregar(xTop + topW + gap, topY, topW, "selectServer.direct",
                BotonExpediente.Tipo.NORMAL, this.realDirect);
        agregar(xTop + (topW + gap) * 2, topY, topW, "selectServer.add",
                BotonExpediente.Tipo.NORMAL, this.realAdd);

        this.edit = agregar(xBottom, bottomY, bottomW, "selectServer.edit",
                BotonExpediente.Tipo.NORMAL, this.realEdit);
        this.delete = agregar(xBottom + bottomW + gap, bottomY, bottomW, "selectServer.delete",
                BotonExpediente.Tipo.TERMINAL, this.realDelete);
        agregar(xBottom + (bottomW + gap) * 2, bottomY, bottomW, "selectServer.refresh",
                BotonExpediente.Tipo.NORMAL, this.realRefresh);
        agregar(xBottom + (bottomW + gap) * 3, bottomY, bottomW, "gui.cancel",
                BotonExpediente.Tipo.NORMAL, this.realCancel);
        sincronizarEstados();
    }

    private BotonExpediente agregar(int x, int y, int w, String clave,
                                     BotonExpediente.Tipo tipo, Button real) {
        BotonExpediente b = new BotonExpediente(x, y, w, 21,
                Component.translatable(clave), tipo, () -> pulsar(real));
        this.addRenderableWidget(b);
        return b;
    }

    private static void pulsar(Button boton) {
        if (boton != null && boton.active) boton.onPress();
    }

    private void sincronizarEstados() {
        if (this.select != null) this.select.active = this.realSelect != null && this.realSelect.active;
        if (this.edit != null) this.edit.active = this.realEdit != null && this.realEdit.active;
        if (this.delete != null) this.delete.active = this.realDelete != null && this.realDelete.active;
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, panelX, panelY, panelW, panelH);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        sincronizarEstados();
        super.render(g, mouseX, mouseY, partialTick);
        ChromeExpediente.marcoSubpantalla(g, this.font, this.width, this.height,
                panelX, panelY, panelW, panelH,
                Component.translatable("jobsmenu.interfaz.multijugador.subtitulo"), "CREW-012");
        Component nota = Component.translatable("jobsmenu.interfaz.multijugador.nota");
        int nw = this.font.width(nota);
        if (nw < this.width - 40) {
            g.drawString(this.font, nota, this.width / 2 - nw / 2, 31,
                    com.santipdr.jobsmenu.client.ui.Paleta.conAlfa(
                            com.santipdr.jobsmenu.client.ui.Paleta.tintaSecundaria(), 0.62F), false);
        }
    }
}
