package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * La linea de la administracion al pie de la hoja.
 *
 * Los avisos van rotando solos cada siete segundos, pero tambien se pueden
 * pasar a mano: el que quiere leerlos todos no tiene que esperar sentado, y el
 * que no los mira ni se entera de que se podia. Es la unica cosa de la pantalla
 * que cambia de contenido sin llevar a ninguna parte, y por eso suena distinto
 * de los renglones: no es una eleccion, es dar vuelta una hoja.
 */
public class NotaAviso extends AbstractButton {

    /** Cantidad de avisos disponibles en los archivos de idioma. */
    public static final int AVISOS = 20;

    /** Notas especiales que la hoja debe contemplar al medir su alto. */
    public static final String[] ESPECIALES = {
            "jobsmenu.aviso.especial.anonuevo",
            "jobsmenu.aviso.especial.navidad",
            "jobsmenu.aviso.especial.difuntos",
            "jobsmenu.aviso.especial.trabajador",
            "jobsmenu.aviso.especial.viernes13",
            "jobsmenu.aviso.especial.madrugada",
            "jobsmenu.aviso.especial.medianoche",
    };

    private static final float SUAVIZADO = 0.25F;

    /** Avisos adelantados a mano desde que arranco el juego. */
    private static int corrimiento;

    /** Momento de la ultima vuelta, automatica o a mano. */
    private static long base = System.currentTimeMillis();

    /** La fecha especial solo puede cambiar al cambiar de minuto. */
    private static long minutoEspecialCache = Long.MIN_VALUE;
    private static String especialCache;

    private float foco;
    private boolean sonaba;
    private float luzFrame = 1.0F;

    /** Cache de la particion de texto: la nota cambia cada varios segundos. */
    private List<FormattedCharSequence> lineasCache = Collections.emptyList();
    private Component textoMedido = Component.empty();
    private int anchoMedido = -1;

    /** Evita crear Component.translatable para la misma nota en cada frame. */
    private String claveTextoCache = "";
    private Component textoCache = Component.empty();

    public NotaAviso(int x, int y, int ancho, int alto) {
        super(x, y, ancho, alto, Component.empty());
        this.foco = 0.0F;
        this.sonaba = false;
    }

    /** Actualiza la luz del snapshot de pantalla para evitar lecturas por widget. */
    public void setLuzFrame(float luz) {
        this.luzFrame = Math.max(0.0F, Math.min(1.0F, luz));
    }

    private static long rotacionMs() {
        return ConfigTurno.duracionAvisos() * 1_000L;
    }

    /** El aviso que toca en un instante ya capturado por el frame. */
    private static int indice(long ahora) {
        long vueltas = Math.floorDiv(ahora - base, rotacionMs());
        return (int) Math.floorMod(vueltas + corrimiento, AVISOS);
    }

    private Component textoActual(long ahora) {
        int i = indice(ahora);
        String clave = null;
        if (i % 5 == 0) {
            clave = especialDeAhora(ahora);
        }
        if (clave == null) {
            clave = "jobsmenu.aviso." + i;
        }
        if (!clave.equals(this.claveTextoCache)) {
            this.claveTextoCache = clave;
            this.textoCache = Component.translatable(clave);
        }
        return this.textoCache;
    }

