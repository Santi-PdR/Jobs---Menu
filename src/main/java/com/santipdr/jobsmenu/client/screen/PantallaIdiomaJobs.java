package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.ToggleExpediente;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/** Selector de idioma Jobs: compacto, responsive y sin chrome vanilla visible. */
public final class PantallaIdiomaJobs extends Screen {

    private static final int ITEM_H = 22;
    private final Screen anterior;
    private final Options opciones;
    private final LanguageManager idiomas;
    private ListaIdiomas lista;
    private ToggleExpediente unicode;
    private CampoBusquedaCentrado busqueda;
    private String aplicado;
    private String pendiente;
    private String filtroConservado = "";
    private boolean focoBusquedaConservado;
    private double scrollConservado;
    private int panelX, panelY, panelW, panelH;
    private boolean aplicando;
    private boolean falloAplicacion;

    public PantallaIdiomaJobs(Screen anterior, Options opciones, LanguageManager idiomas) {
        super(Component.translatable("jobsmenu.interfaz.idioma.titulo"));
        this.anterior = anterior;
        this.opciones = opciones;
        this.idiomas = idiomas;
    }

    @Override
    protected void init() {
        if (this.aplicado == null) {
            this.aplicado = this.idiomas.getSelected();
        }
        if (this.pendiente == null) {
            this.pendiente = this.aplicado;
        }

        this.panelW = Math.max(180, Math.min(430, this.width - 12));
        this.panelH = Math.max(190, Math.min(326, this.height - 12));
        this.panelW = Math.min(this.panelW, this.width - 4);
        this.panelH = Math.min(this.panelH, this.height - 4);
        this.panelX = Math.max(2, (this.width - panelW) / 2);
        this.panelY = Math.max(2, (this.height - panelH) / 2);

        int margen = panelW < 260 ? 10 : 20;
        int listX = panelX + margen;
        int listW = Math.max(90, panelW - margen * 2);
        int searchY = panelY + (panelH < 230 ? 42 : 49);
        this.busqueda = new CampoBusquedaCentrado(this.font, listX, searchY, listW, 19,
                Component.translatable("jobsmenu.interfaz.idioma.buscar"));
        this.busqueda.setResponder(s -> {
            this.filtroConservado = s;
            if (this.lista != null) this.lista.recargar(s);
        });
        this.addRenderableWidget(this.busqueda);

        int listY = searchY + 25;
        int footerY = panelY + panelH - 31;
        int listH = Math.max(62, footerY - listY - 10);
        this.lista = new ListaIdiomas(this.minecraft, listX, listY, listW, listH);
        this.addRenderableWidget(this.lista);

        if (!this.filtroConservado.isEmpty()) {
            this.busqueda.setValue(this.filtroConservado);
        } else {
            this.lista.recargar("");
        }
        this.lista.setScrollAmount(this.scrollConservado);
        if (this.focoBusquedaConservado) {
            this.setFocused(this.busqueda);
            this.busqueda.setFocused(true);
        }

        int gap = panelW < 260 ? 4 : 8;
        int util = Math.max(80, panelW - margen * 2);
        int bw = Math.max(38, (util - gap) / 2);
        int x0 = panelX + margen;
        this.unicode = this.addRenderableWidget(new ToggleExpediente(
                x0, footerY, bw, 22, Component.translatable("options.forceUnicodeFont"),
                () -> this.opciones.forceUnicodeFont().get(), v -> {
                    this.opciones.forceUnicodeFont().set(v);
                    this.opciones.save();
                }));
        this.addRenderableWidget(new BotonExpediente(
                x0 + bw + gap, footerY, bw, 22,
                Component.translatable("jobsmenu.interfaz.aplicar_cerrar"),
                BotonExpediente.Tipo.PRINCIPAL, this::aplicarYCerrar));
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        if (this.busqueda != null) {
            this.filtroConservado = this.busqueda.getValue();
            this.focoBusquedaConservado = this.busqueda.isFocused();
        }
        if (this.lista != null) {
            this.scrollConservado = this.lista.getScrollAmount();
        }
        super.resize(minecraft, width, height);
    }

