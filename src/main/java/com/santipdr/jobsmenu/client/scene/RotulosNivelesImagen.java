package com.santipdr.jobsmenu.client.scene;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Rotulos revisados contra las imagenes reales de los niveles 18-31.
 *
 * Los niveles anteriores siguen usando los lang JSON historicos. Este catalogo
 * queda separado a proposito para que los fondos entregados como imagen no
 * reciban nombres deducidos solo por paleta, indice o color dominante.
 * Los escapes Unicode conservan la ortografia visible sin romper la politica
 * ASCII del codigo Java que comprueba tools/verificar.py.
 */
public final class RotulosNivelesImagen {

    private static final int PRIMERO = 18;
    private static final int ULTIMO = 31;

    private static final String[] NOMBRES_ES = {
            "NIVEL 18 \u00b7 Interferencia carmes\u00ed",
            "NIVEL 19 \u00b7 La anomal\u00eda p\u00farpura",
            "NIVEL 20 \u00b7 El hu\u00e9sped de tinta",
            "NIVEL 21 \u00b7 El claro del centinela",
            "NIVEL 22 \u00b7 La caverna del vig\u00eda",
            "NIVEL 23 \u00b7 La c\u00e1mara de p\u00e1nico",
            "NIVEL 24 \u00b7 El umbral escarlata",
            "NIVEL 25 \u00b7 El bosque bajo la se\u00f1al",
            "NIVEL 26 \u00b7 La luna del observador",
            "NIVEL 27 \u00b7 La fortaleza roja",
            "NIVEL 28 \u00b7 El registro corrompido",
            "NIVEL 29 \u00b7 La entidad del borde",
            "NIVEL 30 \u00b7 El distrito de caza",
            "NIVEL 31 \u00b7 El nexo de contenci\u00f3n"
    };

    private static final String[] NOMBRES_EN = {
            "LEVEL 18 \u00b7 Crimson Interference",
            "LEVEL 19 \u00b7 The Violet Anomaly",
            "LEVEL 20 \u00b7 The Ink Host",
            "LEVEL 21 \u00b7 The Sentinel Clearing",
            "LEVEL 22 \u00b7 The Watcher Cavern",
            "LEVEL 23 \u00b7 The Panic Chamber",
            "LEVEL 24 \u00b7 The Scarlet Threshold",
            "LEVEL 25 \u00b7 The Forest Beneath the Signal",
            "LEVEL 26 \u00b7 The Watcher's Moon",
            "LEVEL 27 \u00b7 The Red Fortress",
            "LEVEL 28 \u00b7 The Corrupted Record",
            "LEVEL 29 \u00b7 The Edge Entity",
            "LEVEL 30 \u00b7 The Hunting District",
            "LEVEL 31 \u00b7 The Containment Nexus"
    };

    private static final String[][] NOTAS_ES = {
            {"La imagen se desgarra en rojo aunque la se\u00f1al figura como estable.", "No siga las l\u00edneas de interferencia: no corresponden a ning\u00fan pasillo.", "Si la silueta cambia entre parpadeos, abandone el punto de observaci\u00f3n."},
            {"El foco del cielo no es una estrella registrada por la instalaci\u00f3n.", "La neblina violeta oculta profundidad; no la use para calcular distancia.", "Mantenga la vista fuera del centro cuando el destello vuelva a abrirse."},
            {"La figura blanca no pertenece al inventario ni a ninguna cuadrilla.", "Las ramificaciones negras cambian de forma sin mover su punto de origen.", "No espere a distinguir un rostro para informar una presencia."},
            {"El terreno abierto no est\u00e1 vac\u00edo: la figura alta sigue en el per\u00edmetro.", "No cruce la l\u00ednea de \u00e1rboles aunque el centro del claro parezca seguro.", "Las formas del sector no responden a se\u00f1ales, luz ni llamadas."},
            {"La silueta del fondo permanece erguida cuando cambia la iluminaci\u00f3n.", "No use la pared de la cueva para estimar su altura.", "La ruta autorizada termina antes de la figura. No avance para comprobarlo."},
            {"Los registros visuales muestran actividad alrededor del ocupante.", "No toque estructuras org\u00e1nicas aunque parezcan inm\u00f3viles o decorativas.", "Si oye movimiento detr\u00e1s, contin\u00fae hacia la salida sin girarse."},
            {"El arco rojo no figura como acceso operativo en ning\u00fan plano.", "La figura junto al umbral no debe usarse como referencia de escala.", "No atraviese el centro aunque parezca despejado durante un apag\u00f3n."},
            {"La cuadr\u00edcula luminosa sobre los \u00e1rboles no pertenece al cielo exterior.", "Mantenga distancia de cualquier figura inm\u00f3vil en el l\u00edmite del bosque.", "Los errores de imagen pueden persistir despu\u00e9s del cambio de nivel."},
            {"La superficie blanca registra una presencia suspendida frente al planeta.", "No siga sombras que no coincidan con la direcci\u00f3n de la luz.", "Archive las lecturas del horizonte sin acercarse al borde del sector."},
            {"La estructura central recibe luz de una fuente que no figura en servicio.", "No use las siluetas del exterior como rutas de aproximaci\u00f3n.", "Las plantas superiores est\u00e1n cerradas aunque vea ventanas encendidas."},
            {"La imagen llega incompleta y con varias capas superpuestas.", "No intente corregir manualmente el desenfoque ni la rotaci\u00f3n del registro.", "Si aparecen s\u00edmbolos nuevos, an\u00f3telos sin quedarse frente a la pantalla."},
            {"La masa del fondo no mantiene una forma estable entre inspecciones.", "Permanezca detr\u00e1s de la l\u00ednea de observaci\u00f3n y no responda a sus movimientos.", "El magenta indica saturaci\u00f3n del registro, no una zona segura."},
            {"La criatura del sector supera la altura de las estructuras cercanas.", "No permanezca en espacios abiertos cuando la iluminaci\u00f3n cambie a rojo.", "Las rutas marcadas en el suelo son de evacuaci\u00f3n, no de aproximaci\u00f3n."},
            {"Las formas rojas convergen alrededor de objetos fuera de inventario.", "No retire luces, cubos ni fragmentos suspendidos dentro del per\u00edmetro.", "Si el patr\u00f3n empieza a cerrarse, salga antes de que complete el c\u00edrculo."}
    };