    /**
     * Resolver calendario es bastante mas caro que elegir un indice. Como las
     * ventanas especiales tienen precision de minutos, se calcula una vez por
     * minuto de reloj y todos los frames reutilizan el resultado.
     */
    private static String especialDeAhora(long ahoraMs) {
        long minuto = Math.floorDiv(ahoraMs, 60_000L);
        if (minuto == minutoEspecialCache) {
            return especialCache;
        }
        minutoEspecialCache = minuto;
        LocalDateTime ahora = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(ahoraMs), ZoneId.systemDefault());
        especialCache = especialDe(ahora);
        return especialCache;
    }

    private static String especialDe(LocalDateTime ahora) {
        int mes = ahora.getMonthValue();
        int dia = ahora.getDayOfMonth();
        int hora = ahora.getHour();
        int minuto = ahora.getMinute();

        if (mes == 1 && dia == 1) {
            return "jobsmenu.aviso.especial.anonuevo";
        }
        if (mes == 12 && (dia == 24 || dia == 25)) {
            return "jobsmenu.aviso.especial.navidad";
        }
        if (mes == 10 && dia == 31) {
            return "jobsmenu.aviso.especial.difuntos";
        }
        if (mes == 5 && dia == 1) {
            return "jobsmenu.aviso.especial.trabajador";
        }
        if (dia == 13 && ahora.getDayOfWeek() == DayOfWeek.FRIDAY) {
            return "jobsmenu.aviso.especial.viernes13";
        }
        if (hora == 3 && minuto >= 13 && minuto < 18) {
            return "jobsmenu.aviso.especial.madrugada";
        }
        if (hora == 0 && minuto < 5) {
            return "jobsmenu.aviso.especial.medianoche";
        }
        return null;
    }

    /** Sin el clac de fabrica: el gesto propio va en onPress(). */
    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager gestor) {
    }

    @Override
    public void onPress() {
        corrimiento++;
        base = System.currentTimeMillis();
        MezclaAudio.gesto(SonidosNivel.UI_ALTERNAR, 0.75F);
    }

    private List<FormattedCharSequence> lineas(Minecraft cliente, Component texto) {
        float escala = ConfigTurno.textoGrande() ? 1.15F : 1.0F;
        int ancho = Math.max(1, Math.round(this.getWidth() / escala));
        if (ancho != this.anchoMedido || !texto.equals(this.textoMedido)) {
            this.lineasCache = cliente.font.split(texto, ancho);
            this.textoMedido = texto;
            this.anchoMedido = ancho;
        }
        return this.lineasCache;
    }

    @Override
    public void renderWidget(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        Minecraft cliente = Minecraft.getInstance();
        long ahora = System.currentTimeMillis();

        boolean encima = this.isHoveredOrFocused();
        if (encima && !this.sonaba) {
            MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.40F);
        }
        this.sonaba = encima;

        float objetivo = encima ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido() || !ConfigTurno.escenaViva()) {
            this.foco = objetivo;
        } else {
            this.foco += (objetivo - this.foco) * SUAVIZADO;
            if (Math.abs(objetivo - this.foco) < 0.02F) {
                this.foco = objetivo;
            }
        }

        Component texto = textoActual(ahora);
        int x = this.getX();
        int y = this.getY();
        int ancho = this.getWidth();
        List<FormattedCharSequence> lineas = lineas(cliente, texto);

        float tinta = 0.10F + 0.90F * this.luzFrame;
        float escala = ConfigTurno.textoGrande() ? 1.15F : 1.0F;
        int color = Paleta.conAlfa(
                Paleta.mezclar(Paleta.tintaSecundaria(), Paleta.tintaPrincipal(), this.foco), tinta);
        int alto = 0;
        for (FormattedCharSequence linea : lineas) {
            if (escala == 1.0F) {
                grafico.drawString(cliente.font, linea, x, y + alto, color, false);
            } else {
                grafico.pose().pushPose();
                grafico.pose().translate(x, y + alto, 0.0D);
                grafico.pose().scale(escala, escala, 1.0F);
                grafico.drawString(cliente.font, linea, 0, 0, color, false);
                grafico.pose().popPose();
            }
            alto += Math.round(10.0F * escala);
        }

        if (this.foco > 0.0F) {
            int largo = Math.round(ancho * this.foco);
            grafico.fill(x, y + alto, x + largo, y + alto + 1,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, 0.55F * this.foco * tinta));
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput salida) {
        salida.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                textoActual(System.currentTimeMillis()));
    }
}
