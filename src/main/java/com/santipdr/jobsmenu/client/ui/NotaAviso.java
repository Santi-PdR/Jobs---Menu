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
import java.time.LocalDateTime;
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
 *
 * DONDE VIVE EL ESTADO
 *
 * El indice y el momento de la ultima vuelta son estaticos a proposito. La
 * pantalla se reconstruye entera cada vez que cambia el tamano de la ventana, y
 * si el estado viviese en la instancia, redimensionar el juego mandaria el
 * aviso de vuelta al primero.
 *
 * Pasar uno a mano no solo adelanta el indice: tambien reinicia el reloj, asi
 * el aviso recien traido dura los siete segundos completos y no los tres que le
 * quedaban al anterior.
 */
public class NotaAviso extends AbstractButton {

    /** Cantidad de avisos disponibles en los archivos de idioma. */
    public static final int AVISOS = 20;

    /**
     * Todas las notas especiales que la administracion puede colar por fecha u
     * hora. Estan aca, y no solo en especialDeHoy(), porque la hoja necesita
     * reservar alto para la mas larga de TODAS las notas -comunes y especiales-
     * antes de saber que dia es: una nota especial que parta en mas lineas
     * empujaria los renglones si la hoja no la hubiese tenido en cuenta. El
     * orden de esta lista no decide cual gana; de eso se ocupa especialDeHoy().
     */
    public static final String[] ESPECIALES = {
            "jobsmenu.aviso.especial.anonuevo",
            "jobsmenu.aviso.especial.navidad",
            "jobsmenu.aviso.especial.difuntos",
            "jobsmenu.aviso.especial.trabajador",
            "jobsmenu.aviso.especial.viernes13",
            "jobsmenu.aviso.especial.madrugada",
            "jobsmenu.aviso.especial.medianoche",
    };

    /** Cada cuantos milisegundos pasa solo al siguiente (configurable). */
    private static long rotacionMs() {
        return ConfigTurno.duracionAvisos() * 1_000L;
    }

    /** Cuanto se acerca el foco a su destino en cada fotograma. */
    private static final float SUAVIZADO = 0.25F;

    /** Avisos adelantados a mano desde que arranco el juego. */
    private static int corrimiento;

    /** Momento de la ultima vuelta, automatica o a mano. */
    private static long base = System.currentTimeMillis();

    private float foco;
    private boolean sonaba;
    private float luzFrame = 1.0F;

    /** Cache de la particion de texto: la nota cambia cada siete segundos. */
    private List<FormattedCharSequence> lineasCache = Collections.emptyList();
    private Component textoMedido = Component.empty();
    private int anchoMedido = -1;

    public NotaAviso(int x, int y, int ancho, int alto) {
        super(x, y, ancho, alto, Component.empty());
        this.foco = 0.0F;
        this.sonaba = false;
    }

    /** Actualiza la luz del snapshot de pantalla para evitar lecturas por widget. */
    public void setLuzFrame(float luz) {
        this.luzFrame = Math.max(0.0F, Math.min(1.0F, luz));
    }

    /** El aviso que toca ahora mismo. */
    private static int indice() {
        long vueltas = Math.floorDiv(System.currentTimeMillis() - base, rotacionMs());
        return (int) Math.floorMod(vueltas + corrimiento, AVISOS);
    }

    /**
     * La clave del aviso que se muestra: casi siempre el rotativo comun, pero
     * en ciertas fechas y horas la administracion cuela una nota propia.
     *
     * ES UN GUINO, NO UN CARTEL. Solo aparece cuando la fecha real coincide, y
     * ademas solo en una de cada cinco vueltas de la rotacion, para que quien
     * este mirando justo esos dias tenga que tener algo de suerte para leerla.
     * El que nunca abra el menu un 31 de octubre no se entera de que existe, y
     * esa es la idea: se descubre, no se anuncia. Todo sale del reloj del
     * sistema, sin estado.
     *
     * Los mensajes viven en lang, en la voz seca de siempre: nada de romper el
     * tono con chistes. Ver jobsmenu.aviso.especial.*.
     */
    private static Component textoActual() {
        int i = indice();
        // La nota especial no se roba todas las vueltas: una de cada cinco.
        if (i % 5 == 0) {
            String especial = especialDeHoy();
            if (especial != null) {
                return Component.translatable(especial);
            }
        }
        return Component.translatable("jobsmenu.aviso." + i);
    }

    /**
     * La nota especial que corresponde a la fecha y hora de hoy, o null.
     *
     * El orden importa: primero lo mas raro (una fecha concreta, que pasa una
     * vez al ano) y al final lo mas comun (una hora del dia, que vuelve cada
     * jornada). Asi un viernes 13 a las tres de la manana gana el viernes 13, y
     * la Navidad a medianoche gana la Navidad. Lo senalado siempre le gana a lo
     * cotidiano.
     */
    private static String especialDeHoy() {
        LocalDateTime ahora = LocalDateTime.now();
        int mes = ahora.getMonthValue();
        int dia = ahora.getDayOfMonth();
        int hora = ahora.getHour();
        int minuto = ahora.getMinute();

        // --- Fechas concretas: lo mas raro, gana siempre. ---
        if (mes == 1 && dia == 1) {
            return "jobsmenu.aviso.especial.anonuevo";
        }
        if (mes == 12 && (dia == 24 || dia == 25)) {
            return "jobsmenu.aviso.especial.navidad";
        }
        if (mes == 10 && dia == 31) {
            return "jobsmenu.aviso.especial.difuntos";
        }
        // El Dia del Trabajador: guino directo al nombre del server.
        if (mes == 5 && dia == 1) {
            return "jobsmenu.aviso.especial.trabajador";
        }
        if (dia == 13 && ahora.getDayOfWeek() == DayOfWeek.FRIDAY) {
            return "jobsmenu.aviso.especial.viernes13";
        }

        // --- Horas del dia: lo mas comun, solo si no cayo ninguna fecha. ---
        // La hora de las brujas: una ventana corta, no una hora entera diaria.
        if (hora == 3 && minuto >= 13 && minuto < 18) {
            return "jobsmenu.aviso.especial.madrugada";
        }
        // El cambio de turno solo durante los primeros cinco minutos.
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

        boolean encima = this.isHoveredOrFocused();
        if (encima && !this.sonaba) {
            // Mas bajo que el de los renglones: esto no es una opcion del
            // listado y no tiene que sonar como si lo fuese.
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

        Component texto = textoActual();

        int x = this.getX();
        int y = this.getY();
        int ancho = this.getWidth();
        List<FormattedCharSequence> lineas = lineas(cliente, texto);

        // Con la luz cortada, la letra chica es lo primero que deja de leerse.
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

        // Al pasar el cursor, una raya de lapiz por debajo que se dibuja sola
        // de izquierda a derecha. Tres pixeles de alto en total; no se mira,
        // se nota.
        if (this.foco > 0.0F) {
            int largo = Math.round(ancho * this.foco);
            grafico.fill(x, y + alto, x + largo, y + alto + 1,
                    Paleta.conAlfa(Paleta.TINTA_TENUE, 0.55F * this.foco * tinta));
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput salida) {
        salida.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                textoActual());
    }
}
