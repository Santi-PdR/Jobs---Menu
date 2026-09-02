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

/** Selector de idioma propio: misma configuracion vanilla, lectura de archivo Jobs. */
public final class PantallaIdiomaJobs extends Screen {

    private static final int ITEM_H = 20;
    private final Screen anterior;
    private final Options opciones;
    private final LanguageManager idiomas;
    private ListaIdiomas lista;
    private ToggleExpediente unicode;
    private CampoBusquedaCentrado busqueda;
    private String aplicado;
    private String pendiente;
    private int panelX, panelY, panelW, panelH;
    private boolean aplicando;

    public PantallaIdiomaJobs(Screen anterior, Options opciones, LanguageManager idiomas) {
        super(Component.translatable("jobsmenu.interfaz.idioma.titulo"));
        this.anterior = anterior;
        this.opciones = opciones;
        this.idiomas = idiomas;
    }

    @Override
    protected void init() {
        this.aplicado = this.idiomas.getSelected();
        this.pendiente = this.aplicado;
        this.panelW = Math.min(410, Math.max(260, this.width - 24));
        this.panelH = Math.min(310, Math.max(230, this.height - 20));
        this.panelX = (this.width - panelW) / 2;
        this.panelY = Math.max(6, (this.height - panelH) / 2);

        int listX = panelX + 20;
        int listW = panelW - 40;
        int searchY = panelY + 48;
        this.busqueda = new CampoBusquedaCentrado(this.font, listX, searchY, listW, 18,
                Component.translatable("jobsmenu.interfaz.idioma.buscar"));
        this.busqueda.setResponder(s -> {
            if (this.lista != null) this.lista.recargar(s);
        });
        this.addRenderableWidget(this.busqueda);

        int listY = panelY + 70;
        int footerY = panelY + panelH - 31;
        int listH = Math.max(70, footerY - listY - 12);
        this.lista = new ListaIdiomas(this.minecraft, listX, listY, listW, listH);
        this.addRenderableWidget(this.lista);

        int gap = 8;
        int bw = Math.max(100, (panelW - 48 - gap) / 2);
        int x0 = panelX + 20;
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

    private void aplicarYCerrar() {
        if (this.aplicando) return;
        if (this.pendiente != null && !Objects.equals(this.pendiente, this.idiomas.getSelected())) {
            this.aplicando = true;
            this.opciones.languageCode = this.pendiente;
            this.idiomas.setSelected(this.pendiente);
            this.opciones.save();
            MezclaAudio.gesto(SonidosNivel.UI_CONFIRMAR, 0.52F);
            this.minecraft.reloadResourcePacks().thenRun(() ->
                    this.minecraft.execute(() -> this.minecraft.setScreen(this.anterior)));
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
        super.render(g, mouseX, mouseY, partialTick);

        if (this.aplicando) {
            g.fill(0, 0, this.width, this.height, Paleta.conAlfa(Paleta.VANO, 0.56F));
            Component msg = Component.translatable("jobsmenu.interfaz.idioma.aplicando");
            int w = this.font.width(msg) + 28;
            int x = (this.width - w) / 2;
            int y = this.height / 2 - 15;
            g.fill(x, y, x + w, y + 30, Paleta.papelAviso());
            g.drawString(this.font, msg, x + 14, y + 11, Paleta.tintaPrincipal(), false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.aplicando) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_F && this.busqueda != null) {
            this.setFocused(this.busqueda);
            this.busqueda.setFocused(true);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.busqueda != null
                && this.busqueda.isFocused() && !this.busqueda.getValue().isEmpty()) {
            this.busqueda.setValue("");
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (!this.aplicando) {
            this.opciones.save();
            this.minecraft.setScreen(this.anterior);
        }
    }

    @Override
    public void renderBackground(GuiGraphics g) {
    }

    /**
     * Campo de filtro sin sombra ni texto duplicado. EditBox conserva entrada,
     * foco, portapapeles y narracion; solo reemplazamos su dibujo para centrarlo.
     */
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
            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();
            int borde = Paleta.conAlfa(Paleta.tintaSecundaria(), this.isFocused() ? 0.68F : 0.38F);
            g.fill(x - 1, y - 1, x + w + 1, y + h + 1, borde);
            g.fill(x, y, x + w, y + h,
                    Paleta.mezclar(Paleta.papelAviso(), Paleta.PARED_ALTA, 0.035F));

            boolean vacio = this.getValue().isEmpty();
            String texto = vacio ? this.pista.getString() : this.getValue();
            int max = Math.max(8, w - 18);
            if (this.fuente.width(texto) > max) {
                texto = this.fuente.plainSubstrByWidth(texto, max);
            }
            int tw = this.fuente.width(texto);
            int tx = x + Math.max(8, (w - tw) / 2);
            int ty = y + (h - this.fuente.lineHeight) / 2;
            int color = vacio
                    ? Paleta.conAlfa(Paleta.tintaSecundaria(), 0.70F)
                    : Paleta.tintaSecundaria();
            g.drawString(this.fuente, texto, tx, ty, color, false);

            if (!vacio && this.isFocused() && (System.currentTimeMillis() / 500L) % 2L == 0L) {
                int cx = Math.min(x + w - 7, tx + tw + 1);
                g.fill(cx, ty - 1, cx + 1, ty + this.fuente.lineHeight + 1,
                        Paleta.conAlfa(Paleta.tintaPrincipal(), 0.72F));
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
            } else {
                this.setScrollAmount(0.0D);
            }
        }

        @Override
        public int getRowWidth() { return this.rowW; }

        @Override
        protected int getScrollbarPosition() { return this.getRowLeft() + this.getRowWidth() + 4; }
    }

    private final class EntradaIdioma extends ObjectSelectionList.Entry<EntradaIdioma> {
        private final String codigo;
        private final LanguageInfo info;
        private long ultimoClick;

        EntradaIdioma(String codigo, LanguageInfo info) {
            this.codigo = codigo;
            this.info = info;
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int rowWidth, int rowHeight,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            boolean pending = Objects.equals(PantallaIdiomaJobs.this.pendiente, this.codigo);
            boolean active = Objects.equals(PantallaIdiomaJobs.this.aplicado, this.codigo);
            if (pending || hovered) {
                g.fill(left + 2, top + 1, left + rowWidth - 2, top + rowHeight - 2,
                        Paleta.conAlfa(Paleta.PARED, pending ? 0.24F : 0.12F));
            }
            String prefijo = pending ? "> " : active ? "- " : "";
            String nombre = this.info.toComponent().getString();
            String codigoTxt = this.codigo;
            int cw = PantallaIdiomaJobs.this.font.width(codigoTxt);
            int maxNombre = Math.max(24, rowWidth - cw - 30);
            if (PantallaIdiomaJobs.this.font.width(prefijo + nombre) > maxNombre) {
                nombre = PantallaIdiomaJobs.this.font.plainSubstrByWidth(nombre,
                        Math.max(8, maxNombre - PantallaIdiomaJobs.this.font.width(prefijo + "..."))) + "...";
            }
            Component label = Component.literal(prefijo + nombre);
            int tx = left + 8;
            int ty = top + (rowHeight - PantallaIdiomaJobs.this.font.lineHeight) / 2;
            g.drawString(PantallaIdiomaJobs.this.font, label, tx, ty,
                    pending ? Paleta.tintaPrincipal() : Paleta.tintaSecundaria(), false);
            if (rowWidth > 190) {
                g.drawString(PantallaIdiomaJobs.this.font, codigoTxt,
                        left + rowWidth - cw - 8, ty,
                        Paleta.conAlfa(Paleta.tintaSecundaria(), 0.52F), false);
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
            PantallaIdiomaJobs.this.lista.setSelected(this);
            MezclaAudio.gesto(SonidosNivel.UI_ELEGIR, 0.36F);
            if (doble) PantallaIdiomaJobs.this.aplicarYCerrar();
            return true;
        }

        @Override
        public Component getNarration() { return this.info.toComponent(); }
    }
}