    private static final String[][] NOTAS_EN = {
            {"The image tears red even though the signal is logged as stable.", "Do not follow the interference lines; they match no registered corridor.", "If the silhouette moves between flickers, leave the observation point."},
            {"The light in the sky is not a star registered by the facility.", "The violet haze hides depth; do not use it to estimate distance.", "Keep your eyes away from the center when the flare opens again."},
            {"The pale figure belongs to neither inventory nor any registered crew.", "The black branches change shape without moving their point of origin.", "Do not wait to make out a face before reporting a presence."},
            {"The open ground is not empty; the tall figure remains in the perimeter.", "Do not cross the tree line even when the clearing looks safe.", "The shapes in this sector do not respond to signals, light, or calls."},
            {"The far silhouette remains upright when the lighting changes.", "Do not use the cave wall to estimate its height.", "The authorized route ends before the figure. Do not advance to verify it."},
            {"Visual records show activity gathering around the occupant.", "Do not touch organic structures even when they appear still or decorative.", "If you hear movement behind you, continue to the exit without turning."},
            {"The red arch is not listed as an operational access point on any plan.", "The figure beside the threshold must not be used as a scale reference.", "Do not cross the center even if it appears clear during a blackout."},
            {"The luminous grid above the trees does not belong to the outside sky.", "Keep your distance from any motionless figure at the forest edge.", "Image faults may persist after the level changes."},
            {"The white surface records a presence suspended before the planet.", "Do not follow shadows that disagree with the direction of the light.", "Archive horizon readings without approaching the edge of the sector."},
            {"The central structure is lit by a source not listed as in service.", "Do not use the exterior silhouettes as approach routes.", "Upper floors remain closed even when you can see lit windows."},
            {"This sector's image arrives incomplete and with overlapping layers.", "Do not manually correct the blur or rotation of the record.", "If new symbols appear, log them without remaining before the display."},
            {"The mass in the background keeps no stable shape between inspections.", "Remain behind the observation line and do not answer its movements.", "Magenta indicates record saturation, not a safe zone."},
            {"The creature in this sector rises above the nearby structures.", "Do not remain in open ground when the lighting turns red.", "Marked routes on the ground are for evacuation, not approach."},
            {"Red forms converge around objects absent from inventory records.", "Do not remove lights, cubes, or suspended fragments inside the perimeter.", "If the pattern begins to close, leave before it completes the circle."}
    };

    private RotulosNivelesImagen() {
    }

    public static Component nombre(Nivel nivel) {
        int numero = nivel.numero();
        if (!esImagenRevisada(numero)) {
            return Component.translatable("jobsmenu." + nivel.clave + ".nombre");
        }
        return Component.literal((espanol() ? NOMBRES_ES : NOMBRES_EN)[numero - PRIMERO]);
    }

    public static Component nota(Nivel nivel, int variante) {
        int numero = nivel.numero();
        if (!esImagenRevisada(numero)) {
            return Component.translatable("jobsmenu." + nivel.clave + ".nota" + variante);
        }
        int cual = Math.floorMod(variante, 3);
        String[][] notas = espanol() ? NOTAS_ES : NOTAS_EN;
        return Component.literal(notas[numero - PRIMERO][cual]);
    }

    private static boolean esImagenRevisada(int numero) {
        return numero >= PRIMERO && numero <= ULTIMO;
    }

    private static boolean espanol() {
        String idioma = Minecraft.getInstance().getLanguageManager().getSelected();
        return idioma != null && idioma.toLowerCase(java.util.Locale.ROOT).startsWith("es_");
    }
}
