package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Multijugador de Jobs. La lista, ping, LAN, MOTD y acciones siguen siendo las
 * de Minecraft; solo se cambia el marco y la superficie de interaccion.
 */
public final class PantallaMultijugadorJobs extends JoinMultiplayerScreen {

    private static final String SERVIDOR_IP = "JobsDosh.exaroton.me:56477";

    private Button realSelect, realDirect, realAdd, realEdit, realDelete, realRefresh, realCancel;
    private BotonExpediente select, edit, delete, refresh;
    private int panelX, panelY, panelW, panelH;

    public PantallaMultijugadorJobs(Screen anterior) {
        super(anterior);
    }

    @Override
    protected void init() {
        super.init();
        this.panelX = 12;
        this.panelY = 8;
        this.panelW = this.width - 24;
        this.panelH = this.height - 16;

        asegurarServidorOficial();

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
                // La lista empieza debajo de titulo, subtitulo y nota. En 0.14.0
                // esos tres textos compartian la misma franja y se montaban.
                int top = Math.max(78, panelY + 70);
                int bottom = Math.max(top + 40, this.height - 80);
                lista.updateSize(this.width, this.height, top, bottom);
            }
        }
        crearBotones();
    }

    /** Mantiene el acceso oficial guardado, traducido y en el primer renglon. */
    private void asegurarServidorOficial() {
        ServerList servidores = this.getServers();
        if (servidores == null) return;

        String nombre = Component.translatable("jobsmenu.servidor.oficial").getString();
        int indice = -1;
        for (int i = 0; i < servidores.size(); i++) {
            ServerData dato = servidores.get(i);
            if (SERVIDOR_IP.equalsIgnoreCase(dato.ip)) {
                dato.name = nombre;
                indice = i;
                break;
            }
        }
        if (indice < 0) {
            servidores.add(new ServerData(nombre, SERVIDOR_IP, false), false);
            indice = servidores.size() - 1;
        }
        while (indice > 0) {
            servidores.swap(indice, indice - 1);
            indice--;
        }
        servidores.save();
        if (this.serverSelectionList != null) {
            this.serverSelectionList.updateOnlineServers(servidores);
        }
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

        this.select = agregar(xTop, topY, topW, "selectServer.select", "jobsmenu.tooltip.servidor.entrar",
                BotonExpediente.Tipo.PRINCIPAL, this.realSelect);
        agregar(xTop + topW + gap, topY, topW, "selectServer.direct", "jobsmenu.tooltip.servidor.directa",
                BotonExpediente.Tipo.NORMAL, this.realDirect);
        agregar(xTop + (topW + gap) * 2, topY, topW, "selectServer.add", "jobsmenu.tooltip.servidor.agregar",
                BotonExpediente.Tipo.NORMAL, this.realAdd);

        this.edit = agregar(xBottom, bottomY, bottomW, "selectServer.edit", "jobsmenu.tooltip.servidor.editar",
                BotonExpediente.Tipo.NORMAL, this.realEdit);
        this.delete = agregar(xBottom + bottomW + gap, bottomY, bottomW, "selectServer.delete", "jobsmenu.tooltip.servidor.eliminar",
                BotonExpediente.Tipo.TERMINAL, this.realDelete);
        this.refresh = agregar(xBottom + (bottomW + gap) * 2, bottomY, bottomW, "selectServer.refresh", "jobsmenu.tooltip.servidor.actualizar",
                BotonExpediente.Tipo.NORMAL, this.realRefresh);
        agregar(xBottom + (bottomW + gap) * 3, bottomY, bottomW, "gui.cancel", "jobsmenu.tooltip.volver",
                BotonExpediente.Tipo.NORMAL, this.realCancel);
        sincronizarEstados();
    }

    private BotonExpediente agregar(int x, int y, int w, String clave, String ayuda,
                                     BotonExpediente.Tipo tipo, Button real) {
        BotonExpediente b = new BotonExpediente(x, y, w, 21,
                Component.translatable(clave), tipo, () -> pulsar(real));
        this.addRenderableWidget(b);
        b.setTooltip(Tooltip.create(Component.translatable(ayuda)));
        return b;
    }

    private static void pulsar(Button boton) {
        if (boton != null && boton.active) boton.onPress();
    }

    private void sincronizarEstados() {
        boolean oficial = servidorOficialSeleccionado();
        if (this.select != null) this.select.active = this.realSelect != null && this.realSelect.active;
        if (this.edit != null) this.edit.active = !oficial && this.realEdit != null && this.realEdit.active;
        if (this.delete != null) this.delete.active = !oficial && this.realDelete != null && this.realDelete.active;
        if (oficial) {
            Component ayuda = Component.translatable("jobsmenu.tooltip.servidor.protegido");
            if (this.edit != null) this.edit.setTooltip(Tooltip.create(ayuda));
            if (this.delete != null) this.delete.setTooltip(Tooltip.create(ayuda));
        } else {
            if (this.edit != null) this.edit.setTooltip(Tooltip.create(
                    Component.translatable("jobsmenu.tooltip.servidor.editar")));
            if (this.delete != null) this.delete.setTooltip(Tooltip.create(
                    Component.translatable("jobsmenu.tooltip.servidor.eliminar")));
        }
    }

    private boolean servidorOficialSeleccionado() {
        if (this.serverSelectionList == null) return false;
        ServerSelectionList.Entry entrada = this.serverSelectionList.getSelected();
        return entrada instanceof ServerSelectionList.OnlineServerEntry online
                && SERVIDOR_IP.equalsIgnoreCase(online.getServerData().ip);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panelArchivo(g, panelX, panelY, panelW, panelH);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        sincronizarEstados();
        super.render(g, mouseX, mouseY, partialTick);

        // JoinMultiplayerScreen dibuja su titulo despues del fondo. Cubrimos solo
        // la franja de cabecera, nunca la lista, y reconstruimos una unica jerarquia.
        int headerBottom = Math.min(this.height - 82, this.panelY + 70);
        g.fill(this.panelX + 6, this.panelY + 6,
                this.panelX + this.panelW - 6, headerBottom,
                Paleta.conAlfa(Paleta.VANO, 0.96F));
        ChromeExpediente.cabeceraArchivo(g, this.font,
                Component.translatable("jobsmenu.interfaz.multijugador.titulo"),
                Component.translatable("jobsmenu.interfaz.multijugador.subtitulo"),
                panelX, panelY, panelW);

        int tarjetaX = panelX + 20;
        int tarjetaY = panelY + 47;
        int tarjetaW = panelW - 40;
        g.fill(tarjetaX, tarjetaY, tarjetaX + tarjetaW, tarjetaY + 18,
                Paleta.conAlfa(Paleta.PARED, 0.12F));
        g.fill(tarjetaX, tarjetaY, tarjetaX + 3, tarjetaY + 18,
                Paleta.conAlfa(Paleta.PARED_ALTA, 0.72F));
        Component oficial = Component.translatable("jobsmenu.servidor.oficial");
        g.drawString(this.font, oficial, tarjetaX + 9, tarjetaY + 5,
                Paleta.conAlfa(Paleta.FLUOR, 0.90F), false);
        Component fijado = Component.translatable("jobsmenu.servidor.fijado");
        int fw = this.font.width(fijado);
        if (tarjetaW > 250) {
            g.drawString(this.font, fijado, tarjetaX + tarjetaW / 2 - fw / 2, tarjetaY + 5,
                    Paleta.conAlfa(Paleta.PARED_ALTA, 0.78F), false);
        }
        int ipW = this.font.width(SERVIDOR_IP);
        if (ipW < tarjetaW / 2) {
            g.drawString(this.font, SERVIDOR_IP, tarjetaX + tarjetaW - ipW - 8, tarjetaY + 5,
                    Paleta.conAlfa(Paleta.TECHO, 0.62F), false);
        }

        ChromeExpediente.pieArchivo(g, this.font, panelX, panelY, panelW, panelH, "ACCESS");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_F5 && this.realRefresh != null && this.realRefresh.active) {
            this.realRefresh.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