    private void aplicarYCerrar() {
        if (this.aplicando) return;
        if (this.pendiente != null && !Objects.equals(this.pendiente, this.idiomas.getSelected())) {
            String idiomaAnterior = this.idiomas.getSelected();
            this.aplicando = true;
            this.falloAplicacion = false;
            this.opciones.languageCode = this.pendiente;
            this.idiomas.setSelected(this.pendiente);
            this.opciones.save();
            MezclaAudio.gesto(SonidosNivel.UI_CONFIRMAR, 0.52F);
            this.minecraft.reloadResourcePacks().whenComplete((ignorado, error) ->
                    this.minecraft.execute(() -> {
                        if (error == null) {
                            this.aplicado = this.pendiente;
                            this.aplicando = false;
                            this.minecraft.setScreen(this.anterior);
                            return;
                        }

                        // Rollback de estado: una recarga fallida no debe dejar
                        // Options/LanguageManager apuntando a un idioma que no
                        // llego a aplicarse en los recursos activos.
                        this.opciones.languageCode = idiomaAnterior;
                        this.idiomas.setSelected(idiomaAnterior);
                        this.opciones.save();
                        this.aplicado = idiomaAnterior;
                        this.aplicando = false;
                        this.falloAplicacion = true;
                        MezclaAudio.gesto(SonidosNivel.UI_NEGADO, 0.46F);
                    }));
            return;
        }
        this.opciones.save();
        this.minecraft.setScreen(this.anterior);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.cabecera(g, this.font, this.title,
                Component.translatable("jobsmenu.interfaz.idioma.subtitulo"), panelX, panelY, panelW);
        ChromeExpediente.esquinas(g, panelX, panelY, panelW, panelH);

        int margen = panelW < 260 ? 10 : 20;
        int railY = panelY + (panelH < 230 ? 31 : 36);
        int railX = panelX + margen;
        int railW = Math.max(20, panelW - margen * 2);
        g.fill(railX, railY, railX + railW, railY + 1,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.26F));
        if (panelW > 245 && this.pendiente != null) {
            String estado = Objects.equals(this.aplicado, this.pendiente)
                    ? this.pendiente.toUpperCase(java.util.Locale.ROOT)
                    : this.aplicado.toUpperCase(java.util.Locale.ROOT) + "  >  " + this.pendiente.toUpperCase(java.util.Locale.ROOT);
            int max = Math.max(20, railW - 4);
            estado = this.font.plainSubstrByWidth(estado, max);
            g.drawString(this.font, estado, railX + railW - this.font.width(estado), railY - 9,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.58F), false);
        }

        super.render(g, mouseX, mouseY, partialTick);

        if (this.falloAplicacion && !this.aplicando && this.panelW > 220) {
            String error = Component.translatable("jobsmenu.subtitulo.ui.negado").getString();
            if (this.pendiente != null) {
                error += " // " + this.pendiente.toUpperCase(java.util.Locale.ROOT);
            }
            error = ChromeExpediente.ajustar(this.font, error, Math.max(80, this.panelW - 50));
            int ew = this.font.width(error);
            int ex = this.panelX + (this.panelW - ew) / 2;
            int ey = this.panelY + this.panelH - 43;
            g.drawString(this.font, error, ex, ey,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.72F), false);
        }

        if (this.aplicando) {
            g.fill(0, 0, this.width, this.height, Paleta.conAlfa(Paleta.VANO, 0.62F));
            Component msg = Component.translatable("jobsmenu.interfaz.idioma.aplicando");
            int w = Math.min(this.width - 12, this.font.width(msg) + 34);
            int x = (this.width - w) / 2;
            int y = this.height / 2 - 17;
            g.fill(x - 2, y - 2, x + w + 2, y + 34,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.42F));
            g.fill(x, y, x + w, y + 30, Paleta.papelAviso());
            g.fill(x, y, x + 3, y + 30, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.82F));
            g.drawString(this.font, msg, x + 16, y + 11, Paleta.tintaPrincipal(), false);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.aplicando) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.aplicando) return true;
        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_F && this.busqueda != null) {
            this.setFocused(this.busqueda);
            this.busqueda.setFocused(true);
            this.focoBusquedaConservado = true;
            return true;
        }
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && this.pendiente != null) {
            aplicarYCerrar();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.busqueda != null && this.busqueda.isFocused()) {
            if (!this.busqueda.getValue().isEmpty()) {
                this.busqueda.setValue("");
                return true;
            }
            this.busqueda.setFocused(false);
            this.setFocused(null);
            this.focoBusquedaConservado = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public void onClose() {
        if (!this.aplicando) {
            this.opciones.save();
            this.minecraft.setScreen(this.anterior);
        }
    }
    @Override public void renderBackground(GuiGraphics g) { }

    private static final class CampoBusquedaCentrado extends EditBox {
        private final Font fuente;
        private final Component pista;
        CampoBusquedaCentrado(Font fuente, int x, int y, int ancho, int alto, Component pista) {
            super(fuente, x, y, ancho, alto, pista);
            this.fuente = fuente;
            this.pista = pista;
            this.setBordered(false);
            this.setHint(Component.empty());
        }
        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            int x = this.getX(), y = this.getY(), w = this.getWidth(), h = this.getHeight();
            int borde = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, this.isFocused() ? 0.78F : 0.34F);
            g.fill(x - 2, y - 2, x + w + 2, y + h + 2, Paleta.conAlfa(Paleta.UI_ACENTO, this.isFocused() ? 0.20F : 0.07F));
            g.fill(x - 1, y - 1, x + w + 1, y + h + 1, borde);
            g.fill(x, y, x + w, y + h,
                    Paleta.mezclar(Paleta.papelAviso(), Paleta.UI_PAPEL_FOCO, this.isFocused() ? 0.68F : 0.20F));
            boolean vacio = this.getValue().isEmpty();
            String texto = vacio ? this.pista.getString() : this.getValue();
            int max = Math.max(8, w - 18);
            if (this.fuente.width(texto) > max) texto = this.fuente.plainSubstrByWidth(texto, max);
            int tw = this.fuente.width(texto);
            int tx = x + Math.max(8, (w - tw) / 2);
            int ty = y + (h - this.fuente.lineHeight) / 2;
            g.drawString(this.fuente, texto, tx, ty,
                    vacio ? Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.68F) : Paleta.UI_TINTA_TENUE, false);
            if (!vacio && this.isFocused() && (System.currentTimeMillis() / 500L) % 2L == 0L) {
                int cx = Math.min(x + w - 7, tx + tw + 1);
                g.fill(cx, ty - 1, cx + 1, ty + this.fuente.lineHeight + 1,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.88F));
            }
        }
    }

    private final class ListaIdiomas extends ObjectSelectionList<EntradaIdioma> {
        private final int rowW;
        ListaIdiomas(Minecraft minecraft, int left, int top, int width, int height) {
            super(minecraft, width, height, top, top + height, ITEM_H);
            this.rowW = width - 10;
            this.setLeftPos(left);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            this.setRenderSelection(false);
            recargar("");
        }
        void recargar(String filtro) {
            this.clearEntries();
            String aguja = filtro == null ? "" : filtro.trim().toLowerCase(java.util.Locale.ROOT);
            EntradaIdioma seleccionada = null;
            for (var e : PantallaIdiomaJobs.this.idiomas.getLanguages().entrySet()) {
                String nombre = e.getValue().toComponent().getString().toLowerCase(java.util.Locale.ROOT);
                String codigo = e.getKey().toLowerCase(java.util.Locale.ROOT);
                if (!aguja.isEmpty() && !nombre.contains(aguja) && !codigo.contains(aguja)) continue;
                EntradaIdioma entrada = new EntradaIdioma(e.getKey(), e.getValue());
                this.addEntry(entrada);
                if (Objects.equals(e.getKey(), PantallaIdiomaJobs.this.pendiente)) seleccionada = entrada;
            }
            if (seleccionada != null) {
                this.setSelected(seleccionada);
                this.centerScrollOn(seleccionada);
            } else this.setScrollAmount(0.0D);
        }
        @Override public int getRowWidth() { return this.rowW; }
        @Override protected int getScrollbarPosition() { return this.getRowLeft() + this.getRowWidth() + 4; }
    }

    private final class EntradaIdioma extends ObjectSelectionList.Entry<EntradaIdioma> {
        private final String codigo;
        private final LanguageInfo info;
        private long ultimoClick;
        EntradaIdioma(String codigo, LanguageInfo info) { this.codigo = codigo; this.info = info; }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int rowWidth, int rowHeight,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            boolean pending = Objects.equals(PantallaIdiomaJobs.this.pendiente, this.codigo);
            boolean active = Objects.equals(PantallaIdiomaJobs.this.aplicado, this.codigo);
            int x0 = left + 2, x1 = left + rowWidth - 2;
            if (pending) {
                g.fill(x0, top + 1, x1, top + rowHeight - 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.22F));
                g.fill(x0, top + 1, x0 + 3, top + rowHeight - 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.82F));
            } else if (hovered) {
                g.fill(x0, top + 1, x1, top + rowHeight - 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.09F));
                g.fill(x0, top + 3, x0 + 2, top + rowHeight - 4,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.38F));
            }
            if (active) {
                int cy = top + rowHeight / 2;
                g.fill(left + 8, cy - 2, left + 12, cy + 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.86F));
            }
            String nombre = this.info.toComponent().getString();
            String codigoTxt = this.codigo.toUpperCase(java.util.Locale.ROOT);
            int cw = PantallaIdiomaJobs.this.font.width(codigoTxt);
            int maxNombre = Math.max(24, rowWidth - cw - 42);
            if (PantallaIdiomaJobs.this.font.width(nombre) > maxNombre) {
                nombre = PantallaIdiomaJobs.this.font.plainSubstrByWidth(nombre, Math.max(8, maxNombre - 8)) + "...";
            }
            int tx = left + (active ? 17 : 9);
            int ty = top + (rowHeight - PantallaIdiomaJobs.this.font.lineHeight) / 2;
            g.drawString(PantallaIdiomaJobs.this.font, nombre, tx, ty,
                    pending ? Paleta.tintaPrincipal() : Paleta.tintaSecundaria(), false);
            if (rowWidth > 150) {
                int badgeX = left + rowWidth - cw - 12;
                g.fill(badgeX - 4, ty - 2, badgeX + cw + 4, ty + PantallaIdiomaJobs.this.font.lineHeight + 2,
                        Paleta.conAlfa(Paleta.VANO, pending ? 0.13F : 0.07F));
                g.drawString(PantallaIdiomaJobs.this.font, codigoTxt, badgeX, ty,
                        Paleta.conAlfa(pending ? Paleta.UI_ACENTO_FUERTE : Paleta.tintaSecundaria(), pending ? 0.82F : 0.48F), false);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;
            long ahora = System.currentTimeMillis();
            boolean doble = Objects.equals(PantallaIdiomaJobs.this.pendiente, this.codigo)
                    && ahora - this.ultimoClick < 420L;
            this.ultimoClick = ahora;
            PantallaIdiomaJobs.this.pendiente = this.codigo;
            PantallaIdiomaJobs.this.falloAplicacion = false;
            PantallaIdiomaJobs.this.lista.setSelected(this);
            MezclaAudio.gesto(SonidosNivel.UI_ELEGIR, 0.36F);
            if (doble) PantallaIdiomaJobs.this.aplicarYCerrar();
            return true;
        }
        @Override public Component getNarration() { return this.info.toComponent(); }
    }
}
